/**
 *  Copyright 2011 SECTRA Imtec AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * @author      Markus Berg <markus.berg @ sectra.se>
 * @version     1.6
 * @since       2011-09-21
 */
package com.sectra.jfileshare.ajax;

import com.sectra.jfileshare.objects.Conf;
import com.sectra.jfileshare.objects.FileItem;
import com.sectra.jfileshare.objects.UserItem;
import com.sectra.jfileshare.utils.Helpers;

import java.io.File;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.security.MessageDigest;
import java.security.DigestOutputStream;
// import java.text.DecimalFormat;
// import java.text.NumberFormat;
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

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
    static final long serialVersionUID = 1L;

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(FileReceiverServlet.class.getName());

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
            throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession();

        resp.setContentType("text/xml;charset=UTF-8");

        FileUploadListener listener = (FileUploadListener) session.getAttribute("uploadListener");
        if (listener == null) {
            listener = new FileUploadListener();
        }

        try {
            JAXBContext context = JAXBContext.newInstance(FileUploadListener.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(listener, out);
        } catch (Exception ignore) {
        }
        out.flush();
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        UserItem currentUser = (UserItem) session.getAttribute("user");
        if (currentUser != null && ServletFileUpload.isMultipartContent(req)) {
            Conf conf = (Conf) getServletContext().getAttribute("conf");
            // keep files of up to 10 MiB in memory 10485760
            FileItemFactory factory = new DiskFileItemFactory(10485760, new File(conf.getPathTemp()));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(conf.getFileSizeMax());

            // set file upload progress listener
            FileUploadListener listener = new FileUploadListener();
            session.setAttribute("uploadListener", listener);
            upload.setProgressListener(listener);

            File tempFile = File.createTempFile(String.format("%05d-", currentUser.getUid()), null, new File(conf.getPathTemp()));
            tempFile.deleteOnExit();
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
                        file.setType(item.getContentType() == null ? "application/octet-stream" : item.getContentType());
                        file.setOwnerUid(currentUser.getUid());

                        try {
                            filestream = new FileOutputStream(tempFile);
                            MessageDigest md = MessageDigest.getInstance("MD5");
                            outstream = new DigestOutputStream(filestream, md);
                            long filesize = IOUtils.copyLarge(instream, outstream);

                            if (filesize == 0) {
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
                if (conf.getDaysFileExpiration() != 0) {
                    file.setDaysToKeep(conf.getDaysFileExpiration());
                }
                if (file.create(ds, req.getRemoteAddr())) {
                    File finalFile = new File(conf.getPathStore(), Integer.toString(file.getFid()));
                    tempFile.renameTo(finalFile);
                    logger.log(Level.INFO, "User {0} storing file \"{1}\" in the filestore", new Object[]{currentUser.getUid(), file.getName()});
                    req.setAttribute("msg", "File <strong>\"" + Helpers.htmlSafe(file.getName()) + "\"</strong> uploaded successfully. <a href='" + req.getContextPath() + "/file/edit/" + file.getFid() + "'>Click here to edit file</a>");
                    req.setAttribute("javascript", "parent.uploadComplete('info');");
                } else {
                    req.setAttribute("msg", "Unable to contact the database");
                    req.setAttribute("javascript", "parent.uploadComplete('critical');");
                }
            } catch (SizeLimitExceededException e) {
                tempFile.delete();
                req.setAttribute("msg", "File is too large. The maximum size of file uploads is " + FileItem.humanReadable(conf.getFileSizeMax()));
                req.setAttribute("javascript", "parent.uploadComplete('warning');");
            } catch (FileUploadException e) {
                tempFile.delete();
                req.setAttribute("msg", "Unable to upload file");
                req.setAttribute("javascript", "parent.uploadComplete('warning');");
            } catch (Exception e) {
                tempFile.delete();
                req.setAttribute("msg", "Unable to upload file. ".concat( e.getMessage() == null ? "" : e.getMessage()));
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
