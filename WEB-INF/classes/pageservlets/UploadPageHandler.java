package pageservlets;

import generic.ServletPageRequestHandler;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import utils.CustomLogger;

import java.sql.SQLException;
import java.sql.Connection;
import java.util.regex.Pattern;
import java.io.File;

import http.MultipartRequest;
import http.Exceptions.MultipartRequestException;

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
                if (req.getFile("file") != null ){
                    File file = req.getFile("file");
                    CustomLogger.logme(this.getClass().getName(), file.getName());
                    CustomLogger.logme(this.getClass().getName(), file.getPath());
                    CustomLogger.logme(this.getClass().getName(), file.getAbsolutePath());
                }
            }


            return "/templates/UploaderPage.jsp";

        }


    }
