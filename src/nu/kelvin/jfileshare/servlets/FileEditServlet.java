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
 * @author      Markus Berg  <markus.berg @ sectra.se>
 * @version     1.17
 * @since       2011-09-21
 */
package nu.kelvin.jfileshare.servlets;

import nu.kelvin.jfileshare.objects.Conf;
import nu.kelvin.jfileshare.objects.FileItem;
import nu.kelvin.jfileshare.objects.NoSuchFileException;
import nu.kelvin.jfileshare.objects.UserItem;

import java.io.IOException;
import java.sql.SQLException;

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

public class FileEditServlet extends HttpServlet {
    static final long serialVersionUID = 1L;

    private DataSource ds;
    // private static final Logger logger =
    //        Logger.getLogger(FileEditServlet.class.getName());

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

        String PathInfo = req.getPathInfo().substring(1);
        int fid = Integer.parseInt(PathInfo);
        try {
            FileItem file = new FileItem();
            file.fetch(ds, fid);

            HttpSession session = req.getSession();
            UserItem currentUser = (UserItem) session.getAttribute("user");

            if (currentUser.hasEditAccessTo(file)) {
                req.setAttribute("file", file);
                req.setAttribute("tab", "Edit file");
                disp = app.getRequestDispatcher("/templates/FileEdit.jsp");
            } else {
                req.setAttribute("message_critical", "You do not have access to edit that file");
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

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("login".equals(req.getParameter("action"))) {
            // This POST is the result of a login
            doGet(req, resp);
            return;
        }
        HttpSession session = req.getSession();
        UserItem currentUser = (UserItem) session.getAttribute("user");
        if (!currentUser.isValidCSRFToken(req.getParameter("CSRFToken"))) {
            doGet(req, resp);
            return;
        }

        ServletContext app = getServletContext();
        Conf conf = (Conf) app.getAttribute("conf");
        RequestDispatcher disp;

        int fid = Integer.parseInt(req.getPathInfo().substring(1));

        try {
            FileItem file = new FileItem();
            file.fetch(ds, fid);


            if (currentUser.hasEditAccessTo(file)) {
                req.setAttribute("tab", "Edit file");
                req.setAttribute("message", "Your changes to this file have been saved");
                if (req.getParameter("bEnabled") != null
                        && req.getParameter("bEnabled").equals("true")) {
                    file.setEnabled(true);
                } else {
                    file.setEnabled(false);
                }

                if ("true".equals(req.getParameter("bPermanent"))) {
                    file.setDateExpiration(null);
                } else {
                    // only set this if file expiration is enabled
                    if (conf.getDaysFileExpiration() != 0) {
                        file.setDaysToKeep(conf.getDaysFileExpiration());
                    }
                }

                if (req.getParameter("fileName") != null
                        && !req.getParameter("fileName").equals("")) {
                    file.setName(req.getParameter("fileName"));
                }

                Integer iDownloads = null;
                if (!"".equals(req.getParameter("iDownloads"))) {
                    try {
                        iDownloads = new Integer(req.getParameter("iDownloads"));
                    } catch (NumberFormatException ignore) {
                    }
                }
                file.setDownloads(iDownloads);

                if (req.getParameter("bUsePw") == null
                        || req.getParameter("bUsePw").equals("")) {
                    file.setPwHash(null);
                } else if (req.getParameter("bUsePw").equals("true")
                        && req.getParameter("sPassword") != null
                        && !req.getParameter("sPassword").equals("")) {
                    file.setPwPlainText(req.getParameter("sPassword"));
                }

                file.update(ds, req.getRemoteAddr());
                req.setAttribute("file", file);
                disp = app.getRequestDispatcher("/templates/FileEdit.jsp");
            } else {
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
