package com.sectra.jfileshare.ajax;

import com.sectra.jfileshare.objects.Conf;
import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.objects.NoSuchFileException;
import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.utils.Helpers;
import gnu.mail.providers.smtp.SMTPTransport;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.SQLException;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportListener;
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

import javax.sql.DataSource;

public class FileNotificationServlet extends HttpServlet {
    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(FileNotificationServlet.class.getName());
    
    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            ds = (DataSource) env.lookup("jdbc/jfileshare");
        } catch (NamingException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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
                throw new Exception("You do not have admin access to that file");
            }
            // Email address sanity check
            emailValidated = new InternetAddress(emailRecipient);
            emailValidated.validate();

            // Everything checks out. Let's send the email notification
            sendEmailNotification(file, currentUser, emailValidated);
            buffy.append("\t<status>info</status>\n");
            buffy.append("\t<msg>Email notification has been sent to &lt;strong&gt;");
            buffy.append(Helpers.htmlSafe(emailRecipient));
            buffy.append("&lt;/strong&gt; regarding the file &lt;strong&gt;\"");
            buffy.append(file.getName());
            buffy.append("\"&lt;/strong&gt;</msg>\n");
        } catch (NoSuchFileException e) {
            buffy.append("\t<status>warning</status>\n");
            buffy.append("\t<msg>".concat(e.getMessage()).concat("</msg>\n"));
        } catch (SQLException e) {
            buffy.append("\t<status>critical</status>\n");
            buffy.append("\t<msg>".concat(e.getMessage()).concat("</msg>\n"));
        } catch (AddressException e) {
            buffy.append("\t<status>warning</status>\n");
            buffy.append("\t<msg>Unable to send email. ".concat(e.getMessage()).concat("</msg>\n"));
        } catch (MessagingException e) {
            logger.log(Level.WARNING, "Unable to send notification email: {0}", e.toString());
            buffy.append("\t<status>critical</status>\n");
            buffy.append("\t<msg>Failed to send email notification. ");
            buffy.append("Reason unknown. Please try again later, or contact ");
            buffy.append("the server administrator.</msg>\n");
            buffy.append("\t<stacktrace>");
            buffy.append(e.toString().concat("</stacktrace>\n"));
        } catch (NullPointerException ignore) {
            // This will happen if there's no currentUser object
            // We'll just send a sessionexpired-message to the UA to
            // trigger a logout.
            buffy.append("\t<status>sessionexpired</status>\n");
        } catch (Exception e) {
            buffy.append("\t<status>warning</status>\n");
            buffy.append("\t<msg>Unknown error</msg>\n");
            buffy.append("\t<stacktrace>".concat(e.getMessage()).concat("</stacktrace>\n"));
        }

        buffy.append("</response>\n");

        out.println(buffy.toString());
        out.flush();
        out.close();
    }

    private void sendEmailNotification(FileItem file,
            UserItem currentUser,
            InternetAddress emailRecipient)
            throws MessagingException, SendFailedException {
        Conf conf = (Conf) getServletContext().getAttribute("conf");

        Properties props = System.getProperties();
        // props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", conf.getSmtpServer());
        props.put("mail.smtp.port", ((Integer) conf.getSmtpServerPort()).toString());
        props.put("mail.smtp.reportsuccess", "true");
        Session session = Session.getInstance(props, null);

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(currentUser.getEmail()));
        msg.setRecipient(Message.RecipientType.TO, emailRecipient);
        msg.setSender(conf.getSmtpSender());
        msg.setSubject("File " + file.getName() + " available for download");

        MimeMultipart mp = new MimeMultipart();
        mp.setSubType("alternative");

        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText(currentUser.getUsername() + " has made "
                + "the following file available for download:\n"
                + "Filename: " + file.getName() + "\n"
                + "Filesize: " + FileItem.humanReadable(file.getSize()) + "\n\n"
                + file.getURL(conf.getBaseUrl())
                + (file.getDateExpiration() == null ? "" : "\n(note: this link will expire in " + file.getDaysUntilExpiration() + " day(s))"), "utf-8");

        MimeBodyPart mbp2 = new MimeBodyPart();
        mbp2.setContent("<h1>File available for download</h1>"
                + "<p><strong>" + currentUser.getUsername() + "</strong> has made the following \n"
                + "file available for download:</p>\n"
                + "<table>\n"
                + "<tr><th style=\"text-align: right;\">Filename:</th><td>" + file.getName() + "</td></tr>\n"
                + "<tr><th style=\"text-align: right;\">Filesize:</th><td>" + FileItem.humanReadable(file.getSize()) + "</td></tr>\n"
                + "</table>\n"
                + "<p><a href=\"" + file.getURL(conf.getBaseUrl()) + "\">" + file.getURL(conf.getBaseUrl()) + "</a>\n"
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
        SMTPTransport transport = (SMTPTransport)session.getTransport("smtp");
        transport.connect();
        transport.sendMessage(msg, msg.getAllRecipients());
        transport.close();

        // Transport.send(msg);

        logger.log(Level.INFO, "Sending email notification to {0}", emailRecipient.getAddress());
    }
}
