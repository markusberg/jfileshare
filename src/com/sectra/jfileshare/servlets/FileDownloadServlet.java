package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.Conf;
import com.sectra.jfileshare.objects.FileItem;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;

public class FileDownloadServlet extends HttpServlet {

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(FileDownloadServlet.class.getName());

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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, java.io.IOException {
        // By the time we get here, the fileauthfilter has done the sanity
        // checking and authentication already. We can jump right into
        // serving the file.
        Conf conf = (Conf) getServletContext().getAttribute("conf");
        FileItem file = (FileItem) req.getAttribute("file");
        File fileOnDisk = new File(conf.getPathStore() + "/" + file.getFid().toString());

        logger.info("Preparing to stream file");
        resp.setContentType(file.getType());
        String disposition = req.getServletPath().equals("/file/get") && "image".equals(file.getType().substring(0,5)) ? "inline" : "attachment";
        resp.setHeader("Content-disposition", disposition+"; filename=\"" + file.getName() + "\"");
        resp.setHeader("Content-length", Long.toString(fileOnDisk.length()));

        FileInputStream instream = new FileInputStream(fileOnDisk);
        ServletOutputStream outstream = resp.getOutputStream();

        try {
            IOUtils.copyLarge(instream, outstream);
        } finally {
            if (instream != null) {
                instream.close();
            }
            if (outstream != null) {
                outstream.close();
            }
        }
        file.logDownload(ds, req.getRemoteAddr());
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, java.io.IOException {
        doGet(req, resp);
    }
}
