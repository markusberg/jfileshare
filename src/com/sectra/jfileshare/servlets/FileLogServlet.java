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

import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.objects.FileLog;
import com.sectra.jfileshare.objects.NoSuchFileException;
import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.utils.Helpers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
// import java.util.logging.Logger;

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

public class FileLogServlet extends HttpServlet {
    static final long serialVersionUID = 1L;

    private DataSource ds;
    // private static final Logger logger =
    //         Logger.getLogger(FileLogServlet.class.getName());

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
        HttpSession session = req.getSession();
        UserItem currentUser = (UserItem) session.getAttribute("user");
        String PathInfo = req.getPathInfo().substring(1);

        int fid = Integer.parseInt(PathInfo);

        ServletContext app = getServletContext();
        RequestDispatcher disp;
        try {
            FileItem file = new FileItem();
            file.fetch(ds, fid);

            if (!currentUser.hasEditAccessTo(file)) {
                req.setAttribute("message_critical", "You don't have access to view the logs of this file");
                disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
            } else {
                req.setAttribute("file", file);
                ArrayList<FileLog> downloadLogs = file.getLogs(ds);
                req.setAttribute("downloadLogs", downloadLogs);
                if (downloadLogs.isEmpty()) {
                    req.setAttribute("message", "The file <strong>\"" + Helpers.htmlSafe(file.getName()) + "\"</strong> has never been downloaded");
                }
                req.setAttribute("tab", "File log");
                disp = app.getRequestDispatcher("/templates/FileLog.jsp");
            }
        } catch (NoSuchFileException e) {
            req.setAttribute("message_warning", e.getMessage());
            disp = app.getRequestDispatcher("/templates/404.jsp");
        } catch (SQLException e) {
            req.setAttribute("message_critical", e.getMessage());
            disp = app.getRequestDispatcher("/templates/Error.jsp");
        }
        disp.forward(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
