package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.ajax.FileUploadListener;
import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.objects.UserItem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;

import java.sql.SQLException;
import java.sql.Connection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.security.MessageDigest;
import java.security.DigestOutputStream;

import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;

import javax.sql.DataSource;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

// import com.sectra.jfileshare.objects.UserItem;
public class FileUploadServlet extends HttpServlet {

    private DataSource ds;
    private String pathTemp;
    private String pathStore;
    private int daysFileRetention;
    private static final Logger logger =
            Logger.getLogger(FileUploadServlet.class.getName());
    private long filesizeMax;

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            ds = (DataSource) env.lookup("jdbc/jfileshare");

            ServletContext context = getServletContext();
            pathTemp = context.getInitParameter("PATH_TEMP").toString();
            pathStore = context.getInitParameter("PATH_STORE").toString();
            daysFileRetention = Integer.parseInt(context.getInitParameter("DAYS_FILE_RETENTION").toString());
            filesizeMax = Long.valueOf(context.getInitParameter("FILESIZE_MAX").toString());

        } catch (NamingException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletContext app = getServletContext();
        RequestDispatcher disp = app.getRequestDispatcher("/templates/FileUpload.jsp");
        disp.forward(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (req.getParameter("action") != null
                && req.getParameter("action").equals("login")) {
            // This POST is the result of a login
            doGet(req, resp);
        } else if (ServletFileUpload.isMultipartContent(req)) {
            HttpSession session = req.getSession();

            // keep files of up to 10 MiB in memory 10485760
            FileItemFactory factory = new DiskFileItemFactory(10485760, new File(this.pathTemp));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(filesizeMax);
            logger.info("Max filesize: " + FileItem.humanReadable(filesizeMax));
            UserItem oCurrentUser = (UserItem) session.getAttribute("user");

            // set file upload progress listener
            FileUploadListener listener = new FileUploadListener();
            session.setAttribute("uploadListener", listener);
            upload.setProgressListener(listener);

            // FileItem tempFileItemObject = null;
            boolean usepw = false;
            String pwPlainText = "";

            try {
                FileItem oFile = new FileItem();

                /* iterate over all uploaded items */
                FileItemIterator it = upload.getItemIterator(req);
                FileOutputStream outfile = null;

                while (it.hasNext()) {
                    FileItemStream item = it.next();
                    String name = item.getFieldName();
                    InputStream stream = item.openStream();

                    if (item.isFormField()) {
                        String value = Streams.asString(stream);
                        // logger.info(name + " : " + value);
                        /* not the file upload. Maybe the password field? */
                        if (name.equals("usepw")
                                && value.equals("true")) {
                            usepw = true;
                        }

                        if (name.equals("password")
                                && !value.equals("")) {
                            logger.info("Uploaded file has password set");
                            pwPlainText = value;
                        }
                        stream.close();
                    } else {
                        // This is the file you're looking for
                        oFile.setName(item.getName());
                        oFile.setType(item.getContentType());
                        oFile.setOwnerUid(oCurrentUser.getUid());

                        try {
                            outfile = new FileOutputStream(this.pathTemp + "/" + Integer.toString(oCurrentUser.getUid()));

                            logger.info("Calculating md5 sum");
                            MessageDigest md = MessageDigest.getInstance("MD5");
                            DigestOutputStream din = new DigestOutputStream(outfile, md);

                            int c;
                            byte[] argle = new byte[4096];
                            long size = 0L;
                            while ((c = stream.read(argle)) != -1) {
                                din.write(argle, 0, c);
                                size += c;
                            }
                            md = din.getMessageDigest();
                            oFile.setMd5sum(toHex(md.digest()));
                            oFile.setSize(size);

                        } finally {
                            if (stream != null) {
                                stream.close();
                            }
                            if (outfile != null) {
                                outfile.close();
                            }
                        }

                    }
                }
                /* All done. Save the new file */
                if (usepw && !pwPlainText.equals("")) {
                    oFile.setPwPlainText(pwPlainText);
                }
                oFile.setDaysToKeep(daysFileRetention);
                oFile.save(ds);

                if (1 == 0) {
                    req.setAttribute("message_critical", "Unable to connect to database. Please contact your system administrator.");
                }

                File tempfile = new File(this.pathTemp, Integer.toString(oCurrentUser.getUid()));
                File finalfile = new File(this.pathStore, Integer.toString(oFile.getFid()));
                boolean success = tempfile.renameTo(finalfile);

                if (success) {
                    logger.info("User " + oCurrentUser.getUid() + " storing file \"" + oFile.getName() + "\" in the filestore");
                    req.setAttribute("message", "File \"" + oFile.getName() + "\" uploaded successfully");
                } else {
                    logger.info("Failed to move uploaded file to filestore");
                    req.setAttribute("message_critical", "Unable to store the file in the filestore");
                }
                session.setAttribute("uploadListener", null);
            } catch (SizeLimitExceededException e) {
                req.setAttribute("message_critical", "File is too large. The maximum size of file uploads is " + FileItem.humanReadable(filesizeMax) + ".");
            } catch (FileUploadException e) {
                req.setAttribute("message_critical", "Unable to upload file");
                e.printStackTrace();
            } catch (Exception e) {
                req.setAttribute("message_critical", "Unable to upload file");
                e.printStackTrace();
            }
            ServletContext app = getServletContext();
            RequestDispatcher disp = app.getRequestDispatcher("/templates/FileUpload.jsp");

            disp.forward(req, resp);
        }
    }

    /**
     * Convert a byte array to a hex-string. For md5 strings for example
     *
     * @param hashValue Input bytearray
     * @return ascii hexcode representation of input
     */
    public static String toHex(byte[] hashValue) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hashValue.length; i++) {
            String hex = Integer.toHexString(0xFF & hashValue[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
