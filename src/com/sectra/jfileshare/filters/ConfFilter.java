package com.sectra.jfileshare.filters;

import com.sectra.jfileshare.objects.Conf;

import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.sql.DataSource;

public class ConfFilter implements Filter {
    private FilterConfig filterconfig;
    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(LoginFilter.class.getName());

    @Override
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

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Conf conf = (Conf) filterconfig.getServletContext().getAttribute("conf");
        if (conf == null) {
            conf = new Conf(ds);
            filterconfig.getServletContext().setAttribute("conf", conf);
        }
        chain.doFilter(request, response);
    }
}
