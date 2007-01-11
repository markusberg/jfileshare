package pageservlets;

import generic.ServletPageRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.regex.Pattern;
import java.util.Map;
import java.sql.Connection;
import java.sql.SQLException;

import objects.UserItem;
import objects.FileItem;
import views.UserItemView;

/**
 * SECTRA.
 * User: zoran
 * Date: Jan 11, 2007
 * Time: 10:20:40 AM
 */
public class AdminPageHandler implements ServletPageRequestHandler {

    public AdminPageHandler() {
    }

    public boolean liveConnection(){
                    return true;
    }

    public boolean handleRequest(String urlPattern){

        if ( Pattern.compile("(admin)").matcher(urlPattern).find()){
            return true;
        } else {
            return false;
        }

    }

    public String handlePageRequest(Connection conn, HttpServletRequest request, HttpServletResponse response, ServletContext context)
                throws SQLException, ServletException {

        UserItem loginuser = (UserItem) request.getSession().getAttribute("user");
        UserItemView useritemview = new UserItemView(conn,loginuser.getUsername());

        if ( request.getParameter("action") != null ){
            if ( request.getParameter("action").equals("delete")){
                FileItem file = useritemview.getFiles().get(Integer.parseInt(request.getParameter("fid")));
                if ( file != null ){
                    file.delete(conn);
                    //remove the file from the view
                    useritemview.remove(Integer.parseInt(request.getParameter("fid")));
                }

            } else if ( request.getParameter("action").equals("edit")){
                FileItem file = useritemview.getFiles().get(Integer.parseInt(request.getParameter("fid")));
                request.setAttribute("file",file);
                return "/templates/EditFile.jsp";
            } else if ( request.getParameter("action").equals("savefile")){
                FileItem file = new FileItem();
                file.setFid(Integer.parseInt(request.getParameter("fid")));
                file.setPermanent(request.getParameter("permanent").equals("yes"));
                file.setDownloads(Integer.parseInt(request.getParameter("downloads")));
                file.save(conn);
                useritemview = new UserItemView(conn,loginuser.getUsername());
            }

        }
        request.setAttribute("user",useritemview.getUserItem());
        request.setAttribute("files",useritemview.getFiles());



        return "/templates/AdminPage.jsp";
    }

}
