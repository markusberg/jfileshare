package filters;

import config.Config;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.HashSet;

import utils.CustomLogger;

/**
 * User: zoran
 * Date: 2007-mar-04
 * Time: 09:09:04
 */
public class FileAuthenticationFilter implements Filter {
    private FilterConfig filterconfig;
    private DataSource datasource;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterconfig = filterConfig;
        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            datasource = (DataSource) env.lookup("jdbc/" + Config.getDb() );
        } catch (NamingException e){
            CustomLogger.logme(this.getClass().getName(),"Throwing exception: " + e.toString(),true);
            throw new ServletException(e);
        }
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Connection _conn = null;
        try {
            _conn = datasource.getConnection();
            if ( ! authenticated(_conn,servletRequest) ){
               filterconfig.getServletContext().getRequestDispatcher("/templates/FilePassword.jsp").forward(servletRequest,servletResponse); 
            } else {
                filterChain.doFilter(servletRequest,servletResponse);
            }
            _conn.close();
        } catch (SQLException e) {
            CustomLogger.logme(this.getClass().getName(),e.toString(),true);
        } finally {
	    try {
            if ( _conn != null ) _conn.close();
	    } catch (Exception e){
            CustomLogger.logme(this.getClass().getName(),"THIS IS WEIRD");

        }
	}
    }

    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    private boolean authenticated(Connection conn,ServletRequest servletRequest){
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpSession session = request.getSession();
        String[] pathparts = request.getServletPath().split("/");
        String lastpart = pathparts[pathparts.length - 1 ];
        //First, are we authenticated for this file
        if ( session.getAttribute("authfiles") != null ){
            Set<String> authfiles = (Set<String>) session.getAttribute("authfiles");

            if ( authfiles.contains(lastpart)){
                CustomLogger.logme(this.getClass().getName(),"File already authenticated");
                return true;
            }
        }

        //Then check if we need to authenticate
        String password = getPassword(conn,lastpart);
        if (  password == null ){
            CustomLogger.logme(this.getClass().getName(),"File does not require authentication");
            return true;
        } else {
            //We need to authenticate, so are we logging in?
            if ( request.getParameter("sendpassword") != null ){
                //We are logging in.. verify the password.
                if ( utils.Jcrypt.crypt(password,request.getParameter("password")).equals(password)){
                    CustomLogger.logme(this.getClass().getName(),"Saving " + lastpart + " to authfiles in session ");
                    if ( session.getAttribute("authfiles") != null ){
                        Set<String> authfiles = (Set<String>) session.getAttribute("authfiles");
                        authfiles.add(lastpart);
                        session.setAttribute("authfiles",authfiles);
                    } else {
                        Set<String> authfiles = new HashSet<String>();
                        authfiles.add(lastpart);
                        session.setAttribute("authfiles",authfiles);

                    }
                    return true;
                } else {
                    return false;
                }
            }
        }


        CustomLogger.logme(this.getClass().getName(),"Lastpart is " + lastpart);
        //Lastpart is md5sum_SECTRA_fid


        return false;
    }

    private String getPassword(Connection conn, String url){
        String md5sum = url.split("_SECTRA_")[0];
        int fid = Integer.parseInt(url.split("_SECTRA_")[1]);
        String password = null;
        try {
            PreparedStatement st = conn.prepareStatement("SELECT password FROM FileItems where md5sum=? and fid=?");
            st.setString(1,md5sum);
            st.setInt(2,fid);
            ResultSet rs = st.executeQuery();
            while ( rs.next()){
                password = rs.getString("password");
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            CustomLogger.logme(this.getClass().getName(),e.toString(),true);
        }
        if ( password == null || password.length() == 0 ){
            return null;
        }
        return password;
    }
}
