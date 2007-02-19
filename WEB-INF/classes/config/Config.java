package config;

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
    private static final String _UDIR = "/tmp/jfileshare";
    private static final String _FILESTORE = "/tmp/store";

    private static final boolean _USIZE_CHECK = false;

    private static final int _USIZE_ALLOW = 1024000;

    private static final int _KEEP_FOR_DAYS = 14;
    private static final int _USER_EXPIRES = 60;

    private static final boolean _LOGIN_REQUIRES_HTTPS = false;


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
}
