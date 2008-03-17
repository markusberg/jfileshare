package pageservlets.subhandlers;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;

/**
 * SECTRA.
 * User: zoran
 * Date: Mar 16, 2008
 * Time: 11:40:01 AM
 */
public interface PageSubhandler {
    public String handle(Connection conn, HttpServletRequest request);
}
