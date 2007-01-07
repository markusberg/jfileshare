package http.Exceptions;

import utils.CustomLogger;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: zoran
 * Date: Jan 31, 2006
 * Time: 9:20:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class UploadDirectoryException extends MultipartRequestException {

    public UploadDirectoryException(File message) {
        CustomLogger.logme(this.getClass().getName(),"Could not open directory " + message.getAbsolutePath() + " for writing",true);

    }

}
