package generic;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.*;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.SQLException;

import config.Config;
import utils.CustomLogger;
import pageservlets.UploadPageHandler;
import pageservlets.DownloadPageHandler;
import pageservlets.RegistrationPageHandler;
import pageservlets.AdminPageHandler;

/**
 * User: zoran@sectra.se
 * Date: 2005-sep-10
 * Time: 16:50:55
 */
public class MainServlet extends HttpServlet {

    Hashtable handlers = new Hashtable(50);
    DataSource datasource;

    private void initHandlers(){

	    handlers.put("0",new UploadPageHandler());
        handlers.put("1",new DownloadPageHandler());
        handlers.put("2",new RegistrationPageHandler());
        handlers.put("3",new AdminPageHandler());

    }


    public void init(ServletConfig config) throws ServletException {
	super.init(config);
	try {
	    Context env = (Context) new InitialContext().lookup("java:comp/env");
	    datasource = (DataSource) env.lookup("jdbc/" + Config.getDb());

	} catch (NamingException e){
	    throw new ServletException(e);
	}
	initHandlers();

    }


    private java.sql.Connection getConnection(String domain,ServletPageRequestHandler handler)
     throws SQLException {




	    if( handler.liveConnection() )
	    {

		return datasource.getConnection();
	    // read only database //


	    }  else {
		return datasource.getConnection();
	    }



    }


    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws
		ServletException, java.io.IOException
    {
	String servername = request.getServerName();
	ServletContext app = getServletContext();
	String includefile = "/templates/noinclude.jsp";
	RequestDispatcher disp = null;
	Connection conn = null;
	String urlPattern = "";


	try {
		boolean handler_done = false;
		urlPattern = request.getServletPath();
		CustomLogger.logme(this.getClass().getName(),"urlPattern: " + urlPattern );

		for ( Enumeration e = this.handlers.elements(); e.hasMoreElements();){
		    ServletPageRequestHandler handler = (ServletPageRequestHandler) e.nextElement();

		    if ( ! handler_done ){
			if ( handler.handleRequest(urlPattern)){
			    CustomLogger.logme(this.getClass().getName(),"MainServlet.processRequest." + handler.getClass().getName() + "=true" );
			    conn = getConnection(request.getServerName(),handler);
			    includefile = handler.handlePageRequest(conn, request, response, app);
			    CustomLogger.logme(this.getClass().getName(),"includefile = " + includefile);
			    handler_done = true;
			} else {
			    CustomLogger.logme(this.getClass().getName(),"MainServlet.processRequest." + handler.getClass().getName() + "=false");
			}
		    }
		  request.setAttribute("urlPattern",urlPattern);


        }
	} catch (SQLException e){
	    CustomLogger.logme(this.getClass().getName(),"Exception happened " + e.toString(),true);

	} finally {
	    try {
		if ( conn != null) {
		    conn.close();
		}
	    } catch (SQLException e){
		CustomLogger.logme(this.getClass().getName(),"SQLException",true);
	    }
	}

	request.setAttribute("urlPattern", urlPattern);
	disp = app.getRequestDispatcher(includefile);
	disp.forward(request,response);
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException{
	processRequest(request,response);
    }


   public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException{
	processRequest(request,response);
    }


}
