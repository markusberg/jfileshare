package utils;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.Date;
import java.util.GregorianCalendar;

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

    public static Date calcDate(Date date, int days){
        CustomLogger.logme("utils.Helpers","INPUT DATE: " + date);
        GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(new SimpleDateFormat("yyyy").format(date)),Integer.parseInt(new SimpleDateFormat("M").format(date))-1,Integer.parseInt(new SimpleDateFormat("d").format(date))+days,Integer.parseInt(new SimpleDateFormat("H").format(date)),Integer.parseInt(new SimpleDateFormat("m").format(date)),Integer.parseInt(new SimpleDateFormat("s").format(date)));

        CustomLogger.logme("utils.Helpers","OUTPUT DATE: " + cal.getTime());
        return cal.getTime();
    }

}
