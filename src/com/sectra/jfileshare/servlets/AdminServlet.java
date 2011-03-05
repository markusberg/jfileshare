package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.Conf;
import com.sectra.jfileshare.objects.UserItem;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.logging.Logger;

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

/**
 * @author  Markus Berg <markus.berg@sectra.se>
 * @version 2010-05-30
 * @since   1.5
 */
public class AdminServlet extends HttpServlet {

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(AdminServlet.class.getName());

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
        ServletContext app = getServletContext();
        RequestDispatcher disp;
        HttpSession session = req.getSession();
        UserItem User = (UserItem) session.getAttribute("user");
        Conf conf = (Conf) app.getAttribute("conf");

        if (User.isAdmin()) {
            req.setAttribute("conf", conf);
            disp = app.getRequestDispatcher("/templates/Admin.jsp");
        } else {
            req.setAttribute("message_critical", "Access Denied");
            disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
        }
        disp.forward(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (req.getParameter("action") != null
                && req.getParameter("action").equals("login")) {
            // This POST is the result of a login
            doGet(req, resp);
        } else {
            ServletContext app = getServletContext();
            RequestDispatcher disp;
            HttpSession session = req.getSession();
            UserItem user = (UserItem) session.getAttribute("user");
            Conf conf = (Conf) app.getAttribute("conf");

            if (user.isAdmin()) {
                req.setAttribute("conf", conf);
                disp = app.getRequestDispatcher("/templates/Admin.jsp");
            } else {
                req.setAttribute("message_critical", "Access Denied");
                disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
            }

            disp.forward(req, resp);


            /*
            ServletContext app = getServletContext();
            RequestDispatcher disp;

            Connection dbConn = null;
            try {
            dbConn = ds.getConnection();
            } catch (SQLException ignore) {
            } finally {
            if (dbConn != null) {
            try {
            dbConn.close();
            } catch (SQLException ignore) {
            }
            }
            }
             */
        }
    }
}
