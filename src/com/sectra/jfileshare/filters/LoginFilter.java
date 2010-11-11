package com.sectra.jfileshare.filters;

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

import java.util.logging.Logger;

import java.io.IOException;

import com.sectra.jfileshare.objects.UserItem;

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

        if (CheckUser(req, session)) {
            chain.doFilter(request, response);
        } else {
            // User not logged in or login error.
            // Save the url and divert to the login page.
            req.setAttribute("urlPattern", req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo()));
            filterconfig.getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }

    private boolean CheckUser(HttpServletRequest req, HttpSession session) {
        // First check if we are already logged in
        if (session.getAttribute("user") != null) {
            return true;
        }
        // Second, check if we are logging in right now
        if ("login".equals(req.getParameter("action"))) {
            String username = req.getParameter("login_username");
            String pwPlaintext = req.getParameter("login_password");

            if (username != null && pwPlaintext != null) {
                UserItem oUser = new UserItem(ds, username);
                if (oUser.getUid() == null || !oUser.authenticated(pwPlaintext)) {
                    req.setAttribute("message_warning", "Non-existent user or incorrect password");
                } else {
                    logger.info("User " + oUser.getUserInfo() + " is now logged in");
                    oUser.saveLastLogin(ds);
                    session.setAttribute("user", oUser);
                    return true;
                }
                if (1 == 0) {
                    req.setAttribute("message_critical", "Unable to connect to the database. Please contact the system administrator.");
                }
            }
        }
        return false;
    }
}
