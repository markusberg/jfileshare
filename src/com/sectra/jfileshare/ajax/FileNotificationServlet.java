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
import com.sectra.jfileshare.objects.NoSuchFileException;
import com.sectra.jfileshare.objects.UserItem;
import gnu.mail.providers.smtp.SMTPTransport;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.SQLException;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.sql.DataSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

// import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FileNotificationServlet extends HttpServlet {
    static final long serialVersionUID = 1L;

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(FileNotificationServlet.class.getName());

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
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/xml; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession();
        UserItem currentUser = (UserItem) session.getAttribute("user");

        String status;
        String message = "";
        String stacktrace = "";

        try {
            int iFid = Integer.parseInt(req.getParameter("iFid"));
            String emailRecipient = req.getParameter("emailRecipient");
            FileItem file = new FileItem();
            file.fetch(ds, iFid);

            // You must have edit access to the file in order to notify
            // someone of it's existence
            InternetAddress emailValidated = new InternetAddress();
            if (!currentUser.hasEditAccessTo(file)) {
                throw new Exception("You do not have admin access to that file");
            }
            // Email address sanity check
            emailValidated = new InternetAddress(emailRecipient);
            emailValidated.validate();

            // Everything checks out. Let's send the email notification
            sendEmailNotification(file, currentUser, emailValidated);
            status = "info";
            message = "Email notification has been sent to <strong>"
                    + emailRecipient + "</strong> regarding the file "
                    + "<strong>\"" + file.getName() + "\"</strong>";
        } catch (NoSuchFileException e) {
            status = "warning";
            message = e.getMessage();
        } catch (SQLException e) {
            status = "critical";
            message = e.getMessage();
        } catch (AddressException e) {
            status = "warning";
            message = "Unable to send email. " + e.getMessage();
        } catch (MessagingException e) {
            logger.log(Level.WARNING, "Unable to send notification email: {0}", e.toString());
            status = "critical";
            message = "Failed to send email notification."
                    + "Reason unknown. Please try again later, or contact "
                    + "the server administrator.";
            stacktrace = e.toString();
        } catch (NullPointerException ignore) {
            // This will happen if there's no currentUser object
            // We'll just send a sessionexpired-message to the UA to
            // trigger a logout.
            status = "sessionexpired";
        } catch (Exception e) {
            status = "warning";
            message = "Unknown error";
            stacktrace = e.toString();
        }

        try {
            // Create xml response document
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = (Element) doc.createElement("notificationResponse");
            doc.appendChild(rootElement);

            Element xmlStatus = doc.createElement("status");
            xmlStatus.appendChild(doc.createTextNode(status));
            rootElement.appendChild(xmlStatus);

            Element xmlMessage = doc.createElement("msg");
            xmlMessage.appendChild(doc.createTextNode(message));
            rootElement.appendChild(xmlMessage);

            Element xmlStacktrace = doc.createElement("stacktrace");
            xmlStacktrace.appendChild(doc.createTextNode(stacktrace));
            rootElement.appendChild(xmlStacktrace);

            // stream xml content to client
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (ParserConfigurationException pce) {
        } catch (TransformerException tfe) {
        }

        out.flush();
        out.close();
    }

    private void sendEmailNotification(FileItem file,
            UserItem currentUser,
            InternetAddress emailRecipient)
            throws MessagingException, SendFailedException {
        Conf conf = (Conf) getServletContext().getAttribute("conf");

        Properties props = System.getProperties();
        // props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", conf.getSmtpServer());
        props.put("mail.smtp.port", ((Integer) conf.getSmtpServerPort()).toString());
        props.put("mail.smtp.reportsuccess", "true");
        Session session = Session.getInstance(props, null);

        MimeMessage msg = new MimeMessage(session);
        msg.setSender(new InternetAddress(currentUser.getEmail()));
        msg.setRecipient(Message.RecipientType.TO, emailRecipient);
        msg.setFrom(conf.getSmtpSender());
        msg.setSubject("File " + file.getName() + " available for download");

        MimeMultipart mp = new MimeMultipart();
        mp.setSubType("alternative");

        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText(currentUser.getUsername() + " has made "
                + "the following file available for download:\n"
                + "Filename: " + file.getName() + "\n"
                + "Filesize: " + FileItem.humanReadable(file.getSize()) + "\n\n"
                + file.getURL(conf.getBaseUrl())
                + (file.getDateExpiration() == null ? "" : "\n(note: this link will expire in " + file.getDaysUntilExpiration() + (file.getDaysUntilExpiration() == 1 ? " day)" : " days)")), "utf-8");

        MimeBodyPart mbp2 = new MimeBodyPart();
        mbp2.setContent("<h1>File available for download</h1>"
                + "<p><strong>" + currentUser.getUsername() + "</strong> has made the following \n"
                + "file available for download:</p>\n"
                + "<table>\n"
                + "<tr><th style=\"text-align: right;\">Filename:</th><td>" + file.getName() + "</td></tr>\n"
                + "<tr><th style=\"text-align: right;\">Filesize:</th><td>" + FileItem.humanReadable(file.getSize()) + "</td></tr>\n"
                + "</table>\n"
                + "<p><a href=\"" + file.getURL(conf.getBaseUrl()) + "\">" + file.getURL(conf.getBaseUrl()) + "</a>\n"
                + (file.getDateExpiration() == null ? "" : "<br/>(note: this link will expire in " + file.getDaysUntilExpiration() + (file.getDaysUntilExpiration() == 1 ? " day)" : " days)"))
                + "</p>\n", "text/html; charset=utf-8");

        /* Possibly attach image to make it look nicer
        mbp3 = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        mbp3.setDataHandler(new DataHandler(source));
        mbp3.setHeader("Content-ID", "<jfilesharelogo>);
         * Använd <img src="cid:jfilesharelogo" />
         *
         *  mp.setSubType("alternative");
         *
         */

        mp.addBodyPart(mbp1);
        mp.addBodyPart(mbp2);

        // mp.addBodyPart(mbp3);

        msg.setContent(mp);
        SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
        transport.connect();
        transport.sendMessage(msg, msg.getAllRecipients());
        transport.close();

        // Transport.send(msg);

        logger.log(Level.INFO, "Sending email notification to {0}", emailRecipient.getAddress());
    }
}
