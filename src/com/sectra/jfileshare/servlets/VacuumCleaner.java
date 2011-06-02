package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.Conf;
import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.objects.UserItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

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

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(VacuumCleaner.class.getName());
    private long VACUUM_INTERVAL = 1000 * 60 * 10;
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
        Conf conf = (Conf) getServletContext().getAttribute("conf");
        if (conf == null) {
            conf = new Conf(ds);
        }

        // Delete expired users
        ArrayList<UserItem> users = (ArrayList<UserItem>) getExpiredUsers(ds);
        if (!users.isEmpty()) {
            logger.log(Level.INFO, "Vacuuming {0} expired user(s) from the database", users.size());
        }
        for (UserItem user : users) {
            user.delete(ds, conf.getPathStore(), "vacuum");
        }

        // Delete expired files
        ArrayList<FileItem> files = (ArrayList<FileItem>) getExpiredFiles(ds);
        if (!files.isEmpty()) {
            logger.log(Level.INFO, "Vacuuming {0} expired file(s) from the database", files.size());
        }
        for (FileItem file : files) {
            file.delete(ds, conf.getPathStore(), "vacuum");
        }

        // Delete password requests older than 2 days
        try {
            Connection dbConn = ds.getConnection();
            Statement st = dbConn.createStatement();
            int i = st.executeUpdate("DELETE FROM PasswordReset where dateRequest < ( now() - INTERVAL 2 DAY )");

            if (i > 0) {
                logger.log(Level.INFO, "Vacuuming {0} entries from password reset table", Integer.toString(i));
            }
            st.close();
            dbConn.close();
        } catch (SQLException e) {
        }

        // Clean out old log entries
        // except file download logs where the files still exist on the server
        try {
            Connection dbConn = ds.getConnection();
            PreparedStatement st = dbConn.prepareStatement("DELETE FROM Logs where date < ( now() - INTERVAL ? DAY ) and (`action`!=? or id not in (select fid from FileItems))");
            st.setInt(1, conf.getDaysLogRetention());
            st.setString(2, "download");
            int i = st.executeUpdate();

            if (i > 0) {
                logger.log(Level.INFO, "Vacuuming {0} log entries", Integer.toString(i));
            }
            st.close();
            dbConn.close();
        } catch (SQLException e) {
        }
}

    private ArrayList<UserItem> getExpiredUsers(DataSource ds) {
        ArrayList<UserItem> users = new ArrayList<UserItem>();
        try {
            Connection dbConn = ds.getConnection();
            PreparedStatement st = dbConn.prepareStatement("select * from UserItems where dateExpiration<now() order by uid");
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                UserItem user = new UserItem();
                user.setUid(rs.getInt("UserItems.uid"));
                user.setUsername(rs.getString("UserItems.username"));
                user.setPwHash(rs.getString("UserItems.pwHash"));
                user.setEmail(rs.getString("UserItems.email"));
                user.setUserType(rs.getInt("UserItems.usertype"));
                user.setDateCreation(rs.getTimestamp("UserItems.dateCreation"));
                user.setDateLastLogin(rs.getTimestamp("UserItems.dateLastLogin"));
                user.setDateExpiration(rs.getTimestamp("UserItems.dateExpiration"));
                users.add(user);
            }

            st.close();
            dbConn.close();
        } catch (SQLException e) {
            logger.warning(e.toString());
        }
        return users;
    }

    private ArrayList<FileItem> getExpiredFiles(DataSource ds) {
        ArrayList<FileItem> files = new ArrayList<FileItem>();
        try {
            Connection dbConn = ds.getConnection();
            PreparedStatement st = dbConn.prepareStatement("select * from FileItems where dateExpiration<now() order by fid");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                FileItem file = new FileItem();
                file.setFid(rs.getInt("fid"));
                file.setName(rs.getString("name"));
                file.setType(rs.getString("type"));
                file.setSize(rs.getLong("size"));
                file.setMd5sum(rs.getString("md5sum"));
                file.setDownloads(rs.getInt("downloads"));
                file.setPwHash(rs.getString("pwHash"));
                file.setDateCreation(rs.getTimestamp("dateCreation"));
                file.setDateExpiration(rs.getTimestamp("dateExpiration"));
                file.setEnabled(rs.getBoolean("enabled"));
                files.add(file);
            }
            st.close();
            dbConn.close();
        } catch (SQLException e) {
            logger.warning(e.toString());
        }
        return files;
    }
}
