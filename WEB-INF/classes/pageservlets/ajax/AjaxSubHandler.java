package pageservlets.ajax;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;

/**
 * User: zoran
 * Date: 2006-mar-16
 * Time: 21:43:46
 * zoran@medorian.com
 * These subhandlers just handle the recieved data. They never return anything to AjaxHandler directly.
 */
public interface AjaxSubHandler {
	public void handle(Connection conn, HttpServletRequest request);
}
