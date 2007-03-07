package pageservlets;

import generic.ServletPageRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.Set;
import java.sql.Connection;
import java.sql.SQLException;

import objects.UserItem;
import objects.FileItem;
import objects.EmailItem;
import views.UserItemView;
import utils.CustomLogger;

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
        loginuser = useritemview.getUserItem();
        CustomLogger.logme(this.getClass().getName(),"From userdata found " + loginuser.getFiles().size());

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
                FileItem file = useritemview.getFiles().get(Integer.parseInt(request.getParameter("fid")));
                file.setFid(Integer.parseInt(request.getParameter("fid")));
                file.setPermanent(request.getParameter("permanent").equals("yes"));
                file.setEnabled(request.getParameter("enabled").equals("yes"));
                if ( request.getParameter("downloads").equals("unlimited")){
                    file.setDownloads(-1);
                } else file.setDownloads(Integer.parseInt(request.getParameter("downloads")));
                if ( request.getParameter("pwsw") != null && request.getParameter("pwsw").equals("on")){
                    CustomLogger.logme(this.getClass().getName(),"Setting password for file ");
                    if ( ! request.getParameter("password").equals("<ENCRYPTED>")){
                        file.setPassword(utils.Jcrypt.crypt(request.getParameter("password")));
                    }

                }
                file.save(conn);
                useritemview = new UserItemView(conn,loginuser.getUsername());
            } else if ( request.getParameter("action").equals("notify")){
                EmailItem email = new EmailItem(request);
                try {
                    email.addRcpt(new InternetAddress(request.getParameter("email")));
                    email.addBcpt(new InternetAddress("zoran@sectra.se"));
                    if ( loginuser.getEmail() != null ){
                        email.setSender(loginuser.getEmail());
                    }

                    email.setSubject("File " + useritemview.getFiles().get(Integer.parseInt(request.getParameter("fid"))).getName() + " is available for download");
                } catch (AddressException e){
                    CustomLogger.logme(this.getClass().getName(),"INVALID ADDRESS " + e.toString(),true);
                }
                
                email.setUrl(useritemview.getFiles().get(Integer.parseInt(request.getParameter("fid"))).getMd5sum() + "_SECTRA_" + request.getParameter("fid"));
                email.sendHTMLMail();
                request.setAttribute("message","Email is sent to " + request.getParameter("email"));
            } else if ( request.getParameter("action").equals("editch") && request.getParameter("uid") != null ){
                if ( loginuser.getChildren().containsKey(Integer.parseInt(request.getParameter("uid")))){
                    UserItemView useritemview2 = new UserItemView(conn,Integer.parseInt(request.getParameter("uid")));
                    request.setAttribute("edited",useritemview2.getUserItem());    
                } else {
                    CustomLogger.logme(this.getClass().getName(),"Nonexistent user or no privilege to edit user");
                }
            } else if ( request.getParameter("action").equals("delch") && request.getParameter("uid") != null ){
                if ( loginuser.getChildren().containsKey(Integer.parseInt(request.getParameter("uid")))){
                    UserItemView useritemview2 = new UserItemView(conn,Integer.parseInt(request.getParameter("uid")));
                    useritemview2.getUserItem().delete(conn);    
                } else {
                    CustomLogger.logme(this.getClass().getName(),"Nonexistent user or no privilege to edit user");
                }
            } else if ( request.getParameter("action").equals("viewlog") && request.getParameter("fid") != null ){
                CustomLogger.logme(this.getClass().getName(),"Searching for logs for fid=" + request.getParameter("fid"));
                Set<FileItem.DownloadLog> downloadlog = new FileItem(Integer.parseInt(request.getParameter("fid"))).getLogs(conn);
                CustomLogger.logme(this.getClass().getName(),"Found " + downloadlog.size()  + " logs");
                request.setAttribute("downloadlogs",downloadlog);
                return "/templates/DownloadLog.jsp";

            }

        }
        request.setAttribute("user",useritemview.getUserItem());
        request.setAttribute("files",useritemview.getFiles());



        return "/templates/AdminPage.jsp";
    }

}
