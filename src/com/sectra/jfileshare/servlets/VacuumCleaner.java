package com.sectra.jfileshare.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.sql.DataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.objects.UserItem;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.Timer;
import java.util.TimerTask;

public class VacuumCleaner extends HttpServlet {

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(VacuumCleaner.class.getName());
    private String PATH_FILE_STORE;
    private long VACUUM_INTERVAL = 1000 * 60 * 10;
    private Timer timer = null;

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            ds = (DataSource) env.lookup("jdbc/jfileshare");
            PATH_FILE_STORE = getServletContext().getInitParameter("PATH_STORE").toString();
        } catch (NamingException e) {
            throw new ServletException(e);
        }
        // vacuum();

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

        // Delete expired users
        ArrayList<UserItem> aUsers = (ArrayList<UserItem>) getExpiredUsers(ds);
        if (aUsers.size() > 0) {
            logger.info("Vacuuming " + aUsers.size() + " expired user(s) from the database");
        }
        for (UserItem user : aUsers) {
            user.delete(ds, PATH_FILE_STORE);
        }

        // Delete expired files
        ArrayList<FileItem> aFiles = (ArrayList<FileItem>) getExpiredFiles(ds);
        if (aFiles.size() > 0) {
            logger.info("Vacuuming " + aFiles.size() + " expired file(s) from the database");
        }
        for (FileItem oFile : aFiles) {
            oFile.delete(ds, PATH_FILE_STORE);
        }

        // Delete password requests older than 2 days
        try {
            Connection dbConn = ds.getConnection();
            Statement st = dbConn.createStatement();
            int i = st.executeUpdate("DELETE FROM PasswordReset where dateRequest < ( now() - INTERVAL 2 DAY )");

            if (i > 0) {
                logger.info("Vacuuming " + Integer.toString(i) + " entries from password reset table");
            }
            st.close();
            dbConn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<UserItem> getExpiredUsers(DataSource ds) {
        ArrayList<UserItem> aUsers = new ArrayList<UserItem>();
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
                aUsers.add(user);
            }

            st.close();
            dbConn.close();
        } catch (SQLException e) {
            logger.warning(e.toString());
        }
        return aUsers;
    }

    private ArrayList<FileItem> getExpiredFiles(DataSource ds) {
        ArrayList<FileItem> aFiles = new ArrayList<FileItem>();
        try {
            Connection dbConn = ds.getConnection();
            PreparedStatement st = dbConn.prepareStatement("select * from FileItems where dateExpiration<now() order by fid");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                FileItem file = new FileItem();
                file.setFid(rs.getInt("fid"));
                file.setName(rs.getString("name"));
                file.setType(rs.getString("type"));
                file.setSize(rs.getDouble("size"));
                file.setMd5sum(rs.getString("md5sum"));
                file.setDownloads(rs.getInt("downloads"));
                file.setPwHash(rs.getString("pwHash"));
                file.setDateCreation(rs.getTimestamp("dateCreation"));
                file.setDateExpiration(rs.getTimestamp("dateExpiration"));
                file.setEnabled(rs.getBoolean("enabled"));
                aFiles.add(file);
            }
            st.close();
            dbConn.close();
        } catch (SQLException e) {
            logger.warning(e.toString());
        }
        return aFiles;
    }
}
