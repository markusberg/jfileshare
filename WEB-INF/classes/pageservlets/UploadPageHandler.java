package pageservlets;

import generic.ServletPageRequestHandler;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import utils.CustomLogger;
import utils.MD5OutputStream;

import java.sql.SQLException;
import java.sql.Connection;
import java.util.regex.Pattern;
import java.util.Date;
import java.io.File;

import http.MultipartRequest;
import http.UploadedFile;
import http.Exceptions.MultipartRequestException;
import objects.FileItem;
import objects.UserItem;
import config.Config;

/**
 * Created by Zoran Pucar zoran@medorian.com.
 * User: zoran
 * Date: 2007-jan-06
 * Time: 01:16:57
 * This is copyrighted software. If you got hold
 * of this software by unauthorized means, please
 * contact us at email above.
 */
public class UploadPageHandler implements ServletPageRequestHandler {


        public UploadPageHandler(){

        }

        public boolean liveConnection(){
            return true;
        }

        public boolean handleRequest(String urlPattern){

            if ( Pattern.compile("(upload)").matcher(urlPattern).find()){
                return true;
            } else {
                return false;
            }

        }

        public String handlePageRequest(Connection conn, HttpServletRequest request, HttpServletResponse response, ServletContext context)
            throws SQLException, ServletException {
            String urlPattern = request.getServletPath();

            CustomLogger.logme(this.getClass().getName(),"UploadPageHandler");
            String[] pathparts = request.getServletPath().split("/");
            String lastpart = pathparts[pathparts.length - 1 ];
            CustomLogger.logme(this.getClass().getName(),"Lastpart is " + lastpart);
            MultipartRequest req = null;

            try {
                req = new MultipartRequest(request);
            } catch (MultipartRequestException e) {
                CustomLogger.logme(this.getClass().getName(), e.toString(),true);
            }

            if ( req.isMultipart()){
                CustomLogger.logme(this.getClass().getName(),"Expecting " + req.getContentLength() + " bytes");
                if (req.getFile("file") != null ){
                    UploadedFile file = req.getFile("file");
                    FileItem savedfile = new FileItem();
                    CustomLogger.logme(this.getClass().getName(),"Name: " + file.getName());
                    CustomLogger.logme(this.getClass().getName(),"Type: " + file.getType());
                    CustomLogger.logme(this.getClass().getName(),"MD5: " + MD5OutputStream.getMD5(file.getFile().getPath()) );
                    savedfile.setDdate(new Date());
                    savedfile.setName(file.getName());
                    savedfile.setType(file.getType());
                    savedfile.setSize(new Double(file.getFile().length()));
                    savedfile.setMd5sum(MD5OutputStream.getMD5(file.getFile().getPath()));
                    UserItem owner = (UserItem) request.getSession().getAttribute("user");
                    savedfile.setOwner(owner);
                    savedfile.save(conn);
                    File destfile = new File(Config.getFilestore() + "/" +  savedfile.getFid());
                    file.getFile().renameTo(destfile);
                    CustomLogger.logme(this.getClass().getName(),"File " + destfile.getAbsolutePath() + " saved");
                    CustomLogger.logme(this.getClass().getName(), file.getName());
                    CustomLogger.logme(this.getClass().getName(), file.getFile().getPath());
                    CustomLogger.logme(this.getClass().getName(), file.getFile().getAbsolutePath());
                }
            }


            return "/templates/UploaderPage.jsp";

        }


    }
