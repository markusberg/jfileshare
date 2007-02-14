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

/**
 * User: zoran
 * Time: 10:59:44
 * zoran@sectra.se
 */
public class AjaxStatusHandler implements AjaxSubHandler  {

	public void handle(Connection conn, HttpServletRequest request){
		String ajaxresponse = "";
        String unid = request.getParameter("unid");
        MultipartRequest.SessionData data = (MultipartRequest.SessionData) request.getSession().getAttribute(unid);
        int contentlength = data.getContentLength()-585;
        File file = data.getFile();
        long length = file.length();

        Float result = Float.parseFloat(Long.toString(length))/contentlength*100;

        int procent = result.intValue();

        int remain = contentlength - Integer.parseInt(Long.toString(length));
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
            hd.startElement("","","unid",atts);
            hd.characters(unid.toCharArray(),0,unid.length());
            hd.endElement("","","unid");
            hd.startElement("","","length",atts);
            String lengthstr = Long.toString(length);
            hd.characters(lengthstr.toCharArray(),0,lengthstr.length());
            hd.endElement("","","length");
            hd.startElement("","","total",atts);
            String total = Integer.toString(contentlength);
            hd.characters(total.toCharArray(),0,total.length());
            hd.endElement("","","total");
            hd.startElement("","","remain",atts);
            String remains = Integer.toString(remain);
            hd.characters(remains.toCharArray(),0,remains.length());
            hd.endElement("","","remain");
            hd.startElement("","","status",atts);
            String status = data.getStatus();
            CustomLogger.logme(this.getClass().getName(),"Status: " + status);
            hd.characters(status.toCharArray(),0,status.length());
            hd.endElement("","","status");
            hd.startElement("","","procent",atts);
            String procents = Integer.toString(procent);
            hd.characters(procents.toCharArray(),0,procents.length());
            hd.endElement("","","procent");
            hd.endElement("","","ajaxresponse");
			hd.endDocument();

            /*





            if ( customer == null ){
				String reply = "false";
				hd.startElement("","","success",atts);
				hd.characters(reply.toCharArray(),0,reply.length());
				hd.endElement("","","success");
			} else {
				String reply = "true";
				hd.startElement("","","success",atts);
				hd.characters(reply.toCharArray(),0,reply.length());
				hd.endElement("","","success");
				hd.startElement("","","customer",atts);
				hd.startElement("","","name",atts);
				hd.characters(customer.getName().toCharArray(),0,customer.getName().length());
				hd.endElement("","","name");
				hd.startElement("","","lastname",atts);
				hd.characters(customer.getLastname().toCharArray(),0,customer.getLastname().length());
				hd.endElement("","","lastname");
				hd.startElement("","","address",atts);
				hd.characters(customer.getAddress().toCharArray(),0,customer.getAddress().length());
				hd.endElement("","","address");
				hd.startElement("","","postcode",atts);
				hd.characters(customer.getPostcode().toCharArray(),0,customer.getPostcode().length());
				hd.endElement("","","postcode");
				hd.startElement("","","city",atts);
				hd.characters(customer.getCity().toCharArray(),0,customer.getCity().length());
				hd.endElement("","","city");
				hd.startElement("","","email",atts);
				hd.characters(customer.getEmail().toString().toCharArray(),0,customer.getEmail().toString().length());
				hd.endElement("","","email");
				String telephone = customer.getTelephone()==null?"":customer.getTelephone();
				hd.startElement("","","telephone",atts);
				hd.characters(telephone.toCharArray(),0,telephone.length());
				hd.endElement("","","telephone");
				hd.startElement("","","offeropt",atts);
				String offeropt = customer.wantsOffer()?"true":"false";
				hd.characters(offeropt.toCharArray(),0,offeropt.length());
				hd.endElement("","","offeropt");
				hd.startElement("","","newsopt",atts);
				String newsopt = customer.wantsNewsletter()?"true":"false";
				hd.characters(newsopt.toCharArray(),0,newsopt.length());
				hd.endElement("","","newsopt");
				hd.endElement("","","customer");
			}
			hd.endElement("","","ajaxresponse");
			hd.endDocument();*/
		}  catch (Exception e){
			CustomLogger.logme(this.getClass().getName(),"Error " + e.toString(),true);
		}
		ajaxresponse = sw.toString();
		CustomLogger.logme(this.getClass().getName(),ajaxresponse);

		request.setAttribute("ajaxresponse",ajaxresponse);
	}

	
}


