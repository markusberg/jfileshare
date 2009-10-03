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

                if ( Pattern.compile("(download)").matcher(urlPattern).find() || Pattern.compile("(direct)").matcher(urlPattern).find()){
                    return true;
                } else {
                    return false;
                }

            }

            public String handlePageRequest(Connection conn, HttpServletRequest request, HttpServletResponse response, ServletContext context)
                throws SQLException, ServletException {
                String urlPattern = request.getServletPath();
                FileItem file = new FileItem();
                if ( Pattern.compile("(direct)").matcher(urlPattern).find()){
                    int fid = Integer.parseInt(request.getParameter("fid"));
                    if ( file.search(conn,fid)){
                        if ( file.allowTinyurl() ){
                            CustomLogger.logme(this.getClass().getName(),"File found");
                            request.setAttribute("file",file);
                        } else {
                            CustomLogger.logme(this.getClass().getName(),"File not allowed tinyurl");
                            request.setAttribute("error", "File is not found");
                        }
                    } else {
                        CustomLogger.logme(this.getClass().getName(),"File not found");
                        request.setAttribute("error", "File is not found");
                    }
                }

                CustomLogger.logme(this.getClass().getName(),"DownloadPageHandler");
                if ( Pattern.compile("(download)").matcher(urlPattern).find() ){
                String[] pathparts = request.getServletPath().split("/");
                String lastpart = pathparts[pathparts.length - 1 ];
                CustomLogger.logme(this.getClass().getName(),"Lastpart is " + lastpart);
                //Lastpart is md5sum_SECTRA_fid
                String md5sum = lastpart.split("_SECTRA_")[0];
                int fid = Integer.parseInt(lastpart.split("_SECTRA_")[1]);


                    if ( file.search(conn,md5sum,fid)){
                        CustomLogger.logme(this.getClass().getName(),"File found");
                         request.setAttribute("file",file);
                    } else {
                        CustomLogger.logme(this.getClass().getName(),"File not found");
                        request.setAttribute("error", "File is not found");
                    }


                if ( pathparts[pathparts.length - 2].equals("get") ){
                    //Serve the file;

                    response.setContentType(file.getType());
                    response.setContentLength(file.getSize().intValue());
                    PrintWriter writer = null;
                    try {
                        writer = response.getWriter();
                        response.setBufferSize(file.getSize().intValue());
                        CustomLogger.logme(this.getClass().getName(),"Trying to read " + file.getFile());
                        FileReader freader = new FileReader(file.getFile());

                        writer.write(freader.read());
                    } catch (IOException e) {
                        CustomLogger.logme(this.getClass().getName(), e.toString(), true);
                    }


                    writer.flush();
                    writer.close();

                }
                }



                return "/templates/DownloaderPage.jsp";

            }

}
