package pageservlets.subhandlers;

import views.UserItemView;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;

import objects.UserItem;

/**
 * SECTRA.
 * User: zoran
 * Date: Mar 16, 2008
 * Time: 11:40:54 AM
 */
public class UserAdminHandler implements PageSubhandler{

    public String handle(Connection conn, HttpServletRequest request) {
        utils.Helpers.debugHttpRequest(request);
        if ( request.getParameter("action") != null && request.getParameter("action").equals("dosearch") && request.getParameter("username") != null && ! request.getParameter("username").equals("")){
            UserItemView userview = new UserItemView(conn,request.getParameter("username"));
            if ( userview.getUserItem() != null){
                request.setAttribute("page","userdetail");
                request.setAttribute("user",userview.getUserItem());
                return "/templates/MainAdmin/Users.jsp";
            }
        }

        if ( request.getParameter("action") != null && request.getParameter("action").equals("doupdateuser") && request.getParameter("username") != null && ! request.getParameter("username").equals("")){
            if ( request.getParameter("uid") !=null && ! request.getParameter("uid").equals("")){
                int uid = new Integer(request.getParameter("uid"));
                 UserItemView userview = new UserItemView(conn,uid);
                UserItem user = userview.getUserItem();
                if ( request.getParameter("password") != null && request.getParameter("password2")!= null && ! request.getParameter("password").equals("") && ! request.getParameter("password2").equals("")){
                    if ( ! request.getParameter("password").equals(request.getParameter("password2"))){
                        request.setAttribute("message","Passwords don't match");
                        request.setAttribute("page","userdetail");
                        request.setAttribute("user",user);
                        return "/templates/MainAdmin/Users.jsp";
                    } else {
                        user.setClearTextPassword(request.getParameter("password"));
                    }

                }
                if ( request.getParameter("expires") != null && request.getParameter("expires").equals("on")) user.setExpires(true);
                if ( request.getParameter("usertype") != null ) user.setUserType(new Integer(request.getParameter("usertype")));
                request.setAttribute("page","search");

                user.save(conn);
                request.setAttribute("message","User successfully saved!");
                return "/templates/MainAdmin/Users.jsp";
            }

        }

        if ( request.getParameter("action") != null && request.getParameter("action").equals("confirmdelete")){
            UserItemView userview = new UserItemView(conn,new Integer(request.getParameter("uid")));
            request.setAttribute("user",userview.getUserItem());
            request.setAttribute("page","confirmdelete");
            return "/templates/MainAdmin/Users.jsp";
        }

        if (request.getParameter("action") != null && request.getParameter("action").equals("dodelete")){
            UserItemView userview = new UserItemView(conn,new Integer(request.getParameter("uid")));
            userview.getUserItem().delete(conn);
            request.setAttribute("message","User deleted!");
        }


        request.setAttribute("page","expiredusers");
        UserItemView userview = new UserItemView(conn);
        request.setAttribute("expiredusers",userview.getAllExpiredUsers());
        request.setAttribute("users",userview.getAllUsers());
        return "/templates/MainAdmin/Users.jsp";
        
    }
}
