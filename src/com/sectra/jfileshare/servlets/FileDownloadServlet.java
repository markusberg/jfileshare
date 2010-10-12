package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.FileItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import java.sql.SQLException;
import java.sql.Connection;

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

public class FileDownloadServlet extends HttpServlet {

    private DataSource ds;
    private String pathFileStore;
    private static final Logger logger =
            Logger.getLogger(FileDownloadServlet.class.getName());

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);
        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            ds = (DataSource) env.lookup("jdbc/jfileshare");
            pathFileStore = getServletContext().getInitParameter("PATH_STORE").toString();

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

        String PathInfo = req.getPathInfo().substring(1);
        String md5sum = PathInfo.split("_SECTRA_")[0];
        int iFid = Integer.parseInt(PathInfo.split("_SECTRA_")[1]);

        FileItem oFile = null;

        oFile = new FileItem(ds, iFid);

        File fileOnDisk = new File(pathFileStore + "/" + Integer.toString(oFile.getFid()));

        logger.info("Preparing to stream file");
        resp.setContentType(oFile.getType());
        resp.setHeader("Content-disposition", "attachment; filename=\"" + oFile.getName() + "\"");
        resp.setHeader("Content-length", Long.toString(fileOnDisk.length()));

        ServletOutputStream sos = resp.getOutputStream();
        FileInputStream fis = new FileInputStream(fileOnDisk);
        BufferedInputStream bis = new BufferedInputStream(fis);
        BufferedOutputStream bos = new BufferedOutputStream(sos);

        try {
            int c;
            byte[] buffersize = new byte[2048];
            while ((c = bis.read(buffersize)) != -1) {
                bos.write(buffersize, 0, c);
            }
        } finally {
            if (bos != null) {
                bos.close();
            }
            if (bis != null) {
                bis.close();
            }
            if (fis != null) {
                fis.close();
            }
            if (sos != null) {
                sos.close();
            }
        }
        String ipAddr = req.getRemoteAddr();
        oFile.logDownload(ds, ipAddr);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, java.io.IOException {
        doGet(req, resp);
    }
}
