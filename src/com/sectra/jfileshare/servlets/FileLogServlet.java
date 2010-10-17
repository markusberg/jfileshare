package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.objects.UserItem;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;

import javax.sql.DataSource;

import java.io.IOException;

import java.util.ArrayList;
import java.util.logging.Logger;

public class FileLogServlet extends HttpServlet {

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(FileLogServlet.class.getName());

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
        HttpSession session = req.getSession();
        UserItem oCurrentUser = (UserItem) session.getAttribute("user");
        String PathInfo = req.getPathInfo().substring(1);

        int iFid = Integer.parseInt(PathInfo);

        ServletContext app = getServletContext();
        RequestDispatcher disp = null;

        FileItem oFile = new FileItem(ds, iFid);

        if (oFile.getFid() == -2) {
            req.setAttribute("message_critical", "Unable to connect to database. Please contact your system administrator.");
            req.setAttribute("tab", "Error");
            disp = app.getRequestDispatcher("/templates/blank.jsp");
        } else if (oFile.getFid() == null) {
            logger.info("File not found");
            req.setAttribute("message_warning", "The requested file is not found");
            disp = app.getRequestDispatcher("/templates/404.jsp");
        } else if (!oCurrentUser.isAdmin()
                && oFile.getOwnerUid() != oCurrentUser.getUid()) {
            // Neither admin nor owner
            req.setAttribute("message_critical", "You don't have access to view the logs of this file");
            disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
        } else {
            req.setAttribute("oFile", oFile);
            ArrayList aDownloadLog = oFile.getLogs(ds);
            req.setAttribute("aDownloadLog", aDownloadLog);
            if (aDownloadLog.size() == 0) {
                req.setAttribute("message", "This file (" + oFile.getName() + ") has never been downloaded");
            }
            req.setAttribute("tab", "File log");
            disp = app.getRequestDispatcher("/templates/FileLog.jsp");
        }
        disp.forward(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
