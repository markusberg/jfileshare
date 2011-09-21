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
package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.Conf;
import com.sectra.jfileshare.objects.UserItem;

import java.io.IOException;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;

import javax.sql.DataSource;

/**
 * @author  Markus Berg <markus.berg@sectra.se>
 * @version 2010-05-30
 * @since   1.5
 */
public class AdminServlet extends HttpServlet {

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(AdminServlet.class.getName());

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
        RequestDispatcher disp;
        HttpSession session = req.getSession();
        UserItem User = (UserItem) session.getAttribute("user");
        Conf conf = (Conf) app.getAttribute("conf");

        if (User.isAdmin()) {
            req.setAttribute("conf", conf);
            disp = app.getRequestDispatcher("/templates/Admin.jsp");
        } else {
            req.setAttribute("message_critical", "Access Denied");
            disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
        }
        disp.forward(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (req.getParameter("action") != null
                && req.getParameter("action").equals("login")) {
            doGet(req, resp);
            return;
        }
        ServletContext app = getServletContext();
        RequestDispatcher disp;
        HttpSession session = req.getSession();
        UserItem user = (UserItem) session.getAttribute("user");
        Conf conf = (Conf) app.getAttribute("conf");

        if (user.isAdmin()) {
            disp = app.getRequestDispatcher("/templates/Admin.jsp");

            // FIXME: No sanity checking on the user input
            conf.setBrandingOrg(req.getParameter("brandingOrg"));
            conf.setBrandingDomain(req.getParameter("brandingDomain"));
            conf.setBrandingLogo(req.getParameter("brandingLogo"));
            conf.setPathStore(req.getParameter("pathStore"));
            conf.setPathTemp(req.getParameter("pathTemp"));

            int daysFileExpiration;
            try {
                daysFileExpiration = Integer.parseInt(req.getParameter("daysFileExpiration"));
            } catch (NumberFormatException e) {
                daysFileExpiration = 0;
            }
            conf.setDaysFileExpiration(daysFileExpiration);
            conf.setSmtpServer(req.getParameter("smtpServer"));

            int smtpServerPort;
            try {
                smtpServerPort = Integer.parseInt(req.getParameter("smtpServerPort"));
            } catch (NumberFormatException e) {
                smtpServerPort = 25;
            }
            conf.setSmtpServerPort(smtpServerPort);
            conf.setSmtpSender(req.getParameter("smtpSender"));

            int fileSizeMax = Integer.parseInt(req.getParameter("fileSizeMax"));
            int fileSizeUnit = Integer.parseInt(req.getParameter("fileSizeUnit"));

            conf.setFileSizeMax(fileSizeMax * (long) Math.pow(1024, fileSizeUnit));

            int daysUserExpiration;
            try {
                daysUserExpiration = Integer.parseInt(req.getParameter("daysUserExpiration"));
            } catch (NumberFormatException e) {
                daysUserExpiration = 60;
            }
            conf.setDaysUserExpiration(daysUserExpiration);

            int daysPasswordExpiration;
            try {
                daysPasswordExpiration = Integer.parseInt(req.getParameter("daysPasswordExpiration"));
            } catch (NumberFormatException e) {
                daysPasswordExpiration = 0;
            }
            conf.setDaysPasswordExpiration(daysPasswordExpiration);
            conf.setDebug("true".equals(req.getParameter("debug")) ? true : false);

            if (conf.save(ds)) {
                req.setAttribute("message", "Changes saved");
                app.setAttribute("conf", conf);
            } else {
                req.setAttribute("message_warning", "Unable to save changes");
            }

        } else {
            req.setAttribute("message_critical", "Access Denied");
            disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
        }
        disp.forward(req, resp);
    }
}
