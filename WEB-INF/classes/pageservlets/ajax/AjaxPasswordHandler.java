package pageservlets.ajax;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import java.io.StringWriter;
import java.io.File;
import java.sql.Connection;

import org.xml.sax.helpers.AttributesImpl;
import utils.CustomLogger;
import http.MultipartRequest;
import objects.UserItem;


/**
 * User: zoran
 * Time: 10:59:44
 * zoran@sectra.se
 */
public class AjaxPasswordHandler implements AjaxSubHandler  {

	public void handle(Connection conn, HttpServletRequest request){
		String ajaxresponse = "";
        UserItem user = (UserItem) request.getSession().getAttribute("user");
        Boolean valid = false;
        String message = "";
        if ( user != null ){

            String password = request.getParameter("password");
            String password2 = request.getParameter("password2");
            if ( password.equals(password2) && password.length() >= 6){
                user.setClearTextPassword(password);
                user.save(conn);
                message = "The password is sucessfully saved";
                valid = true;
            } else {
                message = "The password could not be saved";
            }

        }

        StringWriter sw = new StringWriter();
		try {
			StreamResult streamResult = new StreamResult(sw);
			 SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
			TransformerHandler hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING,"utf-8");
			serializer.setOutputProperty(OutputKeys.INDENT,"yes");
			hd.setResult(streamResult);
			hd.startDocument();
			AttributesImpl atts = new AttributesImpl();
			hd.startElement("","","ajaxresponse",atts);
            String retval = valid.toString();
            hd.startElement("","","status",atts);
            hd.characters(retval.toCharArray(),0,retval.length());
            hd.endElement("","","status");
            hd.startElement("","","message",atts);
            hd.characters(message.toCharArray(),0,message.length());
            hd.endElement("","","message");

            hd.endElement("","","ajaxresponse");
			hd.endDocument();


		}  catch (Exception e){
			CustomLogger.logme(this.getClass().getName(),"Error " + e.toString(),true);
		}
		ajaxresponse = sw.toString();
		CustomLogger.logme(this.getClass().getName(),ajaxresponse);

		request.setAttribute("ajaxresponse",ajaxresponse);
	}


}