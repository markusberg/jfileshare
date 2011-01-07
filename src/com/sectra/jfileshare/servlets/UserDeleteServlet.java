package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.objects.NoSuchUserException;
import com.sectra.jfileshare.utils.Helpers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.servlet.RequestDispatcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;

import javax.sql.DataSource;

public class UserDeleteServlet extends HttpServlet {

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(UserDeleteServlet.class.getName());
    private String PATH_FILE_STORE;

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
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        ServletContext app = getServletContext();
        RequestDispatcher disp;

        try {
            HttpSession session = req.getSession();
            UserItem currentUser = (UserItem) session.getAttribute("user");
            UserItem user;
            String reqUid = req.getPathInfo();
            if (reqUid == null || reqUid.equals("/")) {
                user = currentUser;
            } else {
                Integer uid = Integer.parseInt(reqUid.substring(1));
                user = new UserItem(ds, uid);
            }
            if (!currentUser.hasEditAccessTo(user)) {
                req.setAttribute("message_critical", "You do not have access to delete that user");
                disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
            } else {
                req.setAttribute("user", user);
                req.setAttribute("tab", "Delete user");
                disp = app.getRequestDispatcher("/templates/UserDelete.jsp");
            }
        } catch (NoSuchUserException e) {
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
        if (req.getParameter("action") != null
                && req.getParameter("action").equals("login")) {
            doGet(req, resp);
        } else {
            ServletContext app = getServletContext();
            RequestDispatcher disp;

            HttpSession session = req.getSession();
            UserItem CurrentUser = (UserItem) session.getAttribute("user");
            Integer iUid = Integer.parseInt(req.getPathInfo().substring(1));
            try {
                UserItem User = new UserItem(ds, iUid);
                if (!CurrentUser.hasEditAccessTo(User)) {
                    req.setAttribute("message_critical", "You do not have access to modify user " + User.getUserInfo());
                    disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
                } else {
                    User.delete(ds, PATH_FILE_STORE);
                    req.setAttribute("message", "User " + User.getUserInfo() + " deleted");
                    req.setAttribute("tab", "Delete user");
                    disp = app.getRequestDispatcher("/templates/Blank.jsp");
                }
            } catch (NoSuchUserException e) {
                req.setAttribute("message_warning", e.getMessage());
                disp = app.getRequestDispatcher("/templates/404.jsp");
            } catch (SQLException e) {
                req.setAttribute("message_critical", e.getMessage());
                disp = app.getRequestDispatcher("/templates/Error.jsp");
            }
            disp.forward(req, resp);
        }
    }
}
