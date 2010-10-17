package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.objects.FileItem;

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
public class FileEditServlet extends HttpServlet {

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(FileEditServlet.class.getName());
    private String PATH_FILE_STORE;
    private int DAYS_FILE_RETENTION;

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            ds = (DataSource) env.lookup("jdbc/jfileshare");

            ServletContext context = getServletContext();
            PATH_FILE_STORE = context.getInitParameter("PATH_STORE").toString();
            DAYS_FILE_RETENTION = Integer.parseInt(context.getInitParameter("DAYS_FILE_RETENTION").toString());
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
        int iFid = Integer.parseInt(PathInfo);
        FileItem oFile = new FileItem(ds, iFid);

        if (isAuthenticated(oFile, req)) {
            req.setAttribute("oFile", oFile);
            if (req.getServletPath().equals("/file/edit")) {
                req.setAttribute("tab", "Edit file");
                disp = app.getRequestDispatcher("/templates/FileEdit.jsp");
            } else {
                req.setAttribute("tab", "Delete file");
                disp = app.getRequestDispatcher("/templates/FileDelete.jsp");
            }
        } else {
            disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
        }
        disp.forward(req, resp);

    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (req.getParameter("action") != null
                && req.getParameter("action").equals("login")) {
            // This POST is the result of a login
            doGet(req, resp);
        } else {
            ServletContext app = getServletContext();
            RequestDispatcher disp;

            String PathInfo = req.getPathInfo().substring(1);
            int iFid = Integer.parseInt(PathInfo);
            FileItem oFile = new FileItem(ds, iFid);

            if (isAuthenticated(oFile, req)) {
                if (req.getServletPath().equals("/file/edit")) {
                    req.setAttribute("tab", "Edit file");
                    req.setAttribute("message", "Your changes to this file have been saved");
                    if (req.getParameter("bEnabled") != null
                            && req.getParameter("bEnabled").equals("true")) {
                        oFile.setEnabled(true);
                    } else {
                        oFile.setEnabled(false);
                    }

                    if (req.getParameter("bPermanent") != null
                            && req.getParameter("bPermanent").equals("true")) {
                        oFile.setDateExpiration(null);
                    } else {
                        oFile.setDaysToKeep(DAYS_FILE_RETENTION);
                    }

                    Integer iDownloads = null;
                    if (req.getParameter("iDownloads") != null
                            && !req.getParameter("iDownloads").equals("")) {
                        iDownloads = new Integer(req.getParameter("iDownloads"));
                    }
                    oFile.setDownloads(iDownloads);

                    if (req.getParameter("bUsePw") == null
                            || req.getParameter("bUsePw").equals("")) {
                        oFile.setPwHash(null);
                    } else if (req.getParameter("bUsePw").equals("true")
                            && req.getParameter("sPassword") != null
                            && !req.getParameter("sPassword").equals("")) {
                        oFile.setPwPlainText(req.getParameter("sPassword"));
                    }

                    oFile.save(ds);
                    req.setAttribute("oFile", oFile);
                    disp = app.getRequestDispatcher("/templates/FileEdit.jsp");
                } else {
                    req.setAttribute("tab", "Delete file");
                    oFile.delete(ds, PATH_FILE_STORE);
                    req.setAttribute("message", "File " + oFile.getName() + " was successfully deleted");
                    disp = app.getRequestDispatcher("/templates/Blank.jsp");
                }
            } else {
                disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
            }

            disp.forward(req, resp);
        }
    }

    private boolean isAuthenticated(FileItem oFile, HttpServletRequest req) {
        HttpSession session = req.getSession();
        UserItem oCurrentUser = (UserItem) session.getAttribute("user");

        if (oFile.getFid() == null) {
            logger.info("File not found");
            req.setAttribute("message_warning", "The requested file is not found");
            return false;
        } else if (!isAllowedFileEdit(oCurrentUser, oFile)) {
            logger.info("User " + oCurrentUser.getUserInfo() + " does not have edit access to file " + oFile.getFid());
            req.setAttribute("message_critical", "You do not have access to edit this file");
            return false;
        }
        return true;
    }

    /**
     * Check if user is authorized to edit the file
     *
     * @param oUser
     * @param oFile
     * @return
     */
    private boolean isAllowedFileEdit(UserItem oUser, FileItem oFile) {
        if (oUser.isAdmin()) {
            logger.info("Administrator access to edit file " + oFile.getFid());
            return true;
        } else if (oFile.getOwnerUid() == oUser.getUid()) {
            logger.info("Owner access to edit file " + oFile.getFid());
            return true;
        }
        return false;
    }
}

