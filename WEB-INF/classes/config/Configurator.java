package config;

import utils.CustomLogger;

import javax.servlet.ServletContext;

/**
 * Created by IntelliJ IDEA.
 * User: zoran
 * Date: Sep 23, 2009
 * Time: 11:31:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class Configurator {
    private static Configurator instance = null;
    private String DATABASE = "";
    private String TMP_PATH = "/tmp";
    private String PERM_PATH = "";
    private Configurator() {

    }

    public static Configurator getInstance(ServletContext context){
        if ( instance == null ){
            instance = new Configurator();
            if ( context.getInitParameter("DATABASE") != null ){
                CustomLogger.logme("Configurator","Attribute DATABASE IS NOT NULL");
                instance.DATABASE = context.getInitParameter("DATABASE").toString();
            }

            if ( context.getInitParameter("TMP_PATH") != null ){
                CustomLogger.logme("Configurator","Attribute TMP_PATH IS NOT NULL");
                instance.TMP_PATH = context.getInitParameter("TMP_PATH").toString();
            }

            if ( context.getInitParameter("PERM_PATH") != null ){
                CustomLogger.logme("Configurator","Attribute PERM_PATH IS NOT NULL");
                instance.PERM_PATH = context.getInitParameter("PERM_PATH").toString();
            }


        }

        return instance;
    }

    public String getDATABASE() {
        return DATABASE;
    }

    public String getTMP_PATH() {
        return TMP_PATH;
    }

    public String getPERM_PATH() {
        return PERM_PATH;
    }
}
