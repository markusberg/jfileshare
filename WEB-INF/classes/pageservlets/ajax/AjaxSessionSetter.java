package pageservlets.ajax;

import objects.UserItem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Connection;

import utils.CustomLogger;

/**
 * SECTRA.
 * User: zoran
 * Date: Mar 18, 2008
 * Time: 6:42:40 AM
 */
public class AjaxSessionSetter implements AjaxSubHandler{

    public void handle(Connection conn, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        if (session.getAttribute("user") != null && ((UserItem)session.getAttribute("user")).getUserType()<=UserItem.TYPE_ADMIN){
            CustomLogger.logme(this.getClass().getName(),"Authorized request!");
            if ( request.getParameter("subaction").equals("set")){
                session.setAttribute(request.getParameter("name"),request.getParameter("value"));
            } else if ( request.getParameter("subaction").equals("unset")){
                session.removeAttribute(request.getParameter("name"));
            }
        }
    }
}
