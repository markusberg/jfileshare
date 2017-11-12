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
 * @version     1.17
 * @since       2011-09-21
 */
package nu.kelvin.jfileshare.servlets;

import nu.kelvin.jfileshare.objects.Conf;
import nu.kelvin.jfileshare.objects.UserItem;
import nu.kelvin.jfileshare.objects.FileItem;
import nu.kelvin.jfileshare.objects.NoSuchFileException;
import nu.kelvin.jfileshare.objects.NoSuchUserException;
import nu.kelvin.jfileshare.utils.Helpers;

import java.io.IOException;
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

public class FileDeleteServlet extends HttpServlet {
    static final long serialVersionUID = 1L;

    private DataSource ds;
    // private static final Logger logger =
    //         Logger.getLogger(FileDeleteServlet.class.getName());

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
        UserItem currentUser = (UserItem) session.getAttribute("user");
        ServletContext app = getServletContext();
        RequestDispatcher disp;

        if (!currentUser.isValidCSRFToken(req.getParameter("CSRFToken"))) {
            // CSRF token is missing, we can just fail spectacularly here
            return;
        }

        String PathInfo = req.getPathInfo().substring(1);
        int iFid = Integer.parseInt(PathInfo);
        try {
            FileItem file = new FileItem();
            file.fetch(ds, iFid);

            if (currentUser.hasEditAccessTo(file)) {
                Conf conf = (Conf) app.getAttribute("conf");
                if (file.delete(ds, conf.getPathStore(), req.getRemoteAddr())) {
                    // FIXME: this is ugly. Should be ajax instead.
                    UserItem user = null;
                    if (currentUser.getUid().equals(file.getUid())) {
                        user = currentUser;
                    } else {
                        try {
                            user = new UserItem();
                            user.fetch(ds, file.getUid());
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
