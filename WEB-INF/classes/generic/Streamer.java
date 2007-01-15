package generic;

import config.Config;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Connection;
import java.io.*;

import utils.CustomLogger;
import objects.FileItem;

/**
 * SECTRA.
 * User: zoran
 * Date: Jan 9, 2007
 * Time: 9:13:46 AM
 */
public class Streamer extends HttpServlet {

    DataSource datasource;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            datasource = (DataSource) env.lookup("jdbc/" + Config.getDb());

        } catch (NamingException e){
            throw new ServletException(e);
        }


    }

    private java.sql.Connection getConnection()
     throws SQLException {
	    return datasource.getConnection();
    }


    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws
		ServletException, java.io.IOException {
        String[] pathparts = request.getServletPath().split("/");
        String lastpart = pathparts[pathparts.length - 1 ];
        CustomLogger.logme(this.getClass().getName(),"Lastpart is " + lastpart);
        //Lastpart is md5sum
        FileItem file = new FileItem();
        Connection conn = null;
        try {
            conn = getConnection();
        } catch (SQLException e) {
            CustomLogger.logme(this.getClass().getName(), e.toString(), true);
        }


        if ( file.search(conn,lastpart) && ( file.getDownloads() == -1 || file.getDownloads() > 0 )){
            if ( pathparts[pathparts.length - 2].equals("get") ){
                response.setContentType(file.getType());
                response.setHeader("Content-disposition", "filename=" + file.getName());
                ServletOutputStream sos = response.getOutputStream();
                FileInputStream fis = new FileInputStream(file.getFile());
                BufferedInputStream bis = new BufferedInputStream(fis);
                BufferedOutputStream bos = new BufferedOutputStream(sos);
                byte[] buff = new byte[new Long(file.getFile().length()).intValue()];
                int bytesRead;
                // Simple read/write loop.
                while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                    bos.write(buff, 0, bytesRead);
                }

                if ( bos != null ) bos.close();
                if ( bis != null ) bis.close();


                try {
                    if ( conn != null ) conn.close();
                } catch (SQLException e) {
                    CustomLogger.logme(this.getClass().getName(), e.toString(), true);
                } finally {
                    try {
                        if ( conn != null ) conn.close();
                    } catch (SQLException ex){
                        CustomLogger.logme(this.getClass().getName(),"SERIOUS ERROR HAPPENED!! ", true);
                    }
                }

            }
            CustomLogger.logme(this.getClass().getName(),"Reducing download");
            file.reduceDownload(conn);
            


        } else {
            //If file is not found, dispatch an html-page
            request.setAttribute("error", "File is not found");
            ServletContext app = getServletContext();
            RequestDispatcher disp = app.getRequestDispatcher("/templates/FileNotFound.jsp");
            disp.forward(request,response);

        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException{
	    processRequest(request,response);
    }


   public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException{
	    processRequest(request,response);
    }
}
