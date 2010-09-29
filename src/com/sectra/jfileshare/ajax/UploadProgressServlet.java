package com.sectra.jfileshare.ajax;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.logging.Logger;

/**
 * Send an xml response to how the upload is progressing
 *
 * @author markus
 */
public class UploadProgressServlet extends HttpServlet {

    private static final Logger logger =
            Logger.getLogger(UploadProgressServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        PrintWriter out = res.getWriter();
        HttpSession session = req.getSession();
        FileUploadListener listener = null;
        StringBuffer buffy = new StringBuffer();
        long bytesRead = 0L;
        long contentLength = 0L;
        logger.info("Upload progress is running");

        // Make sure the session has started
        if (session == null) {
            logger.info("No session found");
            out.println("OK");
            out.flush();
            out.close();
            return;
        }

        listener = (FileUploadListener) session.getAttribute("uploadListener");

        if (listener == null) {
            // The listener object hasn't been created yet
            logger.info("No listener object found");
            out.println("OK");
            out.flush();
            out.close();
            return;
        }

        res.setContentType("text/xml;charset=UTF-8");
        buffy.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        buffy.append("<response>\n");
        bytesRead = listener.getBytesRead();
        contentLength = listener.getContentLength();

        buffy.append("\t<bytesRead>" + bytesRead + "</bytesRead>\n");
        buffy.append("\t<bytesTotal>" + contentLength + "</bytesTotal>\n");

        // Check to see if we're done
        if (bytesRead == contentLength) {
            session.setAttribute("uploadListener", null);
        }
        buffy.append("</response>\n");

        out.println(buffy.toString());
        out.flush();
        out.close();
    }
}
