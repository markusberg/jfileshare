package pageservlets;

import generic.ServletPageRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.regex.Pattern;
import java.sql.Connection;
import java.sql.SQLException;

import pageservlets.subhandlers.UserAdminHandler;
import pageservlets.subhandlers.PageSubhandler;

/**
 * SECTRA.
 * User: zoran
 * Date: Mar 10, 2008
 * Time: 1:31:00 PM
 */
public class MainAdminPageHandler implements ServletPageRequestHandler {

    public MainAdminPageHandler() {
    }

    public boolean liveConnection(){
                        return true;
    }


    public boolean handleRequest(String urlPattern){

        if ( Pattern.compile("(mainadmin)").matcher(urlPattern).find()){
            return true;
        } else {
            return false;
        }

    }

    private String handleSubUrl(Connection conn,HttpServletRequest request){
        String urlPattern = request.getServletPath();
        if ( Pattern.compile("(users)").matcher(urlPattern).find()){
            request.setAttribute("subhandler","userSubHandler()");
            PageSubhandler handler= new UserAdminHandler();
            return handler.handle(conn,request);

        } else {
            request.setAttribute("subhandler","UNKNOWN");
        }
        return "/templates/MainAdminPage.jsp";
    }


    public String handlePageRequest(Connection conn, HttpServletRequest request, HttpServletResponse response, ServletContext context)
                throws SQLException, ServletException {
        return handleSubUrl(conn,request);
        //return "/templates/AdminPage.jsp";
    }

}
