package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.objects.NoSuchUserException;
import com.sectra.jfileshare.utils.Helpers;

import java.io.IOException;
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
                req.setAttribute("message_critical", "You do not have access to edit that user");
                disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
            } else {
                req.setAttribute("user", user);
                req.setAttribute("tab", "Edit user");

                req.setAttribute("validatedUsername", user.getUsername());
                req.setAttribute("validatedEmail", user.getEmail());
                req.setAttribute("validatedPassword1", "");
                req.setAttribute("validatedPassword2", "");
                req.setAttribute("validatedBExpiration", user.getDateExpiration() == null ? false : true);
                req.setAttribute("validatedDaysUntilExpiration", user.getDateExpiration() == null ? DAYS_UNTIL_EXPIRATION : user.getDaysUntilExpiration());
                req.setAttribute("validatedUsertype", user.getUserType());

                disp = app.getRequestDispatcher("/templates/UserEdit.jsp");
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
            UserItem currentUser = (UserItem) session.getAttribute("user");
            Integer uid = Integer.parseInt(req.getPathInfo().substring(1));
            try {
                UserItem user = new UserItem(ds, uid);

                if (!currentUser.hasEditAccessTo(user)) {
                    req.setAttribute("message_critical", "You do not have access to modify user " + user.getUserInfo());
                    disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
                } else {
                    req.setAttribute("user", user);

                    // validate user input
                    ArrayList<String> errors = new ArrayList<String>();

                    // only admin is allowed to edit usernames
                    String requestedUsername = user.getUsername();
                    if (currentUser.isAdmin()) {
                        requestedUsername = req.getParameter("username");
                        if (!requestedUsername.equals(user.getUsername())) {
                            if (requestedUsername.length() < 2) {
                                errors.add("The username is too short");
                            } else {
                                try {
                                    UserItem tempuser = new UserItem(ds, requestedUsername);
                                    errors.add("The requested username already exists");
                                } catch (NoSuchUserException ignored) {
                                } catch (SQLException ignored) {
                                }
                            }
                        }
                    }
                    req.setAttribute("validatedUsername", requestedUsername);

                    // Validate email address
                    String requestedEmail = (String) req.getParameter("email");
                    errors.addAll(user.validateEmailAddress(requestedEmail));
                    req.setAttribute("validatedEmail", requestedEmail);

                    // Validate passwords
                    String requestedPassword1 = req.getParameter("password1") == null ? "" : req.getParameter("password1");
                    String requestedPassword2 = req.getParameter("password2") == null ? "" : req.getParameter("password2");
                    if (!requestedPassword1.equals("")) {
                        errors.addAll(user.validatePassword(requestedPassword1, requestedPassword2));
                    }
                    req.setAttribute("validatedPassword1", requestedPassword1);
                    req.setAttribute("validatedPassword2", requestedPassword2);

                    // Validate expiration
                    Boolean requestedBExpiration = user.getDateExpiration() == null ? false : true;
                    Integer requestedDaysUntilExpiration = DAYS_UNTIL_EXPIRATION;
                    if (currentUser.isAdmin()
                            || currentUser.isParentTo(user)) {
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

                    Integer requestedUsertype = user.getUserType();
                    if (currentUser.isAdmin()) {
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
                        user.setUsername(requestedUsername);
                        user.setEmail(requestedEmail);
                        if (requestedBExpiration) {
                            user.setDaysUntilExpiration(requestedDaysUntilExpiration);
                        } else {
                            user.setDateExpiration(null);
                        }
                        user.setUserType(requestedUsertype);

                        if (user.save(ds)) {
                            req.setAttribute("message", "Your changes have been saved");
                            req.setAttribute("validatedPassword1", "");
                            req.setAttribute("validatedPassword2", "");
                        } else {
                            req.setAttribute("message_critical", "Unable to save changes");
                        }
                    }
                    req.setAttribute("tab", "Edit user");
                    disp = app.getRequestDispatcher("/templates/UserEdit.jsp");
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
