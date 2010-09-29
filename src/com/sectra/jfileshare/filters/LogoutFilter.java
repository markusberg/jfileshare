package com.sectra.jfileshare.filters;

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
            logger.info("Logging out user " + user.getUsername());
            request.getSession().removeAttribute("user");
            request.getSession().removeAttribute("authfiles");
            request.getSession().removeAttribute("uploadListener");
            // request.setAttribute("address", request.getContextPath());
            request.setAttribute("message", "You are now logged out");
            request.setAttribute("tab", "Logout");
        }
        filterconfig.getServletContext().getRequestDispatcher("/templates/Blank.jsp").forward(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        logger.info("User logged out");
    }
}
