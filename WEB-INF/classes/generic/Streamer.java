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
import java.util.Hashtable;

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
        //Lastpart is md5sum_SECTRA_fid
        String md5sum = lastpart.split("_SECTRA_")[0];
        int fid = Integer.parseInt(lastpart.split("_SECTRA_")[1]);

        FileItem file = new FileItem();
        Connection conn = null;
        try {
            conn = getConnection();
        } catch (SQLException e) {
            CustomLogger.logme(this.getClass().getName(), e.toString(), true);
        }

        if ( file.search(conn,md5sum,fid)){
            CustomLogger.logme(this.getClass().getName(),"CHECK found file");
            CustomLogger.logme(this.getClass().getName(),file.isEnabled()?"isEnabled":"Not enabled");
            CustomLogger.logme(this.getClass().getName(),Integer.toString(file.getDownloads()));
        } else {
            CustomLogger.logme(this.getClass().getName(),"No file in database");
        }
        if ( file.search(conn,md5sum,fid) && file.isEnabled() && ( file.getDownloads() == -1 || file.getDownloads() > 0 )){
            CustomLogger.logme(this.getClass().getName(),"STREAMER FOUND FILE...");
            if ( pathparts[pathparts.length - 2].equals("get") ){
                CustomLogger.logme(this.getClass().getName(),"... about to stream");
                response.setContentType(file.getType());
                response.setHeader("Content-disposition", "attachment; filename=" + file.getName());
                response.setHeader("Content-length",Long.toString(file.getFile().length()));
                ServletOutputStream sos = response.getOutputStream();
                FileInputStream fis = new FileInputStream(file.getFile());
                BufferedInputStream bis = new BufferedInputStream(fis);
                BufferedOutputStream bos = new BufferedOutputStream(sos);
                Long buffersize = new Long(20971520);
                if ( file.getFile().length() < 20971520) buffersize = file.getFile().length();
                byte[] buff = new byte[new Long(buffersize).intValue()];
                int bytesRead;
                // Simple read/write loop.


                while(-1 != (bytesRead = bis.read(buff,0,buff.length))) {
                    bos.write(buff,0,bytesRead);
                }

                if ( bos != null ) bos.close();
                if ( bis != null ) bis.close();
                if ( fis != null ) fis.close();
                if ( sos != null ) sos.close();
                CustomLogger.logme(this.getClass().getName(),"Reducing download");
                file.registerDownload(conn, request);

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

            


        } else {
            //If file is not found, terminate connections and dispatch an html-page
            CustomLogger.logme(this.getClass().getName(),"Can't find that file");
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
            request.setAttribute("error", "File is not found");
            ServletContext app = getServletContext();
            RequestDispatcher disp = app.getRequestDispatcher("/templates/FileNotFound.jsp");
            disp.forward(request,response);

        }

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


    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException{
	    processRequest(request,response);
    }


   public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException{
	    processRequest(request,response);
    }
}
