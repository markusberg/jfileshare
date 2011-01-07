package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.objects.NoSuchFileException;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
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

        Integer fid = Integer.parseInt(req.getPathInfo().substring(1));
        String md5sum = req.getParameter("md5");
        try {
            FileItem file = new FileItem(ds, fid);
            File fileOnDisk = new File(pathFileStore + "/" + file.getFid().toString());

            logger.info("Preparing to stream file");
            resp.setContentType(file.getType());
            resp.setHeader("Content-disposition", "attachment; filename=\"" + file.getName() + "\"");
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
            String ipAddr = req.getRemoteAddr();
            file.logDownload(ds, ipAddr);
        } catch (NoSuchFileException ignore) {
        } catch (SQLException ignore) {
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, java.io.IOException {
        doGet(req, resp);
    }
}
