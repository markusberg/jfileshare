package http.Exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: zoran
 * Date: Dec 31, 2005
 * Time: 9:17:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class MUnexpectedEndingException extends MultipartRequestException {

    public MUnexpectedEndingException(String message) {
        super(message);
    }

}
