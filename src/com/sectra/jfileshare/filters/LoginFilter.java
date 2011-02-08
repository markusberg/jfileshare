package com.sectra.jfileshare.filters;

import com.sectra.jfileshare.objects.NoSuchUserException;
import com.sectra.jfileshare.objects.UserItem;

import java.io.IOException;
import java.sql.SQLException;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @Override
    public void init(FilterConfig config)
            throws ServletException {
        logger.info("Running LoginFilter");
        try {
            this.filterconfig = config;
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            ds = (DataSource) env.lookup("jdbc/jfileshare");
        } catch (NamingException e) {
            logger.severe(e.toString());
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {
        ds = null;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();
        HttpServletResponse resp = (HttpServletResponse) response;

        String urlPattern = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());

        if (session.getAttribute("user") != null) {
            // First check if we are already logged in
            chain.doFilter(request, response);
        } else if (CheckUser(req, session)) {
            // Sending a redirect instead of just forwarding to the correct page.
            // This makes the backing back to this page not force a re-post of the login form
            // resp.sendRedirect(urlPattern);
            chain.doFilter(request, response);
        } else {
            // User not logged in or login error.
            // Save the url and divert to the login page.
            req.setAttribute("urlPattern", urlPattern);
            filterconfig.getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }

    /**
     * Verify the provided username and password
     * @param req
     * @param session
     * @return
     */
    private boolean CheckUser(HttpServletRequest req, HttpSession session) {
        // Check if we are logging in right now
        if ("login".equals(req.getParameter("action"))) {
            String username = req.getParameter("login_username");
            String pwPlaintext = req.getParameter("login_password");

            if (username != null && pwPlaintext != null) {
                try {
                    UserItem user = new UserItem(ds, username);
                    if (!user.authenticated(pwPlaintext)) {
                        throw new NoSuchUserException();
                    } else {
                        logger.log(Level.INFO, "User {0} is now logged in", user.getUserInfo());
                        user.saveLastLogin(ds);
                        session.setAttribute("user", user);
                        return true;
                    }
                } catch (NoSuchUserException e) {
                    req.setAttribute("message_warning", "Non-existent user or incorrect password");
                } catch (SQLException e) {
                    req.setAttribute("message_critical", e.toString());
                }
            }
        }
        return false;
    }
}
