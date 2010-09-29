package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.FileItem;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

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

public class FileViewServlet extends HttpServlet {

    private DataSource datasource;
    private static final Logger logger =
            Logger.getLogger(FileViewServlet.class.getName());

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            datasource = (DataSource) env.lookup("jdbc/jfileshare");
        } catch (NamingException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String PathInfo = req.getPathInfo().substring(1);
        String md5sum = PathInfo.split("_SECTRA_")[0];
        int iFid = Integer.parseInt(PathInfo.split("_SECTRA_")[1]);

        Connection dbConn = null;
        FileItem oFile = null;
        ServletContext app = getServletContext();
        RequestDispatcher disp;

        try {
            dbConn = datasource.getConnection();
            oFile = new FileItem(dbConn, iFid);

            if (oFile.getFid() != -1) {
                if (oFile.getMd5sum().equals(md5sum)) {
                    req.setAttribute("tab", "File");
                    disp = app.getRequestDispatcher("/templates/FileView.jsp");
                    req.setAttribute("oFile", oFile);
                } else {
                    disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
                    req.setAttribute("message_warning", "File exists, but requires complete address");
                }
            } else {
                logger.info("File not found");
                req.setAttribute("message_warning", "The requested file is not found");
                disp = app.getRequestDispatcher("/templates/404.jsp");
            }
        } catch (SQLException e) {
            req.setAttribute("message_critical", "Unable to connect to database. Please contact your system administrator.");
            req.setAttribute("tab", "Error");
            disp = app.getRequestDispatcher("/templates/Blank.jsp");
            logger.severe("Unable to connect to database " + e.toString());
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }
        }

        disp.forward(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
