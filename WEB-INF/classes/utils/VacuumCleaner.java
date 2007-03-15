package utils;

import config.Config;

import javax.servlet.*;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Set;

import views.UserItemView;
import objects.UserItem;

/**
 * User: zoran@sectra.se
 * Date: Mar 8, 2007
 * Time: 9:12:14 AM
 */
public class VacuumCleaner extends GenericServlet {
    DataSource datasource;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            datasource = (DataSource) env.lookup("jdbc/" + Config.getDb());

        } catch (NamingException e){
            throw new ServletException(e);
        }


    }

    private java.sql.Connection getConnection()
     throws SQLException {
	    return datasource.getConnection();
    }


    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        Connection conn = null ;
        try {
            conn = getConnection();
            CustomLogger.logme(this.getClass().getName(),"Testing vor VACUUM");
            UserItemView eview = new UserItemView();
            Set<UserItem> uzers = eview.getExpiredUsers(conn);
            CustomLogger.logme(this.getClass().getName(),"UZERS " + uzers.size());
            for ( UserItem user: uzers ){
                CustomLogger.logme(this.getClass().getName(),"Deleting user " + user.getUsername() + " (" + user.getUid() + ")");
                user.delete(conn);
            }






            conn.close();
        } catch (SQLException e) {
            CustomLogger.logme(this.getClass().getName(), e.toString(), true);
        } finally {
            try {
                if ( conn != null ) conn.close();
            } catch ( SQLException e ){
                CustomLogger.logme(this.getClass().getName(),e.toString(),true);
            }
        }

    }
}
