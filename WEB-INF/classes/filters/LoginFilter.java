package filters;

import config.Config;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.sql.DataSource;

import utils.CustomLogger;
import utils.Jcrypt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.io.IOException;

import objects.UserItem;

/**
 * Created by Zoran Pucar zoran@medorian.com.
 * User: zoran
 * Date: 2007-jan-06
 * Time: 14:32:27
 * This is copyrighted software. If you got hold
 * of this software by unauthorized means, please
 * contact us at email above.
 */
public class LoginFilter implements Filter {
    private FilterConfig filterconfig;
    private DataSource datasource;

    public void init(FilterConfig config) throws ServletException {
	try {
	    this.filterconfig = config;
	    Context env = (Context) new InitialContext().lookup("java:comp/env");
	    datasource = (DataSource) env.lookup("jdbc/" + Config.getDb() );
	} catch (NamingException e){
	    CustomLogger.logme(this.getClass().getName(),"Throwing exception: " + e.toString(),true);
	    throw new ServletException(e);
	}

    }

    public void destroy() {
	//To change body of implemented methods use File | Settings | File Templates.
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
	Connection _conn = null;
	try {
	    _conn = datasource.getConnection();
	    if ( CheckUser(_conn,servletRequest)) {
		      filterChain.doFilter(servletRequest,servletResponse);

	    }  else {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            HttpServletRequest request = (HttpServletRequest)servletRequest;
            if ( ! request.isSecure() && Config.loginRequiresHttps() ){
                request.setAttribute("address","https://" + request.getServerName() + request.getServletPath());
                filterconfig.getServletContext().getRequestDispatcher("/templates/Forward.jsp").forward(servletRequest,servletResponse);
            }
            response.setHeader("Pragma", "no-cache");
		    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
		    response.setDateHeader("Expires", -1);
		    response.setDateHeader("Last-Modified", System.currentTimeMillis() - 1000*60*30 );
             request.setAttribute("urlPattern",request.getServletPath());
             filterconfig.getServletContext().getRequestDispatcher("/templates/Login.jsp").forward(servletRequest,servletResponse);
	    }
	    _conn.close();
	} catch (java.sql.SQLException e){
	    CustomLogger.logme(this.getClass().getName(),"Could not get connection " + e.toString(),true);

	} finally {
	    try {
		_conn.close();
	    } catch (Exception e){

	    }
	}

    }


    private boolean CheckUser(Connection conn, ServletRequest request){
	    HttpServletRequest req = (HttpServletRequest) request;
	    HttpSession session = req.getSession(true);
	    //First check if we are logged in
	    if ( session.getAttribute("user") != null ){
            CustomLogger.logme(this.getClass().getName(),"USER " + ((UserItem)session.getAttribute("user")).getUsername() + " IS LOGGED IN");
            return true;
        }

        // Second, check if we are logging in right now
	    if ( request.getParameter("submit") != null && request.getParameter("username") != null && request.getParameter("password") != null ){
			String username = request.getParameter("username");
			String password = request.getParameter("password");

			String dbuser = null;
			String dbpass = null;
            String email = null;
            UserItem user = new UserItem();
            ResultSet result = null;
			if ( username != null && password != null ){
			    try {
				PreparedStatement st = conn.prepareStatement("select * from UserItems where username=?");
				    st.setString(1,username);
				result = st.executeQuery();
				while ( result.next()) {
				    dbuser = result.getString("username");
				    dbpass = result.getString("password");
                    if ( dbuser != null ){
                        user.setUsername(dbuser);
                        user.setPassword(dbpass);
                        user.setUid(result.getInt("uid"));
                        user.setEmail(result.getString("email"));
                    }
                }
                result.close();
                    st.close();
                } catch (java.sql.SQLException e){
				    CustomLogger.logme(this.getClass().getName(),e.toString(),true);
			    }
				CustomLogger.logme(this.getClass().getName(),"Got password *******");
                //Jcrypt.crypt(dbpass,password).equals(dbpass)
                //if ( password.equals(dbpass)){
                String encrypted = Jcrypt.crypt(dbpass,password);
                if ( encrypted.equals(dbpass) ){
                    session.setAttribute("user",user);
                    //Save lastlogin-date
                    
                    return true;

				}  else {
                    CustomLogger.logme(this.getClass().getName(),"DB pass " + dbpass + " doesn't macth " + encrypted);
                    return false;
				}

		}






	}


	return false;
    }
}
