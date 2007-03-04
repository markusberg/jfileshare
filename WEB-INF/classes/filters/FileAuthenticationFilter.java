package filters;

import config.Config;

import javax.servlet.*;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;

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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
