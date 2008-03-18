package pageservlets;

import generic.ServletPageRequestHandler;
import pageservlets.ajax.*;
import utils.CustomLogger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * User: zoran@sectra.se
 * Time: 10:56:49
 */
public class AjaxPageHandler implements ServletPageRequestHandler{
    private void log(String msg){
	    Logger.getLogger(this.getClass().getName()).info("[" + this.getClass().toString() + "] " + msg);
    }

    public AjaxPageHandler(){
	    handlers.put("getstatus",new AjaxStatusHandler());
        handlers.put("setunid", new AjaxUploadIdNegotiator());
        handlers.put("sessionadm", new AjaxSessionSetter());
    }


    private Map<String, AjaxSubHandler> handlers = new HashMap<String,AjaxSubHandler>();

    public boolean liveConnection(){
	return true;
    }

    public boolean handleRequest(String urlPattern){

	if ( Pattern.compile("(ajax)").matcher(urlPattern).find()){
	    return true;
	} else {
	    return false;
	}

    }

    public String handlePageRequest(Connection conn, HttpServletRequest request, HttpServletResponse response, ServletContext context)
    throws SQLException, ServletException {
	String urlPattern = (String) request.getAttribute("urlPattern");
	    CustomLogger.logme(this.getClass().getName(),"Request is " + request.getMethod());
	    CustomLogger.logme(this.getClass().getName(),request.getParameter("action")!=null?request.getParameter("action"):"No action");
	    Enumeration params = request.getParameterNames();
	    while (params.hasMoreElements()){
		    String param = (String) params.nextElement();
		    CustomLogger.logme(this.getClass().getName(),"Param " + param + " = " + request.getParameter(param));
	    }
	if ( request.getParameter("action") != null && handlers.containsKey(request.getParameter("action"))){
		log("Running handler for " + request.getParameter("action"));
		handlers.get(request.getParameter("action")).handle(conn,request);
	}
	    CustomLogger.logme(this.getClass().getName(),(String) request.getAttribute("ajaxresponse"));
	return "/templates/ajax/GenericAjax.jsp";

    }


}
