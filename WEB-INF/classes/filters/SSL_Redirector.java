package filters;

import utils.CustomLogger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * SECTRA.
 * User: zoran
 * Date: Mar 5, 2007
 * Time: 9:55:02 AM
 */
public class SSL_Redirector implements Filter {
    private FilterConfig _filterconfig;

    public void init(FilterConfig filterConfig) throws ServletException {
        this._filterconfig = filterConfig;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
       if ( ! servletRequest.isSecure() && config.Config.allwaysForceHttps() ){
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            CustomLogger.logme(this.getClass().getName(),"Redirecting to SSL");
            request.setAttribute("address","https://" + request.getServerName() + request.getServletPath());
            _filterconfig.getServletContext().getRequestDispatcher("/templates/Forward.jsp").forward(servletRequest,servletResponse);

        } else {
           filterChain.doFilter(servletRequest,servletResponse);
       }
    }

    public void destroy() {
        
    }
}
