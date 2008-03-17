package utils;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Set;

/**
 * User: zoran@sectra.se
 * Date: Feb 22, 2007
 * Time: 4:38:04 PM
 */
public class Helpers {

    /**
     * Helps out to format date into readable format
     * @param date regular java.util.Date
     * @return String
     */
    public static String formatDate(java.util.Date date){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}

    public static void debugHttpRequest(HttpServletRequest request){
        for ( Object pname: request.getParameterMap().keySet() ){
            CustomLogger.logme("REQUEST_DEBUGGER: ",pname.toString() + ": " + request.getParameter(pname.toString()));
        }
    }
}
