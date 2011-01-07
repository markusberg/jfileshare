package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.objects.NoSuchUserException;
import com.sectra.jfileshare.utils.Helpers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
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
    private Integer DAYS_USER_EXPIRATION;

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            datasource = (DataSource) env.lookup("jdbc/jfileshare");
            ServletContext context = getServletContext();
            DAYS_USER_EXPIRATION = Integer.parseInt(context.getInitParameter("DAYS_USER_EXPIRATION").toString());

        } catch (NamingException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        UserItem currentUser = (UserItem) session.getAttribute("user");
        RequestDispatcher disp;
        ServletContext app = getServletContext();

        if (currentUser.isExternal()) {
            logger.log(Level.INFO, "{0} has insufficient access to create users", currentUser.getUserInfo());
            req.setAttribute("message_warning", "You do not have access to create users");
            disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
        } else {
            // Set the default values for new users
            req.setAttribute("validatedUsername", "");
            req.setAttribute("validatedEmail", "");
            req.setAttribute("validatedPassword1", "");
            req.setAttribute("validatedPassword2", "");
            req.setAttribute("validatedBExpiration", true);
            req.setAttribute("validatedDaysUntilExpiration", DAYS_USER_EXPIRATION);
            req.setAttribute("validatedUsertype", UserItem.TYPE_EXTERNAL);

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
            UserItem currentUser = (UserItem) session.getAttribute("user");
            RequestDispatcher disp;
            ServletContext app = getServletContext();

            if (currentUser.isExternal()) {
                logger.log(Level.INFO, "{0} has insufficient access to create users", currentUser.getUserInfo());
                req.setAttribute("message_warning", "You do not have access to create users");
                disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
            } else {
                ArrayList<String> errors = new ArrayList<String>();

                // Check username uniqueness
                String username = req.getParameter("username") == null ? "" : req.getParameter("username");
                UserItem user = null;
                if (username.equals("")) {
                    errors.add("Username is empty");
                } else {
                    try {
                        user = new UserItem(datasource, username);
                        errors.add("Username is already taken");
                    } catch (NoSuchUserException ignored) {
                    } catch (SQLException ignored) {
                    }
                }
                user = new UserItem();
                req.setAttribute("validatedUsername", username);

                // Validate email address
                errors.addAll(user.validateEmailAddress(req.getParameter("email")));
                req.setAttribute("validatedEmail", req.getParameter("email"));

                // Validate the amount of time account will be active
                Integer daysUntilExpiration = this.DAYS_USER_EXPIRATION;
                if (req.getParameter("daysUserExpiration") != null) {
                    Integer requestedExpiration = Integer.parseInt(req.getParameter("daysUserExpiration"));
                    if (UserItem.DAY_MAP.containsKey(requestedExpiration)) {
                        daysUntilExpiration = requestedExpiration;
                    }
                }
                req.setAttribute("validatedDaysUntilExpiration", daysUntilExpiration);

                // See if the expiration-box is checked
                if (req.getParameter("bExpiration") != null
                        && req.getParameter("bExpiration").equals("true")) {
                    req.setAttribute("validatedBExpiration", true);
                    user.setDaysUntilExpiration(daysUntilExpiration);
                } else {
                    req.setAttribute("validatedBExpiration", false);
                }

                // Validate passwords
                String password1 = req.getParameter("password1") == null ? "" : req.getParameter("password1");
                String password2 = req.getParameter("password2") == null ? "" : req.getParameter("password2");
                errors.addAll(user.validatePassword(password1, password2));
                req.setAttribute("validatedPassword1", password1);
                req.setAttribute("validatedPassword2", password2);

                // If currentUser is an admin, set the requested user type
                int usertype = UserItem.TYPE_EXTERNAL;
                if (currentUser.isAdmin()) {
                    int reqUsertype = Integer.parseInt(req.getParameter("usertype"));
                    if (reqUsertype == UserItem.TYPE_ADMIN
                            || reqUsertype == UserItem.TYPE_EXTERNAL
                            || reqUsertype == UserItem.TYPE_INTERNAL) {
                        user.setUserType(usertype);
                        usertype = reqUsertype;
                    }
                }
                req.setAttribute("validatedUsertype", usertype);


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
                    user.setUidCreator(currentUser.getUid());
                    user.setUsername(username);

                    if (user.save(datasource)) {
                        req.setAttribute("message", "User \"" + Helpers.htmlSafe(user.getUsername()) + "\" created");
                        disp = app.getRequestDispatcher("/templates/UserAdd.jsp");

                        // Set the default values for new users
                        req.setAttribute("validatedUsername", "");
                        req.setAttribute("validatedEmail", "");
                        req.setAttribute("validatedPassword1", "");
                        req.setAttribute("validatedPassword2", "");
                        req.setAttribute("validatedBExpiration", true);
                        req.setAttribute("validatedDaysUntilExpiration", DAYS_USER_EXPIRATION);
                        req.setAttribute("validatedUsertype", UserItem.TYPE_EXTERNAL);

                    } else {
                        req.setAttribute("message_critical", "Unable to create user due to database error. Please try again, or contact the server administrator.");
                        disp = app.getRequestDispatcher("/templates/Error.jsp");
                    }
                }
            }
            disp.forward(req, resp);
        }
    }
}
