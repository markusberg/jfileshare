/**
 *  Copyright 2011 SECTRA Imtec AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * @author      Markus Berg <markus.berg @ sectra.se>
 * @version     1.6
 * @since       2011-09-21
 */
package nu.kelvin.jfileshare.servlets;

import nu.kelvin.jfileshare.objects.Conf;
import nu.kelvin.jfileshare.objects.UserItem;
import nu.kelvin.jfileshare.objects.NoSuchUserException;
import nu.kelvin.jfileshare.utils.Helpers;
import nu.kelvin.jfileshare.utils.Sha512Crypt;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Date;
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
    static final long serialVersionUID = 1L;

    private DataSource datasource = null;
    private static final Logger logger =
            Logger.getLogger(PasswordResetServlet.class.getName());

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);
        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            datasource = (DataSource) env.lookup("jdbc/jfileshare");

        } catch (NamingException e) {
            logger.log(Level.SEVERE, "Throwing exception: {0}", e.toString());
            throw new ServletException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletContext app = getServletContext();

        TreeMap<String, String> UserInfo = this.retrieveUserInfo(req.getPathInfo());
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
        Conf conf = (Conf) getServletContext().getAttribute("conf");
        if ("PasswordResetRequest".equals(req.getParameter("action"))) {
            String username = req.getParameter("username");
            ServletContext app = getServletContext();
            RequestDispatcher disp = app.getRequestDispatcher("/templates/PasswordReset.jsp");

            UserItem user = new UserItem();
            try {
                user.fetch(datasource, username);
            } catch (NoSuchUserException e) {
                // Doesn't exist in database.
                // Assume that this is a Corporate-internal user
                user.setEmail(username.concat("@").concat(conf.getBrandingDomain()));
            } catch (SQLException e) {
                req.setAttribute("message_critical", e.toString());
                disp = app.getRequestDispatcher("/templates/Error.jsp");
                disp.forward(req, resp);
                return;
            }

            try {
                InternetAddress emailRecipient = new InternetAddress(user.getEmail());
                emailRecipient.validate();
                String key = Sha512Crypt.Sha512_crypt(user.getEmail(), null, 0);
                key = key.substring(key.length() - 50, key.length());
                if (SendResetInstructions(emailRecipient, key, conf)
                        && StoreRecoveryKey(username, user.getEmail(), key)) {
                    if (user.getUid() == null) {
                        req.setAttribute("message", "Account by that name was not found in the database. Instructions on how to reset your password have been sent to: " + emailRecipient.getAddress());
                    } else {
                        req.setAttribute("message", "Instructions on how to reset your password have been sent to the email address that is registered to the user \"" + user.getUsername() + "\".");
                    }
                    disp = app.getRequestDispatcher("/templates/Blank.jsp");
                } else {
                    req.setAttribute("message_critical", "Unable to send email. This is likely a server error. Please try again later, or contact the server administrator");
                }
            } catch (AddressException e) {
                req.setAttribute("message_critical", "Unable to send email. \"" + Helpers.htmlSafe(user.getEmail()) + "\" doesn't validate as a real email address");
            }

            disp.forward(req, resp);
        } else if ("PasswordReset".equals(req.getParameter("action"))) {
            ServletContext app = getServletContext();
            RequestDispatcher disp = app.getRequestDispatcher("/templates/PasswordReset.jsp");
            TreeMap<String,String> UserInfo = this.retrieveUserInfo(req.getPathInfo());

            UserItem user = new UserItem();
            ArrayList<String> errors = new ArrayList<String>();
            try {
                user.fetch(datasource, (String) UserInfo.get("username"));
            } catch (NoSuchUserException e) {
                // Account didn't exist prior to pwreset attempt.
                // Thus, this is an internal user
                user.setUserType(UserItem.TYPE_INTERNAL);
            } catch (SQLException e) {
                req.setAttribute("message_critical", e.toString());
                disp = app.getRequestDispatcher("/templates/Error.jsp");
                disp.forward(req, resp);
                return;
            }

            String password1 = req.getParameter("password1") == null ? "" : req.getParameter("password1");
            String password2 = req.getParameter("password2") == null ? "" : req.getParameter("password2");
            errors.addAll(user.validatePassword(password1, password2));

            if (errors.isEmpty()) {
                user.setUsername((String) UserInfo.get("username"));
                user.setEmail((String) UserInfo.get("emailaddress"));
                if (user.getUid() == null) {
                    user.create(datasource, req.getRemoteAddr());
                } else {
                    user.update(datasource, req.getRemoteAddr());
                }

                req.setAttribute("message", "Password for user <strong>" + Helpers.htmlSafe((String) UserInfo.get("username")) + "</strong> has been reset. You can now login with your newly selected password.");
                this.DropRecoveryKey((String) UserInfo.get("key"));
                disp = app.getRequestDispatcher("/templates/Blank.jsp");
            } else {
                String errormessage = "Password reset failed due to the following " + (errors.size() == 1 ? "reason" : "reasons") + ":<ul>";
                for (String emsg : errors) {
                    errormessage = errormessage.concat("<li>" + emsg + "</li>\n");
                }
                errormessage = errormessage.concat("</ul>\n");
                req.setAttribute("message_critical", errormessage);
                req.setAttribute("username", UserInfo.get("username"));
                req.setAttribute("key", UserInfo.get("key"));
            }

            disp.forward(req, resp);
        } else {
            // Fallback to default page if required fields are missing
            doGet(req, resp);
        }
    }

    private boolean SendResetInstructions(InternetAddress emailRecipient, String key, Conf conf) {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", conf.getSmtpServer());
        props.put("mail.smtp.port", conf.getSmtpServerPort());
        Session session = Session.getInstance(props, null);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setSender(emailRecipient);
            msg.setRecipient(Message.RecipientType.TO, emailRecipient);
            msg.setFrom(conf.getSmtpSender());
			msg.setSentDate(new Date());

            msg.setSubject("Reset Password Instructions");

            MimeMultipart mp = new MimeMultipart();
            mp.setSubType("alternative");

            String txtBody = "A new password to your " + conf.getBrandingOrg()
                    + " jfileshare account has been \n"
                    + "requested. Please follow this link in order to select a new password:\n"
                    + conf.getBaseUrl() + "/passwordreset/" + key + "\n"
                    + "(note: this link will be active for 48 hours)\n\n"
                    + "If you did not make this request, simply ignore this message and your\n"
                    + "password will remain unchanged.\n\n"
                    + "--\n"
                    + "Best regards\n"
                    + "The " + conf.getBrandingOrg() + " jfileshare system";
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(txtBody);

            String htmlBody = "<p>A new password to your " + conf.getBrandingOrg()
                    + " jfileshare account has been requested. \n"
                    + "Please follow this link in order to select a new password:<br />\n"
                    + "<a href=\"" + conf.getBaseUrl() + "/passwordreset/" + key + "\">" + conf.getBaseUrl() + "/passwordreset/" + key + "</a><br />\n"
                    + "<em>(note: this link will be active for 48 hours)</em></p>\n\n"
                    + "<p>If you did not make this request, simply ignore this message and your \n"
                    + "password will remain unchanged.</p>\n"
                    + "<hr />\n"
                    + "<p>Best regards<br />\n"
                    + "The " + conf.getBrandingOrg() + " jfileshare system</p>\n";
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
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private boolean StoreRecoveryKey(String username,
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
            return true;
        } catch (SQLException ignored) {
            return false;
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException ignored) {
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
