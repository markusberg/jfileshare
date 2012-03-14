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
package nu.kelvin.jfileshare.filters;

import nu.kelvin.jfileshare.objects.Conf;
import nu.kelvin.jfileshare.objects.NoSuchUserException;
import nu.kelvin.jfileshare.objects.UserItem;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.sql.DataSource;

public class LoginFilter implements Filter {

    private FilterConfig filterconfig;
    private DataSource ds = null;
    private static final Logger logger =
            Logger.getLogger(LoginFilter.class.getName());

    public void init(FilterConfig config)
            throws ServletException {
        try {
            this.filterconfig = config;
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            ds = (DataSource) env.lookup("jdbc/jfileshare");
        } catch (NamingException e) {
            logger.severe(e.toString());
            throw new ServletException(e);
        }
    }

    public void destroy() {
        ds = null;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();

        if (session.getAttribute("user") != null) {
            // we're already logged in
            chain.doFilter(request, response);
        } else {
            String urlPattern = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());

            // This session was used for a forced password update, but that
            // action was never completed. 
            // Delete the tempuser object from the session:
            if (!"forcedPasswordUpdate".equals(request.getParameter("action"))
                    && session.getAttribute("tempuser") != null) {
                session.removeAttribute("tempuser");
            }

            // We're in the middle of a forced password update
            // validate provided passwords
            if (session.getAttribute("tempuser") != null) {
                UserItem tempuser = (UserItem) session.getAttribute("tempuser");

                String requestedPassword1 = req.getParameter("password1") == null ? "" : req.getParameter("password1");
                String requestedPassword2 = req.getParameter("password2") == null ? "" : req.getParameter("password2");

                ArrayList<String> errors = tempuser.validatePassword(requestedPassword1, requestedPassword2);

                if (errors.isEmpty()) {
                    // Everything checks out. Save user. Move on.
                    tempuser.update(ds, req.getRemoteAddr());
                    session.setAttribute("user", tempuser);
                    session.removeAttribute("tempuser");
                    tempuser.saveLastLogin(ds, req.getRemoteAddr());
                    chain.doFilter(request, response);
                } else {
                    String errormessage = "Password change failed due to the following " + (errors.size() == 1 ? "reason" : "reasons") + ":<ul>";
                    for (String emsg : errors) {
                        errormessage = errormessage.concat("<li>" + emsg + "</li>\n");
                    }
                    errormessage = errormessage.concat("</ul>\n");
                    req.setAttribute("message_critical", errormessage);
                    filterconfig.getServletContext().getRequestDispatcher("/templates/PasswordUpdate.jsp").forward(request, response);
                }
            } else {
                // We're logging in
                UserItem user = CheckUser(req, session);
                if (user != null) {
                    Conf conf = new Conf(ds);
                    if (conf.getDaysPasswordExpiration() != 0
                            && user.passwordIsOlderThan(conf.getDaysPasswordExpiration())) {
                        // User is forced to update his password
                        // Store the user object in a temporary variable in the session
                        session.setAttribute("tempuser", user);
                        req.setAttribute("message_warning", "Your password has expired. Please choose a new one.");
                        req.setAttribute("urlPattern", urlPattern);
                        filterconfig.getServletContext().getRequestDispatcher("/templates/PasswordUpdate.jsp").forward(request, response);
                    } else {
                        // FIXME: I'd like to do a redirect here instead of just forwarding to the correct page.
                        // This makes the backing back to this page not force a re-post of the login form
                        // resp.sendRedirect(urlPattern);

                        user.saveLastLogin(ds, req.getRemoteAddr());
                        session.setAttribute("user", user);
                        chain.doFilter(request, response);
                    }
                } else {
                    // User not logged in or login error.
                    // Save the url and divert to the login page.
                    req.setAttribute("urlPattern", urlPattern);
                    filterconfig.getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
                }
            }
        }
    }

    /**
     * Verify the provided username and password
     * @param req
     * @param session
     * @return
     */
    private UserItem CheckUser(HttpServletRequest req, HttpSession session) {
        // Check if we are logging in right now
        if ("login".equals(req.getParameter("action"))) {
            String username = req.getParameter("login_username");
            String pwPlaintext = req.getParameter("login_password");

            if (username != null && pwPlaintext != null) {
                try {
                    UserItem user = new UserItem();
                    user.fetch(ds, username);
                    if (!user.authenticated(pwPlaintext)) {
                        throw new NoSuchUserException();
                    } else {
                        logger.log(Level.INFO, "User {0} is now logged in", user.getUserInfo());
                        return user;
                    }
                } catch (NoSuchUserException e) {
                    req.setAttribute("message_warning", "Non-existent user or incorrect password");
                } catch (SQLException e) {
                    req.setAttribute("message_critical", e.toString());
                }
            }
        }
        return null;
    }
}
