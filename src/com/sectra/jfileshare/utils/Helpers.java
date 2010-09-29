package com.sectra.jfileshare.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Helpers {

    /**
     * Helps out to format date into readable format
     * @param date regular java.util.Date
     * @return String
     */
    public static String formatDate(Date date) {
        // SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        // return formatter.format(date);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    /**
     * Make string safe to output as value="" in an html form
     * @param field String that will be sent back to user agent
     * @return String safe for returning to user agent
     */
    public static String htmlSafe(String field) {
        if (field == null) {
            return "";
        }
        field = field.replaceAll("\"", "&quot;");
        field = field.replaceAll("<", "&lt;");
        field = field.replaceAll(">", "&gt;");
        return field;
    }
}
