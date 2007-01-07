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

/**
 * Created by Zoran Pucar zoran@medorian.com.
 * User: zoran
 * Date: 2007-jan-06
 * Time: 09:28:50
 * This is copyrighted software. If you got hold
 * of this software by unauthorized means, please
 * contact us at email above.
 */
public class DownloadPageHandler implements ServletPageRequestHandler {

    public DownloadPageHandler() {
    }

    public boolean liveConnection(){
                return true;
            }

            public boolean handleRequest(String urlPattern){

                if ( Pattern.compile("(download)").matcher(urlPattern).find()){
                    return true;
                } else {
                    return false;
                }

            }

            public String handlePageRequest(Connection conn, HttpServletRequest request, HttpServletResponse response, ServletContext context)
                throws SQLException, ServletException {
                String urlPattern = request.getServletPath();

                CustomLogger.logme(this.getClass().getName(),"DownloadPageHandler");
                String[] pathparts = request.getServletPath().split("/");
                String lastpart = pathparts[pathparts.length - 1 ];
                CustomLogger.logme(this.getClass().getName(),"Lastpart is " + lastpart);


                return "/templates/DownloaderPage.jsp";

            }

}
