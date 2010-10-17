package com.sectra.jfileshare.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;

import javax.sql.DataSource;

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

import java.io.IOException;

import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Properties;

import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.utils.Helpers;

public class UserViewServlet extends HttpServlet {

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(UserViewServlet.class.getName());
    private String SMTP_SERVER;
    private String SMTP_SERVER_PORT;
    private InternetAddress SMTP_SENDER;
    private String urlPrefix;

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

            urlPrefix = context.getInitParameter("URL_PREFIX").toString();

            SMTP_SENDER = new InternetAddress(context.getInitParameter("SMTP_SENDER").toString());
            SMTP_SENDER.validate();

        } catch (NamingException e) {
            throw new ServletException(e);
        } catch (AddressException e) {
            logger.warning("SMTP_SENDER address is invalid: " + e.toString());
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        UserItem oCurrentUser = (UserItem) session.getAttribute("user");
        ServletContext app = getServletContext();
        RequestDispatcher disp;

        String reqUid = req.getPathInfo();
        int iUid;

        try {
            reqUid = reqUid.replaceAll("/", "");
            if (reqUid.equals("")) {
                iUid = oCurrentUser.getUid();
            } else {
                iUid = Integer.parseInt(reqUid);
            }
            logger.info("Requesting uid: " + reqUid);
        } catch (NumberFormatException n) {
            iUid = -1;
        } catch (NullPointerException n) {
            iUid = oCurrentUser.getUid();
        }

        UserItem oUser = new UserItem(ds, iUid);

        if (oUser.getUid() == -2) {
            req.setAttribute("message_critical", "Unable to connect to database. Please contact your system administrator.");
            req.setAttribute("tab", "Error");
            disp = app.getRequestDispatcher("/templates/Blank.jsp");
        } else if (oUser.getUid() == null) {
            disp = app.getRequestDispatcher("/templates/404.jsp");
            req.setAttribute("message_warning", "User not found (" + reqUid + ")");
        } else if (!(oCurrentUser.isAdmin()
                || oUser.getUidCreator() == oCurrentUser.getUid()
                || oUser.getUid() == oCurrentUser.getUid())) {
            // logger.info("Currentuser: " + Integer.toString(oCurrentUser.getUid()));
            // logger.info("Creator uid: " + Integer.toString(oUser.getUidCreator()));
            req.setAttribute("message_warning", "You are not authorized to view the details of this user.");
            disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
        } else {
            if (iUid != oCurrentUser.getUid()) {
                req.setAttribute("tab", oUser.getUsername());
            }

            req.setAttribute("oUser", oUser);
            req.setAttribute("aFiles", oUser.getFiles(ds));
            req.setAttribute("aUsers", oUser.getChildren(ds));
            disp = app.getRequestDispatcher("/templates/UserView.jsp");
        }
        disp.forward(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (req.getParameter("action") != null
                && req.getParameter("action").equals("donotify")) {

            HttpSession session = req.getSession();
            UserItem oCurrentUser = (UserItem) session.getAttribute("user");

            int iFid = Integer.parseInt(req.getParameter("iFid"));
            String emailRecipient = req.getParameter("emailRecipient");

            ArrayList<String> errors = new ArrayList<String>();
            FileItem oFile = new FileItem(ds, iFid);

            if (oCurrentUser.isAdmin()
                    || oFile.getOwnerUid() != oCurrentUser.getUid()) {
                if (oFile.getFid() == -1) {
                    errors.add("The file was not found");
                }
                // Email address sanity check
                InternetAddress emailValidated = new InternetAddress();
                try {
                    emailValidated = new InternetAddress(emailRecipient);
                    emailValidated.validate();
                } catch (AddressException e) {
                    errors.add("\"" + Helpers.htmlSafe(emailRecipient) + "\" does not look like a valid email address");
                }

                if (errors.size() > 0) {
                    String errormessage = "Email notification failed due to the following " + (errors.size() == 1 ? "reason" : "reasons") + ":<ul>";
                    for (String emsg : errors) {
                        errormessage = errormessage.concat("<li>" + emsg + "</li>\n");
                    }
                    errormessage = errormessage.concat("</ul>\n");
                    req.setAttribute("message_critical", errormessage);
                } else {
                    // Everything checks out. Let's send the email notification
                    if (urlPrefix.equals("")) {
                        // We need to figure out the absolute path to the servlet
                        String httpScheme = req.getScheme();
                        String serverName = req.getServerName();
                        Integer serverPort = (Integer) req.getServerPort();
                        if (serverPort == 80) {
                            serverPort = null;
                        }

                        urlPrefix = httpScheme + "://"
                                + serverName
                                + (serverPort != null ? ":" + serverPort.toString() : "")
                                + req.getContextPath();
                        logger.info("No url prefix specified. Calculating: " + urlPrefix);
                    }
                    if (sendEmailNotification(oFile, oCurrentUser, emailValidated)) {
                        req.setAttribute("message", "Email notification has been sent to " + Helpers.htmlSafe(emailRecipient) + " regarding the file \"" + oFile.getName() + "\"");
                    } else {
                        req.setAttribute("message_warning", "Failed to send email notification.");
                    }
                }
            }
        }
        doGet(req, resp);
    }

    private boolean sendEmailNotification(FileItem oFile, UserItem oCurrentUser, InternetAddress emailRecipient) {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", SMTP_SERVER);
        props.put("mail.smtp.port", SMTP_SERVER_PORT);
        Session session = Session.getInstance(props, null);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(oCurrentUser.getEmail()));
            msg.setRecipient(Message.RecipientType.TO, emailRecipient);
            msg.setSender(SMTP_SENDER);
            msg.setSubject("File " + oFile.getName() + " available for download");

            MimeMultipart mp = new MimeMultipart();
            mp.setSubType("alternative");

            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(oCurrentUser.getUsername() + " has made "
                    + "the following file available for download:\n"
                    + "Filename: " + oFile.getName() + "\n"
                    + "Filesize: " + FileItem.humanReadable(oFile.getSize()) + "\n\n"
                    + oFile.getURL(urlPrefix));

            MimeBodyPart mbp2 = new MimeBodyPart();
            mbp2.setContent("<h1>File available for download</h1>"
                    + "<p>" + oCurrentUser.getUsername() + " has made the following "
                    + "file available for download:</p>"
                    + "<table>"
                    + "<tr><th style=\"text-align: right;\">Filename:</th><td>" + oFile.getName() + "</td></tr>"
                    + "<tr><th style=\"text-align: right;\">Filesize:</th><td>" + FileItem.humanReadable(oFile.getSize()) + "</td></tr>"
                    + "</table>"
                    + "<p><a href=\"" + oFile.getURL(urlPrefix) + "\">" + oFile.getURL(urlPrefix) + "</a>"
                    + "</p>", "text/html");

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
            return true;
        } catch (MessagingException e) {
            logger.warning("Unable to send notification email: " + e.toString());
            return false;
        }
    }
}

