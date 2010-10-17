package com.sectra.jfileshare.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

/***
 * Simple http forwarding servlet allowing link compatibility with older versions
 * of jfileshare
 * @author markus
 */
public class LinkCompatibilityServlet extends HttpServlet {

    private static final Logger logger =
            Logger.getLogger(LinkCompatibilityServlet.class.getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String PathInfo = req.getPathInfo().substring(1);
        String md5sum = PathInfo.split("_SECTRA_")[0];
        int iFid = Integer.parseInt(PathInfo.split("_SECTRA_")[1]);

        logger.info("Access requested to file: " + iFid);

        String url = req.getContextPath();
        if (req.getServletPath().equals("/download/view")) {
            url += "/file/view/" + iFid + "?md5=" + md5sum;
        } else if (req.getServletPath().equals("/download/get")) {
            url += "/file/download/" + iFid + "?md5=" + md5sum;
        }
        resp.sendRedirect(url);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}