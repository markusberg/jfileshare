package generic;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *  Servlet Dispatcher.
 *
 *@author     Maiyk Ltd
 *@created    March 20, 2003
 */
public interface ServletPageRequestHandler {

	// forwarder //

	/**
	 *  This Function is forwarding the request from the
	 *   Servlet to the Handler

	 *@param  conn      Java SQL Connecton.
	 *@param  request   Description of the Parameter
	 *@param  response  Description of the Parameter
	 *@return           Description of the Return Value
	 */
	public String handlePageRequest(Connection conn,
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    ServletContext context)

	throws SQLException,ServletException;

	public boolean handleRequest(String urlPattern) ;

    public boolean liveConnection() ;

}
