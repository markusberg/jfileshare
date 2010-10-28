package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.ajax.FileUploadListener;
import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.objects.UserItem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;

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
import org.apache.commons.io.IOUtils;

// import com.sectra.jfileshare.objects.UserItem;
public class FileUploadServlet extends HttpServlet {

    private DataSource ds;
    private String PATH_TEMP;
    private String PATH_STORE;
    private int DAYS_FILE_RETENTION;
    private static final Logger logger =
            Logger.getLogger(FileUploadServlet.class.getName());
    private long FILE_SIZE_MAX;

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            ds = (DataSource) env.lookup("jdbc/jfileshare");

            ServletContext context = getServletContext();
            PATH_TEMP = context.getInitParameter("PATH_TEMP").toString();
            PATH_STORE = context.getInitParameter("PATH_STORE").toString();
            DAYS_FILE_RETENTION = Integer.parseInt(context.getInitParameter("DAYS_FILE_RETENTION").toString());
            FILE_SIZE_MAX = Long.valueOf(context.getInitParameter("FILESIZE_MAX").toString());

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
            FileItemFactory factory = new DiskFileItemFactory(10485760, new File(this.PATH_TEMP));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(FILE_SIZE_MAX);
            // logger.info("Max filesize: " + FileItem.humanReadable(FILE_SIZE_MAX));
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
                FileOutputStream filestream = null;

                while (it.hasNext()) {
                    FileItemStream item = it.next();
                    String name = item.getFieldName();
                    InputStream instream = item.openStream();
                    DigestOutputStream outstream = null;

                    if (item.isFormField()) {
                        String value = Streams.asString(instream);
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
                        instream.close();
                    } else {
                        // This is the file you're looking for
                        oFile.setName(item.getName());
                        oFile.setType(item.getContentType());
                        oFile.setOwnerUid(oCurrentUser.getUid());

                        try {
                            filestream = new FileOutputStream(this.PATH_TEMP + "/" + Integer.toString(oCurrentUser.getUid()));

                            logger.info("Calculating md5 sum");
                            MessageDigest md = MessageDigest.getInstance("MD5");
                            outstream = new DigestOutputStream(filestream, md);
                            long filesize = IOUtils.copyLarge(instream, outstream);

                            md = outstream.getMessageDigest();
                            oFile.setMd5sum(toHex(md.digest()));
                            oFile.setSize(filesize);

                        } finally {
                            if (outstream != null) {
                                outstream.close();
                            }
                            if (filestream != null) {
                                filestream.close();
                            }
                            if (instream != null) {
                                instream.close();
                            }
                        }

                    }
                }
                /* All done. Save the new file */
                if (usepw && !pwPlainText.equals("")) {
                    oFile.setPwPlainText(pwPlainText);
                }
                oFile.setDaysToKeep(DAYS_FILE_RETENTION);

                if (!oFile.save(ds)) {
                    req.setAttribute("message_critical", "Unable to contact the database");
                } else {
                    File tempfile = new File(this.PATH_TEMP, Integer.toString(oCurrentUser.getUid()));
                    File finalfile = new File(this.PATH_STORE, Integer.toString(oFile.getFid()));
                    tempfile.renameTo(finalfile);
                    logger.info("User " + oCurrentUser.getUid() + " storing file \"" + oFile.getName() + "\" in the filestore");
                    req.setAttribute("message", "File \"" + oFile.getName() + "\" uploaded successfully");
                }
                session.setAttribute("uploadListener", null);
            } catch (SizeLimitExceededException e) {
                req.setAttribute("message_critical", "File is too large. The maximum size of file uploads is " + FileItem.humanReadable(FILE_SIZE_MAX) + ".");
            } catch (FileUploadException e) {
                req.setAttribute("message_critical", "Unable to upload file");
                e.printStackTrace();
            } catch (Exception e) {
                req.setAttribute("message_critical", "Unable to upload file");
                e.printStackTrace();
            } finally {
                session.setAttribute("uploadListener", null);
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
