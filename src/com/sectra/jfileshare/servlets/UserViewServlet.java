package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.NoSuchUserException;
import com.sectra.jfileshare.objects.UserItem;

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
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;

import javax.sql.DataSource;

public class UserViewServlet extends HttpServlet {

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(UserViewServlet.class.getName());

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
        UserItem currentUser = (UserItem) session.getAttribute("user");
        ServletContext app = getServletContext();
        RequestDispatcher disp;

        try {
            String reqUid = req.getPathInfo();
            Integer uid;
            if (reqUid == null || reqUid.equals("/")) {
                uid = currentUser.getUid();
            } else {
                try {
                    uid = Integer.parseInt(reqUid.substring(1));
                } catch (NumberFormatException n) {
                    throw new NoSuchUserException("Invalid uid");
                }
            }
            UserItem user = new UserItem(ds, uid);

            if (!currentUser.hasEditAccessTo(user)) {
                req.setAttribute("message_warning", "You are not authorized to view the details of this user.");
                disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
            } else {
                if (!currentUser.getUid().equals(uid)) {
                    req.setAttribute("tab", user.getUsername());
                }

                req.setAttribute("user", user);
                req.setAttribute("files", user.getFiles(ds));
                req.setAttribute("users", user.getChildren(ds));
                disp = app.getRequestDispatcher("/templates/UserView.jsp");
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
        doGet(req, resp);
    }
}
