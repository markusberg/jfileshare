package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.UserItem;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.logging.Logger;
import java.util.ArrayList;

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

public class UserAddServlet extends HttpServlet {

    private DataSource datasource;
    private static final Logger logger =
            Logger.getLogger(UserAddServlet.class.getName());
    private Integer daysUserExpiration;

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            datasource = (DataSource) env.lookup("jdbc/jfileshare");
            ServletContext context = getServletContext();
            daysUserExpiration = Integer.parseInt(context.getInitParameter("DAYS_USER_EXPIRATION").toString());

        } catch (NamingException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        UserItem oCurrentUser = (UserItem) session.getAttribute("user");
        RequestDispatcher disp;
        ServletContext app = getServletContext();

        if (oCurrentUser.isExternal()) {
            logger.info(oCurrentUser.getUserInfo() + " has insufficient access to create users");
            req.setAttribute("message_warning", "You do not have access to create users");
            disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
        } else {
            disp = app.getRequestDispatcher("/templates/UserAdd.jsp");
        }
        disp.forward(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (req.getParameter("action") != null
                && req.getParameter("action").equals("login")) {
            // This post is the result of a login
            doGet(req, resp);
        } else if (req.getParameter("action") != null
                && req.getParameter("action").equals("adduser")) {
            HttpSession session = req.getSession();
            UserItem oCurrentUser = (UserItem) session.getAttribute("user");
            RequestDispatcher disp;
            ServletContext app = getServletContext();

            if (oCurrentUser.isExternal()) {
                logger.info(oCurrentUser.getUserInfo() + " has insufficient access to create users");
                req.setAttribute("message_warning", "You do not have access to create users");
                disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
            } else {
                Connection dbConn = null;
                try {
                    dbConn = datasource.getConnection();

                    ArrayList<String> errors = new ArrayList<String>();

                    // Check username uniqueness
                    String username = req.getParameter("username") == null ? "" : req.getParameter("username");
                    UserItem oUser = new UserItem(dbConn, username);
                    errors.addAll(oUser.validateUserName(username));

                    // Validate email address
                    errors.addAll(oUser.validateEmailAddress(req.getParameter("email")));

                    // Validate the amount of time account will be active
                    Integer daysUntilExpiration = this.daysUserExpiration;
                    if (req.getParameter("daysUserExpiration") != null) {
                        Integer requestedExpiration = Integer.parseInt(req.getParameter("daysUserExpiration"));
                        if (UserItem.dayMap.containsKey(requestedExpiration)) {
                            daysUntilExpiration = requestedExpiration;
                        }
                    }
                    req.setAttribute("daysUntilExpiration", daysUntilExpiration);

                    // See if the expiration-box is checked
                    if (req.getParameter("bExpiration") != null
                            && req.getParameter("bExpiration").equals("true")) {
                        req.setAttribute("bExpiration", "true");
                        oUser.setDaysUntilExpiration(daysUntilExpiration);
                    } else {
                        req.setAttribute("bExpiration", "false");
                    }

                    // Validate passwords
                    String password1 = req.getParameter("password1") == null ? "" : req.getParameter("password1");
                    String password2 = req.getParameter("password2") == null ? "" : req.getParameter("password2");
                    errors.addAll(oUser.validatePassword(password1, password2));

                    // If oCurrentUser is an admin, set the requested user type
                    if (oCurrentUser.isAdmin()) {
                        int usertype = Integer.parseInt(req.getParameter("usertype"));
                        oUser.setUserType(usertype);
                    }

                    if (errors.size() > 0) {
                        String errormessage = "User creation failed due to the following " + (errors.size() == 1 ? "reason" : "reasons") + ":<ul>";
                        for (String emsg : errors) {
                            errormessage = errormessage.concat("<li>" + emsg + "</li>\n");
                        }
                        errormessage = errormessage.concat("</ul>\n");
                        req.setAttribute("message_critical", errormessage);
                        disp = app.getRequestDispatcher("/templates/UserAdd.jsp");
                    } else {
                        // Set the creator, username, and save the user
                        oUser.setCreatorUid(oCurrentUser.getUid());
                        oUser.setUsername(username);

                        oUser.save(dbConn);
                        req.setAttribute("message", "User created");
                        disp = app.getRequestDispatcher("/templates/Blank.jsp");
                    }
                } catch (SQLException e) {
                    req.setAttribute("message_critical", "Unable to connect to database. Please contact your system administrator.");
                    req.setAttribute("tab", "Error");
                    disp = app.getRequestDispatcher("/templates/blank.jsp");
                    logger.severe("Unable to connect to database " + e.toString());
                } finally {
                    if (dbConn != null) {
                        try {
                            dbConn.close();
                        } catch (SQLException e) {
                        }
                    }
                }
            }
            disp.forward(req, resp);
        }
    }
}
