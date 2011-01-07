package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.objects.NoSuchFileException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
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

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(FileViewServlet.class.getName());

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
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer fid = Integer.parseInt(req.getPathInfo().substring(1));
        String md5sum = req.getParameter("md5");

        logger.log(Level.INFO, "Access requested to file: {0}", fid);
        ServletContext app = getServletContext();
        RequestDispatcher disp;

        try {
            FileItem file = new FileItem(ds, fid);
            logger.log(Level.INFO, "Fetched file: {0}", file.getFid());

            if (file.getMd5sum().equals(md5sum)) {
                req.setAttribute("tab", "File");
                disp = app.getRequestDispatcher("/templates/FileView.jsp");
                req.setAttribute("file", file);
            } else {
                disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
                req.setAttribute("message_warning", "File exists, but requires complete address");
            }
        } catch (NoSuchFileException e) {
            req.setAttribute("message_warning", e.getMessage());
            disp = app.getRequestDispatcher("/templates/404.jsp");
        } catch (SQLException e) {
            req.setAttribute("message_critical", e.getMessage());
            disp = app.getRequestDispatcher("/templates/Error.jsp");
        }
        disp.forward(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
