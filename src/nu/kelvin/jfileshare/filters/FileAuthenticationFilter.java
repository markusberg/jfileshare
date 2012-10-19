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
 * @version     1.8
 * @since       2011-09-21
 */
package nu.kelvin.jfileshare.filters;

import nu.kelvin.jfileshare.objects.FileItem;
import nu.kelvin.jfileshare.objects.NoSuchFileException;
import nu.kelvin.jfileshare.objects.UserItem;

import java.io.IOException;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.sql.DataSource;

public class FileAuthenticationFilter implements Filter {
    private FilterConfig filterconfig;
    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(FileAuthenticationFilter.class.getName());

    public void init(FilterConfig filterConfig)
            throws ServletException {
        this.filterconfig = filterConfig;
        try {
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

    public void doFilter(ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();

        Integer fid = Integer.parseInt(req.getPathInfo().substring(1));
        String md5sum = req.getParameter("md5");

        try {
            FileItem file = new FileItem();
            file.fetch(ds, fid);

            UserItem currentUser = (UserItem) session.getAttribute("user");
            if (file.getDownloads() != null) {
                logger.log(Level.INFO, "downloads left: {0}", Integer.toString(file.getDownloads()));
            }

            if (!file.getMd5sum().equals(md5sum)) {
                throw new NoSuchFileException("File not found. Incomplete link");
            } else if (currentUser != null && currentUser.hasEditAccessTo(file)) {
                request.setAttribute("file", file);
                chain.doFilter(request, response);
            } else if (!file.isEnabled()) {
                logger.info("File found, but it's disabled");
                request.setAttribute("message_critical", "The requested file has been disabled by its owner");
                filterconfig.getServletContext().getRequestDispatcher("/templates/AccessDenied.jsp").forward(request, response);
            } else if (file.getDownloads() != null && file.getDownloads() == 0) {
                logger.info("File found, but has reached max number of downloads");
                request.setAttribute("message_critical", "The requested file exists, but it has reached its limit on number of downloads. If you require access to this file, please contact the file owner.");
                filterconfig.getServletContext().getRequestDispatcher("/templates/AccessDenied.jsp").forward(request, response);
            } else if (file.getPwHash() == null) {
                logger.info("File does not require authentication");
                request.setAttribute("file", file);
                chain.doFilter(request, response);
            } else if (!authenticated(file, session, req)) {
                // Send to password-screen
                logger.log(Level.INFO, "File {0} is password protected", file.getFid());
                request.setAttribute("tab", "File authentication");
                request.setAttribute("urlPattern", req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo()) + "?md5=" + file.getMd5sum());
                filterconfig.getServletContext().getRequestDispatcher("/templates/FilePassword.jsp").forward(request, response);
            } else {
                // Everything appears to check out
                request.setAttribute("file", file);
                chain.doFilter(request, response);
            }
        } catch (NoSuchFileException e) {
            request.setAttribute("message_warning", e.getMessage());
            filterconfig.getServletContext().getRequestDispatcher("/templates/404.jsp").forward(request, response);
        } catch (SQLException e) {
            request.setAttribute("message_critical", e.getMessage());
            filterconfig.getServletContext().getRequestDispatcher("/templates/Error.jsp").forward(request, response);
        }
    }

    private boolean authenticated(FileItem file, HttpSession session, HttpServletRequest req) {
        //First, are we authenticated for this file
        if (session.getAttribute("authfiles") != null) {
            @SuppressWarnings("unchecked")
            ArrayList<Integer> authfiles = (ArrayList<Integer>) session.getAttribute("authfiles");

            if (authfiles.contains(file.getFid())) {
                logger.info("User already authenticated for file");
                return true;
            }
        }

        // We need to authenticate; do we have a password to authenticate?
        if (req.getParameter("FilePassword") != null) {
            // We are logging in.. verify the password.
            if (file.authenticated(req.getParameter("FilePassword"))) {
                logger.log(Level.INFO, "Saving {0} to authfiles in session ", file.getFid());
                if (session.getAttribute("authfiles") != null) {
                    @SuppressWarnings("unchecked")
                    ArrayList<Integer> authfiles = (ArrayList<Integer>) session.getAttribute("authfiles");
                    authfiles.add(file.getFid());
                    session.setAttribute("authfiles", authfiles);
                } else {
                    ArrayList<Integer> authfiles = new ArrayList<Integer>();
                    authfiles.add(file.getFid());
                    session.setAttribute("authfiles", authfiles);
                }
                return true;
            } else {
                req.setAttribute("message_warning", "Incorrect password");
            }
        }
        return false;
    }
}
