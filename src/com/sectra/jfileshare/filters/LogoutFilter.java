package com.sectra.jfileshare.filters;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import com.sectra.jfileshare.objects.UserItem;

public class LogoutFilter implements Filter {

    private FilterConfig filterconfig;
    private static final Logger logger =
            Logger.getLogger(LogoutFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException {
        this.filterconfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        UserItem user = (UserItem) request.getSession().getAttribute("user");
        if (user != null) {
            request.getSession().removeAttribute("user");
            request.getSession().removeAttribute("authfiles");
            request.getSession().removeAttribute("uploadListener");
            request.setAttribute("message", "You are now logged out");
        }
        if (request.getParameter("reason") != null && request.getParameter("reason").equals("inactivity")) {
            logger.log(Level.INFO, "Logging out user {0} due to inactivity", user.getUserInfo());
            request.setAttribute("message", "You have been logged out due to inactivity");
        } else {
            logger.log(Level.INFO, "User {0} logged out", user.getUserInfo());
        }
        filterconfig.getServletContext().getRequestDispatcher("/index.jsp").forward(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
