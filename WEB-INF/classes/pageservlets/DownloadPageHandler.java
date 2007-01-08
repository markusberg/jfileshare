package pageservlets;

import generic.ServletPageRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.regex.Pattern;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileReader;
import java.nio.CharBuffer;

import utils.CustomLogger;
import objects.FileItem;

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
                //Lastpart is md5sum

                FileItem file = new FileItem();
                if ( file.search(conn,lastpart)){
                     request.setAttribute("file",file);
                } else {
                    request.setAttribute("error", "File is not found");
                }

                if ( pathparts[pathparts.length - 2].equals("get") ){
                    //Serve the file;

                    response.setContentType(file.getType());
                    response.setContentLength(file.getSize().intValue()*1024*1024);
                    PrintWriter writer = null;
                    try {
                        writer = response.getWriter();
                        CustomLogger.logme(this.getClass().getName(),"Trying to read " + file.getFile());
                        FileReader freader = new FileReader(file.getFile());

                        writer.write(freader.read());
                    } catch (IOException e) {
                        CustomLogger.logme(this.getClass().getName(), e.toString(), true);
                    }


                    writer.flush();
                    writer.close();

                }



                return "/templates/DownloaderPage.jsp";

            }

}
