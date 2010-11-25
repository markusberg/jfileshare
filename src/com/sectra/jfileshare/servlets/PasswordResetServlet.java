package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.utils.Helpers;
import com.sectra.jfileshare.utils.Sha512Crypt;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import java.util.logging.Logger;
import java.util.Properties;
import java.util.ArrayList;
import java.util.TreeMap;

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
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;

import javax.sql.DataSource;

public class PasswordResetServlet extends HttpServlet {

    private DataSource datasource = null;
    private static final Logger logger =
            Logger.getLogger(PasswordResetServlet.class.getName());
    private String SMTP_SERVER = "localhost";
    private String SMTP_SERVER_PORT = "25";
    private InternetAddress SMTP_SENDER;
    private String urlPrefix;
    private String pathContext;

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);
        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            datasource = (DataSource) env.lookup("jdbc/jfileshare");

            ServletContext context = getServletContext();
            SMTP_SERVER = context.getInitParameter("SMTP_SERVER").toString();
            SMTP_SERVER = SMTP_SERVER.equals("") ? "localhost" : SMTP_SERVER;

            SMTP_SERVER_PORT = context.getInitParameter("SMTP_SERVER_PORT").toString();
            SMTP_SERVER_PORT = SMTP_SERVER_PORT.equals("") ? "25" : SMTP_SERVER_PORT;

            urlPrefix = context.getInitParameter("URL_PREFIX").toString();

            SMTP_SENDER = new InternetAddress(context.getInitParameter("SMTP_SENDER").toString());
            SMTP_SENDER.validate();

        } catch (NamingException e) {
            logger.log(Level.SEVERE, "Throwing exception: {0}", e.toString());
            throw new ServletException(e);
        } catch (AddressException e) {
            logger.log(Level.WARNING, "SMTP_SENDER address is incorrect: {0}", e.toString());
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletContext app = getServletContext();

        TreeMap UserInfo = this.retrieveUserInfo(req.getPathInfo());
        if (UserInfo.get("key") != null) {
            if (UserInfo.get("username") != null) {
                req.setAttribute("username", (String) UserInfo.get("username"));
                req.setAttribute("key", (String) UserInfo.get("key"));
            } else {
                req.setAttribute("message_warning", "That password recovery key does not exist");
            }
        }

        RequestDispatcher disp = app.getRequestDispatcher("/templates/PasswordReset.jsp");
        disp.forward(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (req.getParameter("action") != null
                && req.getParameter("action").equals("PasswordResetRequest")) {
            String username = req.getParameter("username");

            UserItem oUser = new UserItem(datasource, username);
            String emailAddress = "";
            pathContext = req.getContextPath();

            if (urlPrefix.equals("")) {
                // We need to figure out the absolute path to the servlet
                String httpScheme = req.getScheme();
                String serverName = req.getServerName();
                Integer serverPort = (Integer) req.getServerPort();
                if ((serverPort == 80 && httpScheme.equals("http"))
                    || (serverPort == 443 && httpScheme.equals("https"))) {
                    serverPort = null;
                }

                urlPrefix = httpScheme + "://"
                        + serverName
                        + (serverPort != null ? ":" + serverPort.toString() : "");
                logger.log(Level.INFO, "No url prefix specified. Calculating: {0}", urlPrefix);
            }


            if (oUser.getUid() == null) {
                // username does not exist in database
                emailAddress = username + "@sectra.se";
            } else {
                emailAddress = oUser.getEmail();
            }

            if (!emailAddress.equals("")) {
                try {
                    InternetAddress emailRecipient = new InternetAddress(emailAddress);
                    emailRecipient.validate();
                    String key = Sha512Crypt.Sha512_crypt(emailAddress, null, 0);
                    key = key.substring(key.length() - 50, key.length());
                    if (sendResetInstructions(emailRecipient, key)) {
                        if (oUser.getUid() == null) {
                            req.setAttribute("message", "Account by that name was not found in the database. Instructions on how to reset your password have been sent to: " + emailRecipient.getAddress());
                        } else {
                            req.setAttribute("message", "Instructions on how to reset your password have been sent to the email address that is registered to the user \"" + oUser.getUsername() + "\".");
                        }
                        StoreRecoveryKey(username, emailAddress, key);
                    } else {
                        req.setAttribute("message_critical", "Unable to send email. Instructions on how to reset your password could not be sent to you");
                    }
                } catch (AddressException e) {
                    req.setAttribute("message_critical", "Unable to send email. \"" + Helpers.htmlSafe(emailAddress) + "\" doesn't validate as a real email address");
                }
            }


            if (1 == 0) {
                req.setAttribute("message_critical", "Unable to connect to database");
            }
            ServletContext app = getServletContext();
            RequestDispatcher disp = app.getRequestDispatcher("/templates/PasswordReset.jsp");

            disp.forward(req, resp);
        } else if (req.getParameter("action") != null
                && req.getParameter("action").equals("PasswordReset")) {
            // Validate passwords
            RequestDispatcher disp;
            ServletContext app = getServletContext();
            TreeMap UserInfo = this.retrieveUserInfo(req.getPathInfo());

            ArrayList<String> errors = new ArrayList<String>();
            UserItem user = new UserItem(datasource, (String) UserInfo.get("username"));

            String password1 = req.getParameter("password1") == null ? "" : req.getParameter("password1");
            String password2 = req.getParameter("password2") == null ? "" : req.getParameter("password2");
            errors.addAll(user.validatePassword(password1, password2));

            if (errors.size() > 0) {
                String errormessage = "Password reset failed due to the following " + (errors.size() == 1 ? "reason" : "reasons") + ":<ul>";
                for (String emsg : errors) {
                    errormessage = errormessage.concat("<li>" + emsg + "</li>\n");
                }
                errormessage = errormessage.concat("</ul>\n");
                req.setAttribute("message_critical", errormessage);
                req.setAttribute("username", UserInfo.get("username"));
                req.setAttribute("key", UserInfo.get("key"));
                disp = app.getRequestDispatcher("/templates/UserAdd.jsp");
            } else {
                user.setUsername((String) UserInfo.get("username"));
                user.setEmail((String) UserInfo.get("emailaddress"));
                if (user.getUid() == null) {
                    // Account didn't exist prior to pwreset attempt.
                    // Thus, this is a
                    // Sectra Corporate user
                    user.setUserType(UserItem.TYPE_INTERNAL);
                }
                user.save(datasource);
                req.setAttribute("message", "Password for user <strong>" + (String) UserInfo.get("username") + "</strong> has been reset. You can now login with your newly selected password.");
                this.DropRecoveryKey((String) UserInfo.get("key"));
            }

            if (1 == 0) {
                // FIXME: convert this to some form of exception handling
                req.setAttribute("message_critical", "Unable to connect to database. Please contact your system administrator.");
            }
            disp = app.getRequestDispatcher("/templates/PasswordReset.jsp");
            disp.forward(req, resp);

        } else { // Fallback to default page if required fields are missing
            doGet(req, resp);
        }
    }

    private boolean sendResetInstructions(InternetAddress emailRecipient, String key) {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", SMTP_SERVER);
        props.put("mail.smtp.port", SMTP_SERVER_PORT);
        Session session = Session.getInstance(props, null);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(emailRecipient);
            msg.setRecipient(Message.RecipientType.TO, emailRecipient);
            msg.setSender(SMTP_SENDER);

            msg.setSubject("Reset Password Instructions");

            MimeMultipart mp = new MimeMultipart();
            mp.setSubType("alternative");

            String txtBody = "A new password to your Sectra jfileshare account has been \n"
                    + "requested. Please follow this link in order to select a new password:\n"
                    + urlPrefix + pathContext + "/passwordreset/" + key + "\n"
                    + "(note: this link will be active for 48 hours)\n\n"
                    + "If you did not make this request, simply ignore this message and your\n"
                    + "password will remain unchanged.\n\n"
                    + "--\n"
                    + "Best regards\n"
                    + "The Sectra jfileshare system";
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(txtBody);

            String htmlBody = "<p>A new password to your Sectra jfileshare account has been requested. \n"
                    + "Please follow this link in order to select a new password:<br />\n"
                    + "<a href=\"" + urlPrefix + pathContext + "/passwordreset/" + key + "\">" + urlPrefix + pathContext + "/passwordreset/" + key + "</a><br />\n"
                    + "<em>(note: this link will be active for 48 hours)</em></p>\n\n"
                    + "<p>If you did not make this request, simply ignore this message and your \n"
                    + "password will remain unchanged.</p>\n"
                    + "<hr />\n"
                    + "<p>Best regards<br />\n"
                    + "The Sectra jfileshare system</p>\n";
            MimeBodyPart mbp2 = new MimeBodyPart();
            mbp2.setContent(htmlBody, "text/html");

            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);
            msg.setContent(mp);

            Transport.send(msg);

            return true;
        } catch (MessagingException e) {
            logger.log(Level.WARNING, "Unable to send notification email: {0}", e.toString());
            return false;
        }
    }

    private void DropRecoveryKey(String key) {
        // Drop recovery key from database
        Connection dbConn = null;
        try {
            dbConn = datasource.getConnection();
            PreparedStatement st = dbConn.prepareStatement("delete from PasswordReset where `key`=?");
            st.setString(1, key);
            st.executeUpdate();
        } catch (SQLException ignored) {
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    private void StoreRecoveryKey(String username,
            String emailaddress,
            String key) {
        // Insert key into recovery database
        Connection dbConn = null;
        try {
            dbConn = datasource.getConnection();
            PreparedStatement st = dbConn.prepareStatement("insert into PasswordReset values(now(),?,?,?)");
            st.setString(1, username);
            st.setString(2, emailaddress);
            st.setString(3, key);
            st.executeUpdate();
        } catch (SQLException ignored) {
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /***
     * Fetch information from recovery table
     * @param key
     * @return
     */
    private TreeMap<String, String> retrieveUserInfo(String key) {
        Connection dbConn = null;
        TreeMap<String, String> UserInfo = new TreeMap<String, String>();
        try {
            // Strip the leading slash from key
            key = key.substring(1);
        } catch (NullPointerException e) {
            key = "";
        }

        if (!key.equals("")) {
            UserInfo.put("key", key);
            try {
                dbConn = datasource.getConnection();

                PreparedStatement st = dbConn.prepareStatement("select username,emailaddress from PasswordReset where `key`=?");
                st.setString(1, key);
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    UserInfo.put("username", rs.getString("username"));
                    UserInfo.put("emailaddress", rs.getString("emailaddress"));
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Unable to connect to database: {0}", e.toString());
            } finally {
                if (dbConn != null) {
                    try {
                        dbConn.close();
                    } catch (SQLException e) {
                    }
                }
            }
        }
        return UserInfo;
    }
}
