package com.sectra.jfileshare.ajax;

import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.objects.NoSuchFileException;
import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.utils.Helpers;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.SQLException;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;

import javax.sql.DataSource;

public class FileNotificationServlet extends HttpServlet {

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(FileNotificationServlet.class.getName());
    private String SMTP_SERVER;
    private String SMTP_SERVER_PORT;
    private InternetAddress SMTP_SENDER;
    private String URL_PREFIX;
    private String pathContext;

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            ds = (DataSource) env.lookup("jdbc/jfileshare");

            ServletContext context = getServletContext();
            SMTP_SERVER = context.getInitParameter("SMTP_SERVER").toString();
            SMTP_SERVER = SMTP_SERVER.equals("") ? "localhost" : SMTP_SERVER;

            SMTP_SERVER_PORT = context.getInitParameter("SMTP_SERVER_PORT").toString();
            SMTP_SERVER_PORT = SMTP_SERVER_PORT.equals("") ? "25" : SMTP_SERVER_PORT;

            URL_PREFIX = context.getInitParameter("URL_PREFIX").toString();

            SMTP_SENDER = new InternetAddress(context.getInitParameter("SMTP_SENDER").toString());
            SMTP_SENDER.validate();

        } catch (NamingException e) {
            throw new ServletException(e);
        } catch (AddressException e) {
            logger.log(Level.WARNING, "SMTP_SENDER address is invalid: {0}", e.toString());
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        pathContext = req.getContextPath();
        if (URL_PREFIX.equals("")) {
            // We need to figure out the absolute path to the servlet
            String httpScheme = req.getScheme();
            String serverName = req.getServerName();
            Integer serverPort = (Integer) req.getServerPort();
            if ((serverPort == 80 && httpScheme.equals("http"))
                    || (serverPort == 443 && httpScheme.equals("https"))) {
                serverPort = null;
            }

            URL_PREFIX = httpScheme + "://"
                    + serverName
                    + (serverPort != null ? ":" + serverPort.toString() : "");
            logger.log(Level.INFO, "No url prefix specified. Calculating: {0}", URL_PREFIX);
        }

        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession();
        StringBuilder buffy = new StringBuilder();
        resp.setContentType("text/xml;charset=UTF-8");
        buffy.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        buffy.append("<response>\n");

        UserItem currentUser = (UserItem) session.getAttribute("user");

        try {
            int iFid = Integer.parseInt(req.getParameter("iFid"));
            String emailRecipient = req.getParameter("emailRecipient");
            FileItem file = new FileItem(ds, iFid);

            // You must have edit access to the file in order to notify
            // someone of it's existence
            InternetAddress emailValidated = new InternetAddress();
            if (!currentUser.hasEditAccessTo(file)) {
                throw new Exception("You do not have admin access to that file.");
            }
            // Email address sanity check
            emailValidated = new InternetAddress(emailRecipient);
            emailValidated.validate();

            // Everything checks out. Let's send the email notification
            sendEmailNotification(file, currentUser, emailValidated);
            buffy.append("\t<status>info</status>\n");
            buffy.append("\t<msg>Email notification has been sent to ");
            buffy.append(Helpers.htmlSafe(emailRecipient));
            buffy.append(" regarding the file \"");
            buffy.append(file.getName());
            buffy.append("\"</msg>\n");
        } catch (NoSuchFileException e) {
            buffy.append("\t<status>warning</status>\n");
            buffy.append("\t<msg>".concat(e.getMessage()).concat("</msg>\n"));
        } catch (SQLException e) {
            buffy.append("\t<status>critical</status>\n");
            buffy.append("\t<msg>".concat(e.getMessage()).concat("</msg>\n"));
        } catch (AddressException e) {
            buffy.append("\t<status>warning</status>\n");
            buffy.append("\t<msg>Unable to send email. ".concat(e.getMessage()).concat("</msg>\n"));
            // "\"" + Helpers.htmlSafe(emailRecipient) + "\" does not look like a valid email address";
        } catch (MessagingException e) {
            logger.log(Level.WARNING, "Unable to send notification email: {0}", e.toString());
            buffy.append("\t<status>critical</status>\n");
            buffy.append("\t<msg>Failed to send email notification.");
            buffy.append("Reason unknown. Please try again later, or contact ");
            buffy.append("the server administrator.\n");
            buffy.append(e.toString().concat("</msg>\n"));
        } catch (NullPointerException ignore) {
            // This will happen if there's no currentUser object
            // We'll just send an empty xml response
            buffy.append("\t<status>sessionexpired</status>\n");
        } catch (Exception e) {
            buffy.append("\t<status>warning</status>\n");
            buffy.append("\t<msg>".concat(e.getMessage()).concat("</msg>\n"));
        }

        buffy.append("</response>\n");

        out.println(buffy.toString());
        out.flush();
        out.close();
    }

    private void sendEmailNotification(FileItem file, UserItem currentUser, InternetAddress emailRecipient)
            throws MessagingException {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", SMTP_SERVER);
        props.put("mail.smtp.port", SMTP_SERVER_PORT);
        Session session = Session.getInstance(props, null);

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(currentUser.getEmail()));
        msg.setRecipient(Message.RecipientType.TO, emailRecipient);
        msg.setSender(SMTP_SENDER);
        msg.setSubject("File " + file.getName() + " available for download");

        MimeMultipart mp = new MimeMultipart();
        mp.setSubType("alternative");

        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText(currentUser.getUsername() + " has made "
                + "the following file available for download:\n"
                + "Filename: " + file.getName() + "\n"
                + "Filesize: " + FileItem.humanReadable(file.getSize()) + "\n\n"
                + file.getURL(URL_PREFIX + pathContext)
                + (file.getDateExpiration() == null ? "" : "\n(note: this link will expire in " + file.getDaysUntilExpiration() + " day(s))"), "utf-8");

        MimeBodyPart mbp2 = new MimeBodyPart();
        mbp2.setContent("<h1>File available for download</h1>"
                + "<p>" + currentUser.getUsername() + " has made the following \n"
                + "file available for download:</p>\n"
                + "<table>\n"
                + "<tr><th style=\"text-align: right;\">Filename:</th><td>" + file.getName() + "</td></tr>\n"
                + "<tr><th style=\"text-align: right;\">Filesize:</th><td>" + FileItem.humanReadable(file.getSize()) + "</td></tr>\n"
                + "</table>\n"
                + "<p><a href=\"" + file.getURL(URL_PREFIX + pathContext) + "\">" + file.getURL(URL_PREFIX + pathContext) + "</a>\n"
                + (file.getDateExpiration() == null ? "" : "<br/>(note: this link will expire in " + file.getDaysUntilExpiration() + " day(s))")
                + "</p>\n", "text/html; charset=utf-8");

        /* Possibly attach image to make it look nicer
        mbp3 = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        mbp3.setDataHandler(new DataHandler(source));
        mbp3.setHeader("Content-ID", "<jfilesharelogo>);
         * Använd <img src="cid:jfilesharelogo" />
         *
         *  mp.setSubType("alternative");
         *
         */

        mp.addBodyPart(mbp1);
        mp.addBodyPart(mbp2);

        // mp.addBodyPart(mbp3);

        msg.setContent(mp);

        Transport.send(msg);
    }
}
