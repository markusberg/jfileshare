package pageservlets.ajax;

import org.xml.sax.helpers.AttributesImpl;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import java.sql.Connection;
import java.io.StringWriter;
import java.io.File;
import java.io.IOException;

import utils.CustomLogger;
import http.Exceptions.MultipartRequestException;
import http.Exceptions.UploadDirectoryException;
import config.Config;

/**
 * User: zoran
 * Date: Jan 31, 2007
 * Time: 9:41:02 AM
 */
public class AjaxUploadIdNegotiator implements AjaxSubHandler {

    private File mUploadDirectory = null;
    private File tmp_file = null;

    private void checkUploadDirectory(){
        mUploadDirectory = new File(Config.getUdir());
        mUploadDirectory.mkdirs();

        if(!mUploadDirectory.exists() || !mUploadDirectory.isDirectory() || !mUploadDirectory.canWrite()) {
            CustomLogger.logme(this.getClass().getName(),"DIRECTORY NOT EXISTENT!!!! ", true);
        }
    }


    private String createTmpFile(){
        try {
            tmp_file = File.createTempFile("upl", ".tmp", mUploadDirectory);
        } catch (IOException e) {
            CustomLogger.logme(this.getClass().getName(), e.toString(), true);
        }

        return tmp_file.getAbsolutePath();
    }


    public void handle(Connection conn, HttpServletRequest request) {
        checkUploadDirectory();
        String filename = createTmpFile();
        String ajaxresponse = "";
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
            hd.startElement("","","status",atts);
            boolean statusset = false;
            String unid = null;
            if ( request.getParameter("unid") != null && ! request.getParameter("unid").equals("")){
                unid = request.getParameter("unid");
                request.getSession().setAttribute(unid,filename);
                hd.characters("OK".toCharArray(),0,"OK".length());
                statusset = true;
            } else {
                hd.characters("FAIL".toCharArray(),0,"FAIL".length());
            }
            hd.endElement("","","status");
            if ( statusset ){
                hd.startElement("","","unid",atts);
                hd.characters(unid.toCharArray(),0,unid.length());
                hd.endElement("","","unid");
                hd.startElement("","","filename",atts);
                hd.characters(filename.toCharArray(),0,filename.length());
                hd.endElement("","","filename");
            }
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
