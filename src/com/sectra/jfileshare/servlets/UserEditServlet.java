package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.utils.Helpers;

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

import javax.servlet.RequestDispatcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;

import javax.sql.DataSource;

public class UserEditServlet extends HttpServlet {

    private DataSource datasource;
    private static final Logger logger =
            Logger.getLogger(UserEditServlet.class.getName());
    private String pathFileStore;
    private Integer defaultDaysUntilExpiration;

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            datasource = (DataSource) env.lookup("jdbc/jfileshare");
            ServletContext context = getServletContext();
            pathFileStore = context.getInitParameter("PATH_STORE").toString();
            defaultDaysUntilExpiration = Integer.parseInt(context.getInitParameter("DAYS_USER_EXPIRATION").toString());

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
        UserItem oCurrentUser = (UserItem) session.getAttribute("user");

        UserItem oUser;
        Integer iUid = null;
        try {
            String sUid = req.getPathInfo().substring(1);
            if (sUid.equals("")) {
                throw new NullPointerException();
            }
            iUid = Integer.parseInt(sUid);
            oUser = getUser(iUid);
        } catch (NullPointerException e) {
            oUser = oCurrentUser;
        }

        if (oUser.getUid() == -1) {
            logger.info("Attempting to modify nonexistent user");
            req.setAttribute("message_warning", "No such user (" + Helpers.htmlSafe(iUid.toString()) + ")");
            jspForward = "/templates/404.jsp";
        } else if (oUser.getUid() != oCurrentUser.getUid()
                && oUser.getCreatorUid() != oCurrentUser.getUid()
                && !oCurrentUser.isAdmin()) {
            logger.info(oCurrentUser.getUserInfo() + " has insufficient access to modify user " + oUser.getUserInfo());
            req.setAttribute("message_critical", "You do not have access to modify user " + oUser.getUserInfo());
            jspForward = "/templates/AccessDenied.jsp";
        } else if (req.getServletPath().equals("/user/delete")) {
            req.setAttribute("oUser", oUser);
            req.setAttribute("tab", "Delete user");
            jspForward = "/templates/UserDelete.jsp";
        } else {
            req.setAttribute("oUser", oUser);
            req.setAttribute("tab", "Edit user");

            req.setAttribute("validatedUsername", oUser.getUsername());
            req.setAttribute("validatedEmail", oUser.getEmail());
            req.setAttribute("validatedPassword1", "");
            req.setAttribute("validatedPassword2", "");
            req.setAttribute("validatedBExpiration", oUser.getDateExpiration() == null ? false : true);
            req.setAttribute("validatedDaysUntilExpiration", oUser.getDateExpiration() == null ? defaultDaysUntilExpiration : oUser.getDaysUntilExpiration());
            req.setAttribute("validatedUsertype", oUser.getUserType());

            jspForward = "/templates/UserEdit.jsp";
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
            UserItem oCurrentUser = (UserItem) session.getAttribute("user");

            Integer iUid = Integer.parseInt(req.getPathInfo().substring(1));

            UserItem oUser = getUser(iUid);

            if (oUser.getUid() == -1) {
                logger.info("Attempting to modify nonexistent user");
                req.setAttribute("message_warning", "No such user (" + Helpers.htmlSafe(iUid.toString()) + ")");
                jspForward = "/templates/404.jsp";
            } else if (oUser.getUid() != oCurrentUser.getUid()
                    && oUser.getCreatorUid() != oCurrentUser.getUid()
                    && !oCurrentUser.isAdmin()) {
                logger.info("Insufficient access to modify user");
                req.setAttribute("message_critical", "You do not have access to modify user " + oUser.getUserInfo());
                jspForward = "/templates/AccessDenied.jsp";
            } else if (req.getServletPath().equals("/user/delete")) {
                Connection dbConn = null;
                try {
                    dbConn = datasource.getConnection();
                    oUser.delete(dbConn, pathFileStore);
                } catch (SQLException e) {
                    logger.severe("Unable to connect to database: " + e.toString());
                } finally {
                    if (dbConn != null) {
                        try {
                            dbConn.close();
                        } catch (SQLException e) {
                        }
                    }
                }

                req.setAttribute("message", "User " + oUser.getUserInfo() + " deleted");
                req.setAttribute("tab", "Delete user");
                jspForward = "/templates/Blank.jsp";

            } else {
                logger.info("Editing user");
                req.setAttribute("oUser", oUser);

                // validate user input
                ArrayList<String> errors = new ArrayList<String>();

                // only admin is allowed to edit usernames
                String requestedUsername = oUser.getUsername();
                if (oCurrentUser.isAdmin()) {
                    requestedUsername = (String) req.getParameter("username");
                    if (!requestedUsername.equals(oUser.getUsername())) {
                        UserItem tempuser = (UserItem) getUser(requestedUsername);
                        if (requestedUsername.length() < 2) {
                            errors.add("The username is too short");
                        } else if (tempuser.getUid() != -1) {
                            errors.add("That username already exists");
                        }
                    }
                }
                req.setAttribute("validatedUsername", requestedUsername);

                // Validate email address
                String requestedEmail = (String) req.getParameter("email");
                errors.addAll(oUser.validateEmailAddress(requestedEmail));
                req.setAttribute("validatedEmail", requestedEmail);

                // Validate passwords
                String requestedPassword1 = req.getParameter("password1") == null ? "" : req.getParameter("password1");
                String requestedPassword2 = req.getParameter("password2") == null ? "" : req.getParameter("password2");
                if (!requestedPassword1.equals("")) {
                    errors.addAll(oUser.validatePassword(requestedPassword1, requestedPassword2));
                }
                req.setAttribute("validatedPassword1", requestedPassword1);
                req.setAttribute("validatedPassword2", requestedPassword2);

                // Validate expiration
                Boolean requestedBExpiration = oUser.getDateExpiration() == null ? false : true;
                Integer requestedDaysUntilExpiration = defaultDaysUntilExpiration;
                if (oCurrentUser.isAdmin()
                        || oCurrentUser.getUid() == oUser.getCreatorUid()) {
                    if (req.getParameter("bExpiration") != null
                            && req.getParameter("bExpiration").equals("true")) {
                        requestedBExpiration = true;
                    } else {
                        requestedBExpiration = false;
                    }
                    requestedDaysUntilExpiration = Integer.parseInt(req.getParameter("daysUntilExpiration"));
                    if (requestedDaysUntilExpiration < 1 || requestedDaysUntilExpiration > 365) {
                        requestedDaysUntilExpiration = defaultDaysUntilExpiration;
                    }
                }
                req.setAttribute("validatedBExpiration", requestedBExpiration);
                req.setAttribute("validatedDaysUntilExpiration", requestedDaysUntilExpiration);

                Integer requestedUsertype = oUser.getUserType();
                if (oCurrentUser.isAdmin()) {
                    requestedUsertype = Integer.parseInt((String) req.getParameter("usertype"));
                }
                req.setAttribute("validatedUsertype", requestedUsertype);

                if (errors.size() > 0) {
                    String errormessage = "User edit failed due to the following " + (errors.size() == 1 ? "reason" : "reasons") + ":<ul>";
                    for (String emsg : errors) {
                        errormessage = errormessage.concat("<li>" + emsg + "</li>\n");
                    }
                    errormessage = errormessage.concat("</ul>\n");
                    req.setAttribute("message_critical", errormessage);
                } else {
                    // Set the parameters and save the user
                    
                    oUser.setUsername(requestedUsername);
                    oUser.setEmail(requestedEmail);
                    if (requestedBExpiration) {
                        oUser.setDaysUntilExpiration(requestedDaysUntilExpiration);
                    } else {
                        oUser.setDateExpiration(null);
                    }
                    oUser.setUserType(requestedUsertype);

                    if (oUser.save(datasource)) {
                        req.setAttribute("message", "Your changes have been saved");
                        req.setAttribute("validatedPassword1", "");
                        req.setAttribute("validatedPassword2", "");

                    } else {
                        req.setAttribute("message_critical", "Unable to save changes");
                    }
                }

                req.setAttribute("tab", "Edit user");
                jspForward = "/templates/UserEdit.jsp";
            }


            disp = app.getRequestDispatcher(jspForward);
            disp.forward(req, resp);
        }
    }

    private UserItem getUser(int iUid) {
        UserItem oUser = null;
        Connection dbConn = null;
        try {
            dbConn = datasource.getConnection();
            oUser = new UserItem(dbConn, iUid);
        } catch (SQLException e) {
            logger.severe("Unable to connect to database: " + e.toString());
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }
        }
        return oUser;
    }

    private UserItem getUser(String username) {
        UserItem oUser = null;
        Connection dbConn = null;
        try {
            dbConn = datasource.getConnection();
            oUser = new UserItem(dbConn, username);
        } catch (SQLException e) {
            logger.severe("Unable to connect to database: " + e.toString());
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }
        }
        return oUser;
    }
}
