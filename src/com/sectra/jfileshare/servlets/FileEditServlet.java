package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.Conf;
import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.objects.NoSuchFileException;
import com.sectra.jfileshare.objects.UserItem;

import java.io.IOException;
import java.sql.SQLException;
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
public class FileEditServlet extends HttpServlet {

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(FileEditServlet.class.getName());

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
            FileItem file = new FileItem(ds, fid);

            HttpSession session = req.getSession();
            UserItem User = (UserItem) session.getAttribute("user");

            if (User.hasEditAccessTo(file)) {
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
        ServletContext app = getServletContext();
        RequestDispatcher disp;

        int fid = Integer.parseInt(req.getPathInfo().substring(1));

        try {
            FileItem file = new FileItem(ds, fid);

            HttpSession session = req.getSession();
            UserItem user = (UserItem) session.getAttribute("user");
            Conf conf = (Conf) getServletContext().getAttribute("conf");

            if (user.hasEditAccessTo(file)) {
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
