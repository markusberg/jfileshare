package http.Exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: zoran
 * Date: Jan 31, 2006
 * Time: 9:26:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class MultipartRequestException extends Exception {

	public MultipartRequestException()
	{
		super();
	}

	public MultipartRequestException(String message)
	{
		super(message);
	}

	public MultipartRequestException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public MultipartRequestException(Throwable cause)
	{
		super(cause);
	}
}
