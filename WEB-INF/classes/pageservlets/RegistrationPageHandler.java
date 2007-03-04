package pageservlets;

import generic.ServletPageRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.regex.Pattern;
import java.sql.Connection;
import java.sql.SQLException;

import utils.CustomLogger;
import utils.Jcrypt;
import objects.UserItem;
import config.Config;

/**
 * Created by Zoran Pucar zoran@medorian.com.
 * User: zoran
 * Date: 2007-jan-06
 * Time: 10:00:37
 * This is copyrighted software. If you got hold
 * of this software by unauthorized means, please
 * contact us at email above.
 */
public class RegistrationPageHandler implements ServletPageRequestHandler {

    public RegistrationPageHandler() {
    }

    public boolean liveConnection(){
                    return true;
                }

    public boolean handleRequest(String urlPattern){

        if ( Pattern.compile("(register)").matcher(urlPattern).find()){
            return true;
        } else {
            return false;
        }

    }

    public String handlePageRequest(Connection conn, HttpServletRequest request, HttpServletResponse response, ServletContext context)
        throws SQLException, ServletException {
        String urlPattern = request.getServletPath();
        UserItem loginuser = (UserItem) request.getSession().getAttribute("user");

        CustomLogger.logme(this.getClass().getName(),"RegistrationPageHandler");

        if ( request.getParameter("submit") != null && ! request.getParameter("submit").equals("") && request.getParameter("action") != null && request.getParameter("action").equals("register")){
            CustomLogger.logme(this.getClass().getName(), "Request is post");
            if ( ! request.getParameter("password").equals(request.getParameter("password2"))){
                request.setAttribute("message","Passwords must match");
                return "/templates/RegistrationPage.jsp";
            }
            if ( request.getParameter("username") == null || request.getParameter("username").equals("")){
                request.setAttribute("message","Username can't be empty");
                return "/templates/RegistrationPage.jsp";
            }

            if ( objects.UserItem.checkUserExists(conn,request.getParameter("username"))){
                request.setAttribute("message","Username allready exists");
                return "/templates/RegistrationPage.jsp";
            }
            if ( request.getParameter("email") == null || request.getParameter("email").equals("")){
                request.setAttribute("message","Email can't be empty");
                return "/templates/RegistrationPage.jsp";
            }

            UserItem user = new UserItem();
            user.setUsername(request.getParameter("username"));
            user.setPassword(Jcrypt.crypt(request.getParameter("password")));
            CustomLogger.logme(this.getClass().getName(),"Password " + request.getParameter("password") + " encrypted as " + user.getPassword());
            user.setEmail(request.getParameter("email"));
            if ( loginuser != null ){
                CustomLogger.logme(this.getClass().getName(),"Requestparam expires " + request.getParameter("expires"));
                user.setExpires(request.getParameter("expires") != null && request.getParameter("expires").equals("on"));
                if ( user.expires() ){
                    CustomLogger.logme(this.getClass().getName(),"Requestparam daystoexpire " + request.getParameter("daystoexpire"));
                    user.setExpiry(Integer.parseInt(request.getParameter("daystoexpire")));
                } else {
                    user.setExpiry(Config.getUserExpires());
                }
            }

            if ( loginuser.getUserType() == objects.UserItem.TYPE_ADMIN ){
                user.setUserType(Integer.parseInt(request.getParameter("usertype")));
            } else {
                //Default is external;
                user.setUserType(3);
            }
            user.setCreator(loginuser);
            if ( user.save(conn)){
                request.setAttribute("message","User with uid " + user.getUid() + " successfully registered");
                request.setAttribute("success", true);
            }

            
        }


        return "/templates/RegistrationPage.jsp";

    }

}
