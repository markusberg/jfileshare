/**
 *  Copyright 2011 SECTRA Imtec AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * @author      Markus Berg <markus.berg @ sectra.se>
 * @version     1.6
 * @since       2011-09-21
 */
package nu.kelvin.jfileshare.servlets;

import nu.kelvin.jfileshare.objects.Conf;
import nu.kelvin.jfileshare.objects.UserItem;
import nu.kelvin.jfileshare.objects.NoSuchUserException;
import nu.kelvin.jfileshare.utils.Helpers;

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
    static final long serialVersionUID = 1L;

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
            Conf conf = (Conf) app.getAttribute("conf");
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
        if (!currentUser.isValidCSRFToken(req.getParameter("CSRFToken"))) {
            doGet(req, resp);
            return;
        }

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
            UserItem user = new UserItem();
            if (username.equals("")) {
                errors.add("Username is empty");
            } else {
                try {
                    user.fetch(datasource, username);
                    errors.add("Username is already taken");
                } catch (NoSuchUserException ignored) {
                } catch (SQLException ignored) {
                }
            }
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
