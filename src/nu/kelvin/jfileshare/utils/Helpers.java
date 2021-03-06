/**
 *  Copyright 2011 SECTRA Imtec AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * @author      Markus Berg  <markus.berg @ sectra.se>
 * @version     1.17
 * @since       2011-09-21
 */
package nu.kelvin.jfileshare.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.security.SecureRandom;

public class Helpers {

    public static final String vocabulary = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.-";
    static SecureRandom rnd = new SecureRandom();

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
     * Generate a nice random url-friendly string
     * @param length generated string length
     * @return Random string
     */
    public static String getRandomString(int length) {
        StringBuilder sb = new StringBuilder( length );
        for (int i=0; i<length; i++) {
            sb.append( vocabulary.charAt( rnd.nextInt( vocabulary.length() ) ) );
        }
        return sb.toString();
    }

    /**
     * Generate a random url-friendly string 32 bytes long
     * @return Random string
     */
    public static String getRandomString() {
        return getRandomString(32);
    }
}
