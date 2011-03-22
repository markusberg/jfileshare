package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.Conf;
import com.sectra.jfileshare.objects.FileItem;
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
            req.setAttribute("validatedBrandingOrg", conf.getBrandingOrg());
            req.setAttribute("validatedBrandingDomain", conf.getBrandingDomain());
            req.setAttribute("validatedBrandingLogo", conf.getBrandingLogo());

            req.setAttribute("validatedPathStore", conf.getPathStore());
            req.setAttribute("validatedPathTemp", conf.getPathTemp());
            req.setAttribute("validatedDaysFileRetention", conf.getDaysFileRetention());

            int[] fs = FileItem.getFileSize(conf.getFileSizeMax());
            req.setAttribute("validatedFileSizeMax", fs[1]);
            req.setAttribute("validatedFileSizeUnit", fs[0]);

            req.setAttribute("validatedSmtpServer", conf.getSmtpServer());
            req.setAttribute("validatedSmtpServerPort", conf.getSmtpServerPort());
            req.setAttribute("validatedSmtpSender", conf.getSmtpSender());

            req.setAttribute("validatedDaysUserExpiration", conf.getDaysUserExpiration());
            req.setAttribute("validatedDebug", conf.getDebug() ? "checked" : "");
            req.setAttribute("dbVersion", conf.getDbVersion());

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
        } else {
            ServletContext app = getServletContext();
            RequestDispatcher disp;
            HttpSession session = req.getSession();
            UserItem user = (UserItem) session.getAttribute("user");
            Conf conf = (Conf) app.getAttribute("conf");

            if (user.isAdmin()) {
                disp = app.getRequestDispatcher("/templates/Admin.jsp");

                // FIXME: No sanity checking on the user input
                conf.setBrandingOrg(req.getParameter("brandingOrg"));
                req.setAttribute("validatedBrandingOrg", conf.getBrandingOrg());
                conf.setBrandingDomain(req.getParameter("brandingDomain"));
                req.setAttribute("validatedBrandingDomain", conf.getBrandingDomain());
                conf.setBrandingLogo(req.getParameter("brandingLogo"));
                req.setAttribute("validatedBrandingLogo", conf.getBrandingLogo());

                conf.setPathStore(req.getParameter("pathStore"));
                req.setAttribute("validatedPathStore", conf.getPathStore());
                conf.setPathTemp(req.getParameter("pathTemp"));
                req.setAttribute("validatedPathTemp", conf.getPathTemp());
                conf.setDaysFileRetention(Integer.parseInt(req.getParameter("daysFileRetention")));
                req.setAttribute("validatedDaysFileRetention", conf.getDaysFileRetention());

                conf.setSmtpServer(req.getParameter("smtpServer"));
                req.setAttribute("validatedSmtpServer", conf.getSmtpServer());
                conf.setSmtpServerPort(Integer.parseInt(req.getParameter("smtpServerPort")));
                req.setAttribute("validatedSmtpServerPort", conf.getSmtpServerPort());
                conf.setSmtpSender(req.getParameter("smtpSender"));
                req.setAttribute("validatedSmtpSender", conf.getSmtpSender());

                long validatedFileSizeMax = Long.parseLong(req.getParameter("fileSizeMax"));
                int validatedFileSizeUnit = Integer.parseInt(req.getParameter("fileSizeUnit"));

                conf.setFileSizeMax(validatedFileSizeMax * (long) Math.pow(1024, validatedFileSizeUnit));
                req.setAttribute("validatedFileSizeMax", validatedFileSizeMax);
                req.setAttribute("validatedFileSizeUnit", validatedFileSizeUnit);

                conf.setDaysUserExpiration(Integer.parseInt(req.getParameter("daysUserExpiration")));
                req.setAttribute("validatedDaysUserExpiration", conf.getDaysUserExpiration());

                conf.setDebug("true".equals(req.getParameter("debug")) ? true : false);
                req.setAttribute("validatedDebug", conf.getDebug() ? "checked" : "");
                
                req.setAttribute("dbVersion", conf.getDbVersion());

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
}
