package com.sectra.jfileshare.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

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

    /**
     * Figure out the absolute path to the server in order to construct proper links
     * @param req
     * @return
     */
    public static String getUrlPrefix(HttpServletRequest req) {
        String httpScheme = req.getScheme();
        String serverName = req.getServerName();
        Integer serverPort = (Integer) req.getServerPort();
        if ((serverPort == 80 && httpScheme.equals("http"))
                || (serverPort == 443 && httpScheme.equals("https"))) {
            serverPort = null;
        }

        return httpScheme + "://"
                + serverName
                + (serverPort != null ? ":" + serverPort.toString() : "");
    }
}
