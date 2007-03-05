package config;

import objects.UserItem;

import javax.servlet.http.HttpServletRequest;
import java.util.Hashtable;

import utils.CustomLogger;

/**
 * Created by Zoran Pucar zoran@medorian.com.
 * User: zoran
 * Date: 2007-jan-06
 * Time: 01:14:17
 * This is copyrighted software. If you got hold
 * of this software by unauthorized means, please
 * contact us at email above.
 */
public class Config {
    private static final String _DB = "uploader";
    private static final String _UDIR = "c:/tmp/jfileshare";
    private static final String _FILESTORE = "c:/tmp/store";

    private static final boolean _USIZE_CHECK = false;

    private static final int _USIZE_ALLOW = 1024000;

    private static final int _KEEP_FOR_DAYS = 14;
    private static final int _USER_EXPIRES = 60;

    private static final boolean _LOGIN_REQUIRES_HTTPS = false;
    private static final boolean _ALLWAYS_FORCE_HTTPS = false;

    private static final Hashtable<String,Integer> authorization_map = new Hashtable<String,Integer>();

    static {
        authorization_map.put("/register", UserItem.TYPE_SECTRA);
        authorization_map.put("/upload",UserItem.TYPE_EXTERNAL);
        authorization_map.put("/admin",UserItem.TYPE_EXTERNAL);
    }

    public static String getDb() {
        return _DB;
    }


    public static String getUdir() {
        return _UDIR;
    }


    public static String getFilestore() {
        return _FILESTORE;
    }

    public static boolean usizeCheck() {
        return _USIZE_CHECK;
    }


    public static int getUsizeAllow() {
        return _USIZE_ALLOW;
    }

    public static int getKeepForDays(){
        return _KEEP_FOR_DAYS;
    }

    public static int getUserExpires(){
        return _USER_EXPIRES;
    }


    public static boolean loginRequiresHttps() {
        return _LOGIN_REQUIRES_HTTPS;
    }


    public static boolean allwaysForceHttps() {
        return _ALLWAYS_FORCE_HTTPS;
    }

    public static boolean isAuthorised(HttpServletRequest request, UserItem user){
        CustomLogger.logme("config.Config","Got user " + user.getUsername());
        CustomLogger.logme("config.Config","Got path " + request.getServletPath());
        //Get required type:
        for ( String path : authorization_map.keySet() ){
            CustomLogger.logme("config.Config","Checking for " + path);
            if ( request.getServletPath().startsWith(path)){
                if ( authorization_map.get(path) >= user.getUserType() ){
                    CustomLogger.logme("config.Config",authorization_map.get(path) +">=" + user.getUserType());
                    return true;
                } else {
                    CustomLogger.logme("config.Config","Privilege level of " + authorization_map.get(path) + " required, got only " + user.getUserType());
                    return false;
                }
            } else {
                CustomLogger.logme("config.Config",request.getServletPath() + " does not start with " + path);
            }
        }

        return false;

    }

    public static int getRequiredLevel(String urlPattern){
        for ( String path : authorization_map.keySet() ){
            CustomLogger.logme("config.Config","Checking for " + path);
            if ( urlPattern.startsWith(path)){
                return authorization_map.get(path);
            } else {
                CustomLogger.logme("config.Config",urlPattern + " does not start with " + path);
            }
        }

        //Default is to allow access
        return objects.UserItem.TYPE_EXTERNAL;

    }
}
