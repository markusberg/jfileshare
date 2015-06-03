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
 * @version     1.7
 * @since       2012-03-14
 */
package nu.kelvin.jfileshare.servlets;

import nu.kelvin.jfileshare.objects.Conf;
import nu.kelvin.jfileshare.objects.FileItem;
import nu.kelvin.jfileshare.objects.UserItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Timer;
import java.util.TimerTask;

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

        // Auto-expiration of files that haven't been downloaded or edited
        if (conf.getMonthsFileAutoExpiration() != 0) {
            // logger.log(Level.INFO, "Running tests for auto-expiration");
            ArrayList<FileItem> files2 = FileItem.fetchFilesForAutoExpiration(ds, conf.getMonthsFileAutoExpiration());
            for (FileItem file : files2) {
                logger.log(Level.INFO, "Activating auto-expiration on file " + file.getName() + " (fid:" + file.getFid() + ")");
                long millis = (long) conf.getDaysFileExpiration() * 1000 * 60 * 60 * 24;
                file.setDateExpiration(new Timestamp(System.currentTimeMillis() + millis));
                file.update(ds, "vacuum");

                try {
                    UserItem owner = new UserItem();
                    owner.fetch(ds, file.getUid());
                    logger.log(Level.INFO, "Sending email notification to {0} about this auto-expiration", owner.getEmail());
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
}

