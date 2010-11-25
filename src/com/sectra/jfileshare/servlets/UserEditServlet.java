package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.utils.Helpers;

import java.io.IOException;

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

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(UserEditServlet.class.getName());
    private Integer DAYS_UNTIL_EXPIRATION;

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            ds = (DataSource) env.lookup("jdbc/jfileshare");
            DAYS_UNTIL_EXPIRATION = Integer.parseInt(getServletContext().getInitParameter("DAYS_USER_EXPIRATION").toString());
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
        UserItem currentUser = (UserItem) session.getAttribute("user");

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
            User = currentUser;
        }

        if (User.getUid() == null) {
            logger.info("Attempting to modify nonexistent user");
            req.setAttribute("message_warning", "No such user (" + Helpers.htmlSafe(iUid.toString()) + ")");
            jspForward = "/templates/404.jsp";
        } else if (!currentUser.hasEditAccessTo(User)) {
            req.setAttribute("message_critical", "You do not have access to edit user " + User.getUserInfo());
            jspForward = "/templates/AccessDenied.jsp";
        } else {
            req.setAttribute("oUser", User);
            req.setAttribute("tab", "Edit user");

            req.setAttribute("validatedUsername", User.getUsername());
            req.setAttribute("validatedEmail", User.getEmail());
            req.setAttribute("validatedPassword1", "");
            req.setAttribute("validatedPassword2", "");
            req.setAttribute("validatedBExpiration", User.getDateExpiration() == null ? false : true);
            req.setAttribute("validatedDaysUntilExpiration", User.getDateExpiration() == null ? DAYS_UNTIL_EXPIRATION : User.getDaysUntilExpiration());
            req.setAttribute("validatedUsertype", User.getUserType());

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
                req.setAttribute("oUser", User);

                // validate user input
                ArrayList<String> errors = new ArrayList<String>();

                // only admin is allowed to edit usernames
                String requestedUsername = User.getUsername();
                if (CurrentUser.isAdmin()) {
                    requestedUsername = req.getParameter("username");
                    if (!requestedUsername.equals(User.getUsername())) {
                        UserItem tempuser = new UserItem(ds, requestedUsername);
                        if (requestedUsername.length() < 2) {
                            errors.add("The username is too short");
                        } else if (tempuser.getUid() != null) {
                            errors.add("That username already exists");
                        }
                    }
                }
                req.setAttribute("validatedUsername", requestedUsername);

                // Validate email address
                String requestedEmail = (String) req.getParameter("email");
                errors.addAll(User.validateEmailAddress(requestedEmail));
                req.setAttribute("validatedEmail", requestedEmail);

                // Validate passwords
                String requestedPassword1 = req.getParameter("password1") == null ? "" : req.getParameter("password1");
                String requestedPassword2 = req.getParameter("password2") == null ? "" : req.getParameter("password2");
                if (!requestedPassword1.equals("")) {
                    errors.addAll(User.validatePassword(requestedPassword1, requestedPassword2));
                }
                req.setAttribute("validatedPassword1", requestedPassword1);
                req.setAttribute("validatedPassword2", requestedPassword2);

                // Validate expiration
                Boolean requestedBExpiration = User.getDateExpiration() == null ? false : true;
                Integer requestedDaysUntilExpiration = DAYS_UNTIL_EXPIRATION;
                if (CurrentUser.isAdmin()
                        || CurrentUser.getUid() == User.getUidCreator()) {
                    if (req.getParameter("bExpiration") != null
                            && req.getParameter("bExpiration").equals("true")) {
                        requestedBExpiration = true;
                    } else {
                        requestedBExpiration = false;
                    }
                    requestedDaysUntilExpiration = Integer.parseInt(req.getParameter("daysUntilExpiration"));
                    if (requestedDaysUntilExpiration < 1) {
                        requestedDaysUntilExpiration = DAYS_UNTIL_EXPIRATION;
                    } else if (requestedDaysUntilExpiration > 365) {
                        requestedDaysUntilExpiration = 365;
                    }
                }
                req.setAttribute("validatedBExpiration", requestedBExpiration);
                req.setAttribute("validatedDaysUntilExpiration", requestedDaysUntilExpiration);

                Integer requestedUsertype = User.getUserType();
                if (CurrentUser.isAdmin()) {
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

                    User.setUsername(requestedUsername);
                    User.setEmail(requestedEmail);
                    if (requestedBExpiration) {
                        User.setDaysUntilExpiration(requestedDaysUntilExpiration);
                    } else {
                        User.setDateExpiration(null);
                    }
                    User.setUserType(requestedUsertype);

                    if (User.save(ds)) {
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
}
