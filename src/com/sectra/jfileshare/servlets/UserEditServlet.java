package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.Conf;
import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.objects.NoSuchUserException;

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
        ServletContext app = getServletContext();
        Conf conf = (Conf) app.getAttribute("conf");
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
        if ("login".equals(req.getParameter("action"))) {
            doGet(req, resp);
        } else {
            ServletContext app = getServletContext();
            Conf conf = (Conf) app.getAttribute("conf");
            RequestDispatcher disp;

            HttpSession session = req.getSession();
            UserItem currentUser = (UserItem) session.getAttribute("user");
            Integer uid = Integer.parseInt(req.getPathInfo().substring(1));
            try {
                UserItem user = new UserItem(ds, uid);

                if (!currentUser.hasEditAccessTo(user)) {
                    req.setAttribute("message_critical", "You do not have access to modify that user");
                    disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
                } else {
                    ArrayList<String> errors = new ArrayList<String>();

                    // only admin is allowed to edit usernames
                    if (currentUser.isAdmin()) {
                        String requestedUsername = req.getParameter("username");
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
                        user.setUsername(requestedUsername);
                    }

                    errors.addAll(user.validateEmailAddress(req.getParameter("email")));

                    // Validate passwords
                    String password1 = req.getParameter("password1") == null ? "" : req.getParameter("password1");
                    String password2 = req.getParameter("password2") == null ? "" : req.getParameter("password2");
                    if (!password1.equals("")) {
                        errors.addAll(user.validatePassword(password1, password2));
                    }
                    req.setAttribute("password1", password1);
                    req.setAttribute("password2", password2);

                    // Validate expiration
                    // Disallow setting expiration on yourself
                    // Unless you're admin
                    if (currentUser.isAdmin()
                            || !currentUser.getUid().equals(user.getUid())) {
                        boolean expiration = "true".equals(req.getParameter("bExpiration"));
                        int daysUntilExpiration = Integer.parseInt(req.getParameter("daysUntilExpiration"));
                        if (daysUntilExpiration < 1) {
                            daysUntilExpiration = conf.getDaysUserExpiration();
                        } else if (daysUntilExpiration > 365) {
                            daysUntilExpiration = 365;
                        }
                        if (expiration) {
                            user.setDaysUntilExpiration(daysUntilExpiration);
                        } else {
                            user.setDateExpiration(null);
                        }
                    }

                    // Only allow admin to set usertype
                    if (currentUser.isAdmin()) {
                        int requestedUsertype = Integer.parseInt(req.getParameter("usertype"));
                        user.setUserType(requestedUsertype);
                    }

                    req.setAttribute("user", user);

                    if (!errors.isEmpty()) {
                        String errormessage = "User edit failed due to the following " + (errors.size() == 1 ? "reason" : "reasons") + ":<ul>";
                        for (String emsg : errors) {
                            errormessage = errormessage.concat("<li>" + emsg + "</li>\n");
                        }
                        errormessage = errormessage.concat("</ul>\n");
                        req.setAttribute("message_critical", errormessage);
                    } else {
                        if (user.save(ds)) {
                            req.setAttribute("message", "Your changes have been saved");
                            req.setAttribute("password1", "");
                            req.setAttribute("password2", "");
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
