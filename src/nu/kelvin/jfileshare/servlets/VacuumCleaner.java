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
 * @version     1.16
 * @since       2012-03-14
 */
package nu.kelvin.jfileshare.servlets;

import nu.kelvin.jfileshare.objects.Conf;
import nu.kelvin.jfileshare.objects.FileItem;
import nu.kelvin.jfileshare.objects.NoSuchUserException;
import nu.kelvin.jfileshare.objects.UserItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.sql.DataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class VacuumCleaner extends HttpServlet {

    static final long serialVersionUID = 1L;
    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(VacuumCleaner.class.getName());
    private long VACUUM_INTERVAL = 1000 * 60;
    // private long VACUUM_INTERVAL = 1000;
    private Timer timer = null;

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
        timer = new Timer();
        timer.scheduleAtFixedRate(new PerformVacuum(), 0, VACUUM_INTERVAL);
    }

    @Override
    public void destroy() {
        timer.cancel();
        timer = null;
    }

    class PerformVacuum extends TimerTask {

        @Override
        public void run() {
            vacuum();
        }
    }

    /*
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    vacuum();
    }
     */
    private void vacuum() {
        // logger.info("Running scheduled vacuum of database");
        Connection dbConn = null;
        Conf conf = (Conf) getServletContext().getAttribute("conf");
        if (conf == null) {
            conf = new Conf(ds);
        }

        // Delete expired users
        ArrayList<UserItem> users = UserItem.fetchExpired(ds);
        if (!users.isEmpty()) {
            logger.log(Level.INFO, "Vacuuming {0} expired user(s) from the database", users.size());
            for (UserItem user : users) {
                user.delete(ds, conf.getPathStore(), "vacuum");
            }
        }

        // Delete expired files
        ArrayList<FileItem> files = FileItem.fetchExpired(ds);
        if (!files.isEmpty()) {
            logger.log(Level.INFO, "Vacuuming {0} expired file(s) from the database", files.size());
            for (FileItem file : files) {
                file.delete(ds, conf.getPathStore(), "vacuum");
            }
        }

        // Delete password requests older than 2 days
        try {
            dbConn = ds.getConnection();
            Statement st = dbConn.createStatement();
            int i = st.executeUpdate("DELETE FROM PasswordReset where dateRequest < ( now() - INTERVAL 2 DAY )");

            if (i > 0) {
                logger.log(Level.INFO, "Vacuuming {0} entries from password reset table", Integer.toString(i));
            }
            st.close();
            dbConn.close();
        } catch (SQLException e) {
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }
        }

        // Auto-expiration of files that haven't been downloaded or edited for a while
        if (conf.getMonthsFileAutoExpiration() != 0) {
            // logger.log(Level.INFO, "Running tests for auto-expiration");
            ArrayList<FileItem> files2 = FileItem.fetchFilesForAutoExpiration(ds, conf.getMonthsFileAutoExpiration());
            for (FileItem file : files2) {
                logger.log(Level.INFO, "Activating auto-expiration on file " + file.getName() + " (fid:" + file.getFid() + ")");
                long millis = (long) conf.getDaysFileExpiration() * 1000 * 60 * 60 * 24;
                file.setDateExpiration(new Timestamp(System.currentTimeMillis() + millis));
                file.update(ds, "vacuum");

                try {
                    SendAutoExpirationInformation(file, conf);
                } catch (MessagingException e) {
                    logger.log(Level.WARNING, "Unable to send notification email: {0}", e.toString());
                } catch (Exception e) {
                    logger.log(Level.INFO, "Unable to find user to notify about auto-expiration. {0}", e.toString());
                }
            }
        }

        // Clean out old log entries
        // except upload/download logs where the files still exist on the server
        try {
            dbConn = ds.getConnection();
            PreparedStatement st = dbConn.prepareStatement("DELETE FROM Logs WHERE `date` < ( now() - INTERVAL ? DAY ) AND fid NOT IN (SELECT fid FROM FileItems)");
            st.setInt(1, conf.getDaysLogRetention());
            st.setString(2, "download");
            st.setString(3, "upload");
            st.setString(4, "file edit");
            int i = st.executeUpdate();

            if (i > 0) {
                logger.log(Level.INFO, "Vacuuming {0} log entries", Integer.toString(i));
            }
            st.close();
            dbConn.close();
        } catch (SQLException e) {
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    private void SendAutoExpirationInformation(FileItem file, Conf conf)
        throws MessagingException, NoSuchUserException, SQLException {
        UserItem owner = new UserItem();
        owner.fetch(ds, file.getUid());
        logger.log(Level.INFO, "Sending email notification to {0} about this auto-expiration", owner.getEmail());

        Properties props = System.getProperties();
        props.put("mail.smtp.host", conf.getSmtpServer());
        props.put("mail.smtp.port", ((Integer) conf.getSmtpServerPort()).toString());
        props.put("mail.smtp.reportsuccess", "true");

        Session session = Session.getInstance(props, null);

        MimeMessage msg = new MimeMessage(session);
        // msg.setSender();
        
        InternetAddress emailRecipient = new InternetAddress(owner.getEmail());

        msg.setRecipient(Message.RecipientType.TO, emailRecipient);
        msg.setFrom(conf.getSmtpSender());
        msg.setSentDate(new Date());

        msg.setSubject("File auto-expiration");

        MimeMultipart mp = new MimeMultipart();
        mp.setSubType("alternative");

        String urlFileView = conf.getBaseUrl() + "/file/view/" + file.getFid() + "?md5=" + file.getMd5sum();
        String urlFileEdit = conf.getBaseUrl() + "/file/edit/" + file.getFid();
        String urlFileLog = conf.getBaseUrl() + "/file/log/" + file.getFid();

        String txtBody = 
            "This is an automated message from the " + conf.getBrandingOrg() + " jfileshare system.\n\n"
            + "The following file in your account: \n"
            + file.getName() + " (" + FileItem.humanReadable(file.getSize()) + ")\n"
            + "hasn't been downloaded or edited in more than " + conf.getMonthsFileAutoExpiration() + " months.\n"
            + "It has therefore been marked for automatic deletion in " + conf.getDaysFileExpiration() + " days.\n"
            + "\n"
            + "If you don't want this file to be deleted, you need to reenable its \"permanent\" status from the web interface:\n"
            + urlFileEdit + "\n"
            + "\n"
            + "The download logs for this file are available here:\n"
            + urlFileLog + "\n"
            + "--\n"
            + "Best regards\n"
            + "The " + conf.getBrandingOrg() + " jfileshare system";
        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText(txtBody);

        String htmlBody = 
            "<p>This is an automated message from the " + conf.getBrandingOrg() + " jfileshare system.</p>\n"
            + "<p>The following file in your account:<br />"
            + "<a href=\"" + urlFileView + "\">" + file.getName() + "</a> (" + FileItem.humanReadable(file.getSize()) + ")</br>\n"
            + "hasn't been downloaded or edited in more than " + conf.getMonthsFileAutoExpiration() + " months. \n"
            + "It has therefore been marked for automatic deletion in " + conf.getDaysFileExpiration() + " days.</p>\n"
            + "\n"
            + "<p>If you don't want this file to be deleted, you need to reenable its \"permanent\" status from the web interface:<br />\n"
            + "<a href=\"" + urlFileEdit + "\">" + urlFileEdit + "</a></p>\n"
            + "\n"
            + "<p>The download logs for this file are available here:<br />\n"
            + "<a href=\"" + urlFileLog + "\">" + urlFileLog + "</a></p>\n"
            + "<hr />\n"
            + "<p>Best regards<br />\n"
            + "The " + conf.getBrandingOrg() + " jfileshare system</p>\n";
        MimeBodyPart mbp2 = new MimeBodyPart();
        mbp2.setContent(htmlBody, "text/html");

        mp.addBodyPart(mbp1);
        mp.addBodyPart(mbp2);
        msg.setContent(mp);

        Transport.send(msg);

    }

}

