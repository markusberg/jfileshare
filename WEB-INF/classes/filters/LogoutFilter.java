package filters;

import utils.CustomLogger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import objects.UserItem;

/**
 * SECTRA.
 * User: zoran
 * Date: Mar 2, 2007
 * Time: 11:59:13 AM
 */
public class LogoutFilter implements Filter {
    private FilterConfig filterconfig;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterconfig = filterConfig;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        UserItem user = (UserItem) request.getSession().getAttribute("user");
        if ( user != null ){
            CustomLogger.logme(this.getClass().getName(),"Loggin out user " + user.getUsername());
            request.getSession().removeAttribute("user");
            request.setAttribute("address","/");
            filterconfig.getServletContext().getRequestDispatcher("/templates/Forward.jsp").forward(servletRequest,servletResponse);
        }

    }

    public void destroy() {
        CustomLogger.logme(this.getClass().getName(),"USER logged out");
    }
}
