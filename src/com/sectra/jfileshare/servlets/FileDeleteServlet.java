package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.objects.FileItem;

import java.io.IOException;
import java.io.PrintWriter;

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
    private String PATH_FILE_STORE;

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            ds = (DataSource) env.lookup("jdbc/jfileshare");
            PATH_FILE_STORE = getServletContext().getInitParameter("PATH_STORE").toString();
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
        FileItem oFile = new FileItem(ds, iFid);

        UserItem User = (UserItem) session.getAttribute("user");

        if (User.hasEditAccessTo(oFile)) {
            if (oFile.delete(ds, PATH_FILE_STORE)) {
                UserItem oUser;
                if (User.getUid().equals(oFile.getOwnerUid())) {
                    oUser = User;
                } else {
                    oUser = new UserItem(ds, oFile.getOwnerUid());
                    req.setAttribute("tab", oUser.getUsername());
                }
                req.setAttribute("oUser", oUser);
                req.setAttribute("aFiles", oUser.getFiles(ds));
                req.setAttribute("aUsers", oUser.getChildren(ds));
                req.setAttribute("message", "File <em>\"" + oFile.getName() + "\"</em> was successfully deleted");
                disp = app.getRequestDispatcher("/templates/UserView.jsp");
            } else {
                req.setAttribute("message_critical", "File delete failed");
                req.setAttribute("tab", "Error");
                disp = app.getRequestDispatcher("/templates/Blank.jsp");
            }
        } else {
            req.setAttribute("message_warning", "You don't have access to delete that file");
            logger.info("Illegal file delete by " + User.getUserInfo());
            disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
        }
        disp.forward(req, resp);
    }
}



