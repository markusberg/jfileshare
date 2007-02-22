package utils;

import java.text.SimpleDateFormat;

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
}
