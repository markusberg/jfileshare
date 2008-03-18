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
import java.util.Hashtable;

import objects.UserItem;
import views.UserItemView;

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


    private Hashtable<String,Integer> authorization_map = new Hashtable<String,Integer>();


    public void init(FilterConfig config) throws ServletException {
	try {
	    this.filterconfig = config;
	    Context env = (Context) new InitialContext().lookup("java:comp/env");
	    datasource = (DataSource) env.lookup("jdbc/" + Config.getDb() );
        initAuth();
    } catch (NamingException e){
	    CustomLogger.logme(this.getClass().getName(),"Throwing exception: " + e.toString(),true);
	    throw new ServletException(e);
	}

    }

    /**
     * Initalizes authorization map.
     * The map should take 2 args. One is path, and the other is lowest allowed usertype to access the path
     */
    private void initAuth(){
        this.authorization_map.put("/register",UserItem.TYPE_SECTRA);
        this.authorization_map.put("/upload",UserItem.TYPE_EXTERNAL);
        this.authorization_map.put("/admin",UserItem.TYPE_EXTERNAL);
        this.authorization_map.put("/mainadmin",UserItem.TYPE_ADMIN);
        this.authorization_map.put("/mainadmin/users",UserItem.TYPE_ADMIN);

    }

    public void destroy() {
	//To change body of implemented methods use File | Settings | File Templates.
    }


    private boolean isAuthorised(HttpServletRequest request, UserItem user){
        CustomLogger.logme(this.getClass().getName(),"Got user " + user.getUsername());
        CustomLogger.logme(this.getClass().getName(),"Got path " + request.getServletPath());
        //Get required type:
        /*for ( String path : this.authorization_map.keySet() ){
            CustomLogger.logme(this.getClass().getName(),"Checking for " + path);
            if ( request.getServletPath().startsWith(path)){
                if ( this.authorization_map.get(path) >= user.getUserType() ){
                    CustomLogger.logme(this.getClass().getName(),this.authorization_map.get(path) +">=" + user.getUserType()); 
                    return true;
                } else {
                    CustomLogger.logme(this.getClass().getName(),"Privilege level of " + this.authorization_map.get(path) + " required, got only " + user.getUserType());
                    return false;
                }
            } else {
                CustomLogger.logme(this.getClass().getName(),request.getServletPath() + " does not start with " + path);
            }
        } */

        if ( this.authorization_map.containsKey(request.getServletPath())){
            if ( this.authorization_map.get(request.getServletPath()) >= user.getUserType()){
                CustomLogger.logme(this.getClass().getName(),"Path found, user authorized");
                return true;
            } else {
                CustomLogger.logme(this.getClass().getName(),"Path found, user NOT authorized");
            }


        } else {
            CustomLogger.logme(this.getClass().getName(),"Path NOT found, defaulting to SECTRA-level");
            if ( user.getUserType() <= UserItem.TYPE_SECTRA ){
                CustomLogger.logme(this.getClass().getName(),"User is at least SECTRA-type, allowed");
                return true;
            } else {
                CustomLogger.logme(this.getClass().getName(),"User is not SECTRA-type, NOT ALLOWED");
                return false;
            }
        }

        return false;

    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
	Connection _conn = null;
	try {
	    _conn = datasource.getConnection();
	    if ( CheckUser(_conn,servletRequest)) {

            HttpServletRequest request = (HttpServletRequest)servletRequest;
            HttpSession session = request.getSession();
            HttpServletResponse response = (HttpServletResponse)servletResponse;
            response.setHeader("Pragma", "no-cache");
		    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
		    response.setDateHeader("Expires", -1);
		    response.setDateHeader("Last-Modified", System.currentTimeMillis() - 1000*60*30 );
            UserItem user = (UserItem) session.getAttribute("user");
            if ( user == null ) CustomLogger.logme(this.getClass().getName(),"USER SEAMS TO BE NULL");
            else CustomLogger.logme(this.getClass().getName(),"USER " + user.getUsername() + " is in");
            if ( isAuthorised(request,user) ){
              filterChain.doFilter(servletRequest,servletResponse);
            } else {
                filterconfig.getServletContext().getRequestDispatcher("/templates/NoAccess.jsp").forward(servletRequest,servletResponse);
            }

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
            UserItem user = (UserItem) session.getAttribute("user");
            CustomLogger.logme(this.getClass().getName(),"USER " + user.getUsername() + "(" + user.getUid() + ")" + " IS LOGGED IN");
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
                UserItemView uview = new UserItemView(conn,username);

                user = uview.getUserItem();
                if ( user == null ){
                    return false;
                }
                dbpass = user.getPassword();
                
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
