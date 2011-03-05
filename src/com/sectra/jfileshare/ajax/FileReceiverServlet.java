package com.sectra.jfileshare.ajax;

import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.objects.UserItem;

import java.io.File;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.security.MessageDigest;
import java.security.DigestOutputStream;
import java.util.logging.Level;

import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

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

/**
 * Send an xml response to how the upload is progressing
 *
 * @author markus
 */
public class FileReceiverServlet extends HttpServlet {
    private DataSource ds;
    private String PATH_TEMP;
    private String PATH_STORE;
    private int DAYS_FILE_RETENTION;
    private static final Logger logger =
            Logger.getLogger(FileReceiverServlet.class.getName());
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession();
        FileUploadListener listener = null;
        StringBuilder buffy = new StringBuilder();
        long bytesRead = 0L;
        long contentLength = 0L;
        // logger.info("Upload progress is running");

        // Make sure the session has started
        if (session != null) {
            listener = (FileUploadListener) session.getAttribute("uploadListener");
        }
        resp.setContentType("text/xml;charset=UTF-8");
        buffy.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        buffy.append("<response>\n");

        if (listener == null) {
            buffy.append("\t<bytesRead>0</bytesRead>\n");
            buffy.append("\t<bytesTotal>0</bytesTotal>\n");
        } else {
            bytesRead = listener.getBytesRead();
            contentLength = listener.getContentLength();
            buffy.append("\t<bytesRead>").append(bytesRead).append("</bytesRead>\n");
            buffy.append("\t<bytesTotal>").append(contentLength).append("</bytesTotal>\n");
        }
        buffy.append("</response>\n");

        out.println(buffy.toString());
        out.flush();
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        UserItem currentUser = (UserItem) session.getAttribute("user");
        if (currentUser != null && ServletFileUpload.isMultipartContent(req)) {

            // keep files of up to 10 MiB in memory 10485760
            FileItemFactory factory = new DiskFileItemFactory(10485760, new File(this.PATH_TEMP));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(FILE_SIZE_MAX);
            // logger.info("Max filesize: " + FileItem.humanReadable(FILE_SIZE_MAX));

            // set file upload progress listener
            FileUploadListener listener = new FileUploadListener();
            session.setAttribute("uploadListener", listener);
            upload.setProgressListener(listener);

            try {
                FileItem file = new FileItem();

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
                        if (name.equals("password")
                                && !value.equals("")) {
                            logger.info("Uploaded file has password set");
                            file.setPwPlainText(value);
                        }
                        instream.close();
                    } else {
                        // This is the file you're looking for
                        file.setName(item.getName());
                        file.setType(item.getContentType()==null?"application/octet-stream":item.getContentType());
                        file.setOwnerUid(currentUser.getUid());

                        try {
                            filestream = new FileOutputStream(this.PATH_TEMP + "/" + Integer.toString(currentUser.getUid()));
                            MessageDigest md = MessageDigest.getInstance("MD5");
                            outstream = new DigestOutputStream(filestream, md);
                            long filesize = IOUtils.copyLarge(instream, outstream);

                            if (filesize==0) {
                                throw new Exception("File is empty.");
                            }
                            md = outstream.getMessageDigest();
                            file.setMd5sum(toHex(md.digest()));
                            file.setSize(filesize);

                        } finally {
                            if (outstream != null) {
                                try {
                                    outstream.close();
                                } catch (IOException ignored) {
                                }
                            }
                            if (filestream != null) {
                                try {
                                    filestream.close();
                                } catch (IOException ignored) {
                                }
                            }
                            if (instream != null) {
                                try {
                                    instream.close();
                                } catch (IOException ignored) {
                                }
                            }
                        }
                    }
                }
                /* All done. Save the new file */
                file.setDaysToKeep(DAYS_FILE_RETENTION);

                if (!file.save(ds)) {
                    req.setAttribute("msg", "Unable to contact the database");
                    req.setAttribute("javascript", "parent.uploadComplete('critical');");
                } else {
                    File tempfile = new File(this.PATH_TEMP, Integer.toString(currentUser.getUid()));
                    File finalfile = new File(this.PATH_STORE, Integer.toString(file.getFid()));
                    tempfile.renameTo(finalfile);
                    logger.log(Level.INFO, "User {0} storing file \"{1}\" in the filestore", new Object[]{currentUser.getUid(), file.getName()});
                    req.setAttribute("msg", "File '" + file.getName() + "' uploaded successfully. <a href='" + req.getContextPath() + "/file/edit/" + file.getFid() + "'>Click here to edit file</a>");
                    req.setAttribute("javascript", "parent.uploadComplete('info');");
                }
            } catch (SizeLimitExceededException e) {
                req.setAttribute("msg", "File is too large. The maximum size of file uploads is " + FileItem.humanReadable(FILE_SIZE_MAX));
                req.setAttribute("javascript", "parent.uploadComplete('warning');");
            } catch (FileUploadException e) {
                req.setAttribute("msg", "Unable to upload file");
                req.setAttribute("javascript", "parent.uploadComplete('warning');");
            } catch (Exception e) {
                req.setAttribute("msg", "Unable to upload file. ".concat(e.getMessage()));
                req.setAttribute("javascript", "parent.uploadComplete('warning');");
            } finally {
                session.setAttribute("uploadListener", null);
            }
            ServletContext app = getServletContext();
            RequestDispatcher disp = app.getRequestDispatcher("/templates/AjaxDummy.jsp");
            disp.forward(req, resp);
        }
    }

    /**
     * Convert a byte array to a hex-string. For md5 strings for example
     *
     * @param hashValue Input bytearray
     * @return ascii hexcode representation of input
     */
    private static String toHex(byte[] hashValue) {
        StringBuilder hexString = new StringBuilder();
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
