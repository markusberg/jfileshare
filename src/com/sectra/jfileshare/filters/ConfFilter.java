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
package com.sectra.jfileshare.filters;

import com.sectra.jfileshare.objects.Conf;

import java.io.IOException;

// import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;

import javax.servlet.http.HttpServletRequest;
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
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Conf conf = (Conf) filterconfig.getServletContext().getAttribute("conf");
        if (conf == null) {
            conf = new Conf(ds);
            conf.setUrlPrefix(request);
            HttpServletRequest req = (HttpServletRequest) request;
            conf.setContextPath(req.getContextPath());
            filterconfig.getServletContext().setAttribute("conf", conf);
        }
        chain.doFilter(request, response);
    }
}
