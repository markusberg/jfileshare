package objects;

import sun.net.smtp.SmtpClient;

import java.io.*;
import java.util.Vector;
import java.util.Properties;
import java.util.Date;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import javax.mail.Multipart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.activation.DataHandler;

import utils.CustomLogger;

/**
 * User: zoran
 * Date: 2005-dec-04
 * Time: 17:11:45
 * zoran@medorian.com
 */
public class EmailItem {
    private String subject = "";
    private String sender = "";
    private String body = "Hi,\n" +
            "You have a file from \n" +
            "00PUSER to download from\n" +
            "http://dude.sectra.se/download/view/00PURL\n" +
            "\n" +
            "Kind Regards\n";
    private String htmlbody = "Hi,<BR />" +
            "You have a file from <BR />" +
            "00PUSER to download from<BR />" +
            "<a href=\"http://dude.sectra.se/download/view/00PURL\">http://dude.sectra.se/download/view/00PURL</a><BR />" +
            "<BR />" +
            "Kind Regards<BR />";

    private Vector<InternetAddress> rcpts = new Vector<InternetAddress>();
    private Vector<InternetAddress> bcpts = new Vector<InternetAddress>();

    public String getHtmlbody() {
    	return this.htmlbody;
    }

    public void setHtmlbody(String htmlbody) {
    	this.htmlbody = htmlbody;
    }

    public Vector<InternetAddress> getRcptsv() {
    	return this.rcpts;
    }

    public void setRcptsv(Vector<InternetAddress> rcpts) {
	    this.rcpts = rcpts;
    }

    public Vector<InternetAddress> getBcptsv() {
	    return bcpts;
    }

    public void setBcptsv(Vector<InternetAddress> bcpts) {
	this.bcpts = bcpts;
    }

    public InternetAddress[] getRcpts(){
        int size = this.rcpts.size();
        InternetAddress[] addresses = new InternetAddress[size];
        addresses = this.rcpts.toArray(addresses);
        return addresses;
    }

    public InternetAddress[] getBcpts(){
        int size = this.bcpts.size();
        InternetAddress[] addresses = new InternetAddress[size];
        addresses = this.bcpts.toArray(addresses);

        return addresses;
    }

    public void addBcpt(InternetAddress bcpt){
	    this.bcpts.add(bcpt);
    }

    public void addRcpt(InternetAddress rcpt){
	    this.rcpts.add(rcpt);
    }

    public EmailItem() {
	    this.sender = "dude@sectra.se";
    }

    public EmailItem(String sender){
        this.sender = sender;
    }

    public String getSubject() {
	    return this.subject;
    }

    public void setSubject(String subject) {
	this.subject = subject;
    }

    public String getSender() {
	return sender;
    }

    public void setSender(String sender) {
	this.sender = sender;
    }

    public String getBody() {
	    return body;
    }

    public void setBody(String body) {
	    this.body = body;
    }

    public void setHTMLBody(String body, String url){

	    this.htmlbody = body.replaceAll("00PURL",url);

    }

    public void setUrl(String url){
        this.htmlbody = htmlbody.replaceAll("00PURL",url).replaceAll("00PUSER",this.sender);
        this.body = this.body.replaceAll("00PURL",url).replaceAll("00PUSER",this.sender);
    }





    public void sendHTMLMail(){


		Properties props = new Properties();
		boolean debug = false;
		Session ssession = Session.getInstance(props,null);
		ssession.setDebug(debug);
		try {
		    MimeMessage msg = new MimeMessage(ssession);
		    msg.setFrom(new InternetAddress(this.sender));
		    msg.setRecipients(Message.RecipientType.TO,getRcpts());
		    msg.setRecipients(Message.RecipientType.BCC,getBcpts());
		    msg.setSubject(subject);
		    msg.setSentDate(new Date());
		    MimeBodyPart part1 = new MimeBodyPart();
		    part1.setText(body);

		    MimeBodyPart part2 = new MimeBodyPart();
		    part2.setDataHandler(new DataHandler(htmlbody,"text/html;charset=utf-8"));


		    Multipart mp = new MimeMultipart("alternative");

		    mp.addBodyPart(part1);
		    mp.addBodyPart(part2);

		    msg.setContent(mp);
		    Transport.send(msg);


		} catch (Exception e){
		    CustomLogger.logme(this.getClass().getName(),"Exception in EmailItem.java sendHTMLMail() " + e.toString(),true);
		}





    }
}
