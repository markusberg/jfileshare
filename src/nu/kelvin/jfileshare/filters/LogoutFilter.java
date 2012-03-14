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

import nu.kelvin.jfileshare.objects.UserItem;

public class LogoutFilter implements Filter {

    private FilterConfig filterconfig;
    private static final Logger logger =
            Logger.getLogger(LogoutFilter.class.getName());

    public void init(FilterConfig filterConfig)
            throws ServletException {
        this.filterconfig = filterConfig;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        UserItem user = (UserItem) request.getSession().getAttribute("user");
        if (user != null) {
            request.getSession().removeAttribute("user");
            request.getSession().removeAttribute("authfiles");
            request.getSession().removeAttribute("uploadListener");
        }
        if (request.getParameter("reason") != null && request.getParameter("reason").equals("inactivity")) {
            request.setAttribute("message", "You have been logged out due to inactivity");
            logger.log(Level.INFO, "Logging out user {0} due to inactivity",
                    (user == null ? "" : user.getUserInfo()));
        } else if (request.getParameter("reason") != null && request.getParameter("reason").equals("sessionexpired")) {
            request.setAttribute("message", "You have been logged out because your session has expired");
            logger.log(Level.INFO, "Logging out user {0} because the session expired",
                    (user == null ? "" : user.getUserInfo()));
        } else {
            request.setAttribute("message", "You are now logged out");
            logger.log(Level.INFO, "User {0} logged out",
                    (user == null ? "" : user.getUserInfo()));
        }
        filterconfig.getServletContext().getRequestDispatcher("/index.jsp").forward(servletRequest, servletResponse);
    }

    public void destroy() {
    }
}
