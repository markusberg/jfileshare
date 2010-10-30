package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.utils.Helpers;

import java.io.IOException;

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
        String jspForward = "";
        HttpSession session = req.getSession();
        UserItem CurrentUser = (UserItem) session.getAttribute("user");

        UserItem User;
        Integer iUid = null;
        try {
            String sUid = req.getPathInfo().substring(1);
            if (sUid.equals("")) {
                throw new NullPointerException();
            }
            iUid = Integer.parseInt(sUid);
            User = new UserItem(ds, iUid);
        } catch (NullPointerException e) {
            User = CurrentUser;
        }

        if (User.getUid() == null) {
            logger.info("Attempting to delete nonexistent user");
            req.setAttribute("message_warning", "No such user (" + Helpers.htmlSafe(iUid.toString()) + ")");
            jspForward = "/templates/404.jsp";
        } else if (!CurrentUser.hasEditAccessTo(User)) {
            req.setAttribute("message_critical", "You do not have access to delete user " + User.getUserInfo());
            jspForward = "/templates/AccessDenied.jsp";
        } else {
            req.setAttribute("oUser", User);
            req.setAttribute("tab", "Delete user");
            jspForward = "/templates/UserDelete.jsp";
        }
        disp = app.getRequestDispatcher(jspForward);
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

            String jspForward = "";
            HttpSession session = req.getSession();
            UserItem CurrentUser = (UserItem) session.getAttribute("user");
            Integer iUid = Integer.parseInt(req.getPathInfo().substring(1));
            UserItem User = new UserItem(ds, iUid);

            if (User.getUid() == null) {
                logger.info("Attempting to modify nonexistent user");
                req.setAttribute("message_warning", "No such user (" + Helpers.htmlSafe(iUid.toString()) + ")");
                jspForward = "/templates/404.jsp";
            } else if (!CurrentUser.hasEditAccessTo(User)) {
                req.setAttribute("message_critical", "You do not have access to modify user " + User.getUserInfo());
                jspForward = "/templates/AccessDenied.jsp";
            } else {
                User.delete(ds, PATH_FILE_STORE);
                req.setAttribute("message", "User " + User.getUserInfo() + " deleted");
                req.setAttribute("tab", "Delete user");
                jspForward = "/templates/Blank.jsp";
            }
            disp = app.getRequestDispatcher(jspForward);
            disp.forward(req, resp);
        }
    }
}
