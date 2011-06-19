package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.Conf;
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

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            datasource = (DataSource) env.lookup("jdbc/jfileshare");
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
            // Set the default values for a new user
            Conf conf = (Conf) getServletContext().getAttribute("conf");
            UserItem user = new UserItem();
            user.setDaysUntilExpiration(conf.getDaysUserExpiration());
            req.setAttribute("user", user);
            disp = app.getRequestDispatcher("/templates/UserAdd.jsp");
        }
        disp.forward(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if ("login".equals(req.getParameter("action"))) {
            doGet(req, resp);
            return;
        }

        HttpSession session = req.getSession();
        UserItem currentUser = (UserItem) session.getAttribute("user");
        RequestDispatcher disp;
        ServletContext app = getServletContext();
        Conf conf = (Conf) app.getAttribute("conf");

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
            user.setUsername(username);
            errors.addAll(user.validateEmailAddress(req.getParameter("email")));

            // Validate the amount of time that the account will be active
            if ("true".equals(req.getParameter("bExpiration"))) {
                Integer daysUserExpiration = conf.getDaysUserExpiration();
                if (req.getParameter("daysUserExpiration") != null) {
                    Integer requestedExpiration = Integer.parseInt(req.getParameter("daysUserExpiration"));
                    if (UserItem.DAY_MAP.containsKey(requestedExpiration)) {
                        daysUserExpiration = requestedExpiration;
                    }
                }
                user.setDaysUntilExpiration(daysUserExpiration);
            }

            // Validate passwords
            String password1 = req.getParameter("password1") == null ? "" : req.getParameter("password1");
            String password2 = req.getParameter("password2") == null ? "" : req.getParameter("password2");
            errors.addAll(user.validatePassword(password1, password2));
            req.setAttribute("password1", password1);
            req.setAttribute("password2", password2);

            // If currentUser is an admin, set the requested user type
            if (currentUser.isAdmin()) {
                int usertype = Integer.parseInt(req.getParameter("usertype"));
                if (usertype == UserItem.TYPE_ADMIN
                        || usertype == UserItem.TYPE_EXTERNAL
                        || usertype == UserItem.TYPE_INTERNAL) {
                    user.setUserType(usertype);
                }
            }

            // Only attempt to save the user if there are no errors
            if (!errors.isEmpty()) {
                String errormessage = "User creation failed due to the following " + (errors.size() == 1 ? "reason" : "reasons") + ":<ul>";
                for (String emsg : errors) {
                    errormessage = errormessage.concat("<li>" + emsg + "</li>\n");
                }
                errormessage = errormessage.concat("</ul>\n");
                req.setAttribute("message_critical", errormessage);
                req.setAttribute("user", user);
                disp = app.getRequestDispatcher("/templates/UserAdd.jsp");
            } else {
                // Set the creator and save the user
                user.setUidCreator(currentUser.getUid());

                if (user.create(datasource, req.getRemoteAddr())) {
                    req.setAttribute("message", "User <strong>\"" + Helpers.htmlSafe(user.getUsername()) + "\"</strong> created");
                    UserItem newUser = new UserItem();

                    // Seed the UserAdd form with a blank default user
                    req.setAttribute("password1", "");
                    req.setAttribute("password2", "");
                    newUser.setDaysUntilExpiration(conf.getDaysUserExpiration());
                    req.setAttribute("user", newUser);
                    disp = app.getRequestDispatcher("/templates/UserAdd.jsp");
                } else {
                    req.setAttribute("message_critical", "Unable to create user due to database error. Please try again, or contact the server administrator.");
                    disp = app.getRequestDispatcher("/templates/Error.jsp");
                }
            }
        }
        disp.forward(req, resp);
    }
}
