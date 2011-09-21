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
import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.objects.NoSuchFileException;
import com.sectra.jfileshare.objects.NoSuchUserException;
import com.sectra.jfileshare.utils.Helpers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;


import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.sql.DataSource;

/**
 * @author  Markus Berg <markus.berg@sectra.se>
 * @version 2010-05-30
 * @since   1.5
 */
public class FileDeleteServlet extends HttpServlet {
    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(FileDeleteServlet.class.getName());

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
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        ServletContext app = getServletContext();
        RequestDispatcher disp;

        String PathInfo = req.getPathInfo().substring(1);
        int iFid = Integer.parseInt(PathInfo);
        try {
            FileItem file = new FileItem();
            file.fetch(ds, iFid);
            UserItem currentUser = (UserItem) session.getAttribute("user");

            if (currentUser.hasEditAccessTo(file)) {
                Conf conf = (Conf) getServletContext().getAttribute("conf");
                if (file.delete(ds, conf.getPathStore(), req.getRemoteAddr())) {
                    // FIXME: this is ugly. Should be ajax instead.
                    UserItem user = null;
                    if (currentUser.getUid().equals(file.getOwnerUid())) {
                        user = currentUser;
                    } else {
                        try {
                            user = new UserItem();
                            user.fetch(ds, file.getOwnerUid());
                        } catch (NoSuchUserException ignored) {
                        } catch (SQLException ignored) {
                        }
                        req.setAttribute("tab", user.getUsername());
                    }
                    req.setAttribute("user", user);
                    req.setAttribute("files", user.getFiles(ds));
                    req.setAttribute("users", user.getChildren(ds));
                    req.setAttribute("message", "File <strong>\"" + Helpers.htmlSafe(file.getName()) + "\"</strong> was successfully deleted");
                    disp = app.getRequestDispatcher("/templates/UserView.jsp");
                } else {
                    req.setAttribute("message_critical", "File delete failed");
                    disp = app.getRequestDispatcher("/templates/Error.jsp");
                }
            } else {
                req.setAttribute("message_warning", "You don't have access to delete that file");
                disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
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
}
