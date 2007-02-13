package http;


import http.Exceptions.*;

import java.io.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Hashtable;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import config.Config;
import utils.CustomLogger;

public class MultipartRequest extends HttpServletRequestWrapper implements HttpServletRequest
{
    private static final String	CONTENT_TYPE_HEADER = "Content-Type";
    private static final String	MULTIPART_CONTENT_TYPE = "multipart/form-data";
    private static final String APPLICATION_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String	BOUNDARY_PREFIX = "boundary=";
    private static final int	BOUNDARY_PREFIX_LENGTH = BOUNDARY_PREFIX.length();
    private static final String	CONTENT_DISPOSITION_PREFIX = "content-disposition: ";
    private static final int	CONTENT_DISPOSITION_PREFIX_LENGTH = CONTENT_DISPOSITION_PREFIX.length();
    private static final String	FIELD_NAME_PREFIX = "name=\"";
    private static final int	FIELD_NAME_PREFIX_LENGTH = FIELD_NAME_PREFIX.length();
    private static final String	FILENAME_PREFIX = "filename=\"";
    private static final int	FILENAME_PREFIX_LENGTH = FILENAME_PREFIX.length();
    private static final String	QUOTE = "\"";
    private static final String	FORM_DATA_DISPOSITION = "form-data";
    private static final String	DEFAULT_ENCODING = "UTF-8";

    private File				mUploadDirectory = null;

    private HttpServletRequest	mRequest = null;
    private String				mBoundary = null;
    private ServletInputStream	mInput = null;
    private byte[]				mParameterBuffer = null;
    private byte[]				mFileBuffer = null;
    private String				mEncoding = DEFAULT_ENCODING;

    private HashMap<String, String[]>		mParameters = null;
    private HashMap<String, UploadedFile[]>	mFiles = null;

    public static int DELAY_FILEREAD = 0;
    private File tmp_file = null;


    public MultipartRequest(HttpServletRequest request)
    throws MultipartRequestException
        {
        super(request);
        if (null == request)	throw new IllegalArgumentException("request can't be null");

        mRequest = request;
        mParameters = new HashMap<String, String[]>();
        mFiles = new HashMap<String, UploadedFile[]>();
        mParameterBuffer = new byte[8*1024];
        mFileBuffer = new byte[100*1024];
        if ( this.isMultipart()){
            CustomLogger.logme(this.getClass().getName(),"Request detected as multipart");
            checkUploadDirectory();
            initialize();
            createTmpFile();
            checkInputStart();
            readParts();
         }
    }

    public MultipartRequest(HttpServletRequest request, File tmp_file)
    throws MultipartRequestException
        {
        super(request);
        if (null == request)	throw new IllegalArgumentException("request can't be null");

        mRequest = request;
        mParameters = new HashMap<String, String[]>();
        mFiles = new HashMap<String, UploadedFile[]>();
        mParameterBuffer = new byte[8*1024];
        mFileBuffer = new byte[100*1024];
        this.tmp_file = tmp_file;
        if ( this.isMultipart()){
            CustomLogger.logme(this.getClass().getName(),"Request detected as multipart");
            checkUploadDirectory();
            initialize();
            checkInputStart();
            readParts();
         }
    }


    public MultipartRequest(HttpServletRequest request, int DELAY_FILEREAD )
    throws MultipartRequestException
        {
        super(request);
        if (null == request)	throw new IllegalArgumentException("request can't be null");

        mRequest = request;
        mParameters = new HashMap<String, String[]>();
        mFiles = new HashMap<String, UploadedFile[]>();
        mParameterBuffer = new byte[8*1024];
        mFileBuffer = new byte[100*1024];
        if ( this.isMultipart()){
            CustomLogger.logme(this.getClass().getName(),"Request detected as multipart DELAY_FILEREAD");

            checkUploadDirectory();
            initialize();
            CustomLogger.logme(this.getClass().getName(),"Creating tmp-file");
            createTmpFile();
            CustomLogger.logme(this.getClass().getName(),"Created " + tmp_file.getAbsolutePath());
            checkInputStart();
            readTextParts();
        }
    }

    public void readFilePart(){
        try {
            readNextPart();
        } catch (MultipartRequestException e) {
            CustomLogger.logme(this.getClass().getName(), e.toString(), true);
        }
    }

    static boolean isValidContentType(String type)
    {
	if (null == type ||
	   ( !type.toLowerCase().startsWith(MULTIPART_CONTENT_TYPE) ))
	{
	    return false;
	}

	return true;
    }


    public UploadedFile getFile(String fieldname){
	if ( ! getFileMap().isEmpty()){
		Map filemap = getFileMap();
		UploadedFile[] file = (UploadedFile[]) filemap.get(fieldname);
		CustomLogger.logme(this.getClass().getName(),"file length is " + file.length);
		if ( file.length != 0 ){
		    UploadedFile file1 = file[0];
		    if ( file1.getFile().length() > 0 ){
			return file1;
		    }  else return null;
		}
	    }
	return null;
    }

    public String getParameter(String param){
	if (! isMultipart()){
	    return super.getParameter(param);
	} else {
	    String[] params = mParameters.get(param);
	    CustomLogger.logme(this.getClass().getName(),"Parameter " + param + " " + params[0] + " ");
	    return params[0];
	}
    }
    public Map<String, String[]> getParameterMap()
    {
	return mParameters;
    }

    public Map<String, UploadedFile[]> getFileMap()
    {
	return mFiles;
    }

    void setEncoding(String encoding)
    {
	assert encoding != null;

	mEncoding = encoding;
    }

    private void checkUploadDirectory() throws MultipartRequestException {
        CustomLogger.logme(this.getClass().getName(),"Checking upload dir");
        mUploadDirectory = new File(Config.getUdir());
        mUploadDirectory.mkdirs();

        if(!mUploadDirectory.exists() || !mUploadDirectory.isDirectory() || !mUploadDirectory.canWrite()) {
            throw new UploadDirectoryException(mUploadDirectory);
        }
    }


    public boolean isMultipart(){
        String type_header = mRequest.getHeader(CONTENT_TYPE_HEADER);
        String type_method = mRequest.getContentType();
        String type = null;
        if (type_header == null && type_method != null) {
            type = type_method;
        } else if (type_method == null && type_header != null) {
            type = type_header;
        }
        // If neither value is null, choose the longer value
        else if (type_header != null && type_method != null) {
            type = (type_header.length() > type_method.length() ? type_header : type_method);
        }
        return isValidContentType(type);
    }


    private void initialize() throws MultipartRequestException {
        CustomLogger.logme(this.getClass().getName(),"Initializing");
        // Check the content type to is correct to support a multipart request
        // Access header two ways to work around WebSphere oddities
        String type = null;
        String type_header = mRequest.getHeader(CONTENT_TYPE_HEADER);
        String type_method = mRequest.getContentType();

        // If one value is null, choose the other value
        if (type_header == null && type_method != null) {
            type = type_method;
        } else if (type_method == null && type_header != null) {
            type = type_header;
        }
	// If neither value is null, choose the longer value
	    else if (type_header != null && type_method != null) {
	        type = (type_header.length() > type_method.length() ? type_header : type_method);
	    }

        // ensure that the content-type is correct
        if (!isValidContentType(type)) {
            throw new InvalidContentTypeException(type);
        }

        // extract the boundary seperator that is used by this request
        mBoundary = extractBoundary(type);
        if (null == mBoundary) {
            throw new MissingBoundaryException("Missing boundary ");
        }

	// obtain the input stream
        try {
            mInput = mRequest.getInputStream();
        } catch (IOException e){
            throw new MultipartInputErrorException(e.toString());
        }

    }

    private void checkInputStart() throws MultipartRequestException {
        CustomLogger.logme(this.getClass().getName(),"Checking input start");
        // Read the first line, should be the first boundary
        String line = readLine();
        if (null == line)
        {
            throw new MUnexpectedEndingException("Line is null ");
        }

        // Verify that the line is the boundary
        if (!line.startsWith(mBoundary))
        {
            throw new MInvalidBoundaryException("Invalid boundary : no " + mBoundary + " on line " + line);
        }
    }

    private void readParts() throws MultipartRequestException {
        CustomLogger.logme(this.getClass().getName(),"Reading parts");
        boolean more_parts = true;

        while (more_parts) {
            more_parts = readNextPart();
        }
    }

    private void readTextParts() throws MultipartRequestException {
	    boolean more_parts = true;

        while (more_parts) {
            more_parts = readNextTextPart();
        }
    }

    private String extractBoundary(String line) {
        // Use lastIndexOf() because IE 4.01 on Win98 has been known to send the
        // "boundary=" string multiple times.
        int index = line.lastIndexOf(BOUNDARY_PREFIX);

        if (-1 == index) {
            return null;
        }

        // start from after the boundary prefix
        String boundary = line.substring(index+BOUNDARY_PREFIX_LENGTH);
        if ('"' == boundary.charAt(0)) {
            // The boundary is enclosed in quotes, strip them
            index = boundary.lastIndexOf('"');
            boundary = boundary.substring(1, index);
        }

        // The real boundary is always preceeded by an extra "--"
        boundary = "--"+boundary;

        return boundary;
    }

    private String readLine() throws MultipartRequestException {
        StringBuffer line_buffer = new StringBuffer();

        int result = 0;
        do
        {
            try {
             result = mInput.readLine(mParameterBuffer, 0, mParameterBuffer.length);
            } catch (IOException e) {
                throw new MultipartInputErrorException(e.toString());
            }

            if (result != -1) {
                try {
                    line_buffer.append(new String(mParameterBuffer, 0, result, mEncoding));
                } catch (UnsupportedEncodingException e) {
                    throw new MultipartInputErrorException(e.toString());
                }
            }
        }
        // if the buffer wasn't completely filled, the end of the input has been reached
        while (result == mParameterBuffer.length);

        // if nothing was read, the end of the stream must have been reached
        if (line_buffer.length() == 0) {
            return null;
        }

        // Cut off the trailing \n or \r\n
        // It should always be \r\n but IE5 sometimes does just \n
        int line_length = line_buffer.length();
        if (line_length >= 2 && '\r' == line_buffer.charAt(line_length-2)) {
            // remove the trailing \r\n
            line_buffer.setLength(line_length-2);
        } else if (line_length >= 1 && '\n' == line_buffer.charAt(line_length-1)) {
            // remove the trailing \n
            line_buffer.setLength(line_length-1);
        }

	    return line_buffer.toString();
    }


    private boolean readNextTextPart() throws MultipartRequestException {
        // Read the headers; they look like this (not all may be present):
	// Content-Disposition: form-data; name="field1"; filename="file1.txt"
	// Content-Type: type/subtype
	// Content-Transfer-Encoding: binary
	ArrayList<String> headers = new ArrayList<String>();

	String line = readLine();
	// When no next line could be read, the end was reached.
	// IE4 on Mac sends an empty line at the end; treat that as the ending too.
	if (null == line ||
	    0 == line.length())
	{
	    // No parts left, we're done
	    return false;
	}

	// Read the following header lines we hit an empty line
	// A line starting with whitespace is considered a continuation;
	// that requires a little special logic.
	while (null != line &&
	       line.length() > 0)
	{
	    String	next_line = null;
	    boolean	obtain_next_line = true;
	    while (obtain_next_line)
	    {
		next_line = readLine();

		if (next_line != null &&
		    (next_line.startsWith(" ") ||
		     next_line.startsWith("\t")))
		{
		    line = line + next_line;
		}
		else
		{
		    obtain_next_line = false;
		}
	    }
	    // Add the line to the header list
	    headers.add(line);
	    line = next_line;
	}

	// If we got a null above, it's the end
	if (line == null)
	{
	    return false;
	}

	String fieldname = null;
	String filename = null;
	String content_type = "text/plain";  // rfc1867 says this is the default

	String[]	disposition_info = null;

	for (String headerline : headers)
    {
        //CustomLogger.logme(this.getClass().getName(),"Reading header " + headerline);
        if (headerline.toLowerCase().startsWith(CONTENT_DISPOSITION_PREFIX))
	    {
		// Parse the content-disposition line
		disposition_info = extractDispositionInfo(headerline);

		fieldname = disposition_info[0];
		filename = disposition_info[1];
	    }
	    else if (headerline.toLowerCase().startsWith(CONTENT_TYPE_HEADER.toLowerCase()))
	    {
		// Get the content type, or null if none specified
		String type = extractContentType(headerline);
		if (type != null)
		{
		    content_type = type;
		}
	    }
	}

	if (null == filename)
	{
	    // This is a parameter
	    String		new_value = readParameter();
	    String[]	values = mParameters.get(fieldname);
	    String[]	new_values = null;
	    if (null == values)
	    {
		new_values = new String[1];
	    }
	    else
	    {
		new_values = new String[values.length+1];
		System.arraycopy(values, 0, new_values, 0, values.length);
	    }
	    new_values[new_values.length-1] = new_value;
	    mParameters.put(fieldname, new_values);
	}
	else
	{
        return false;

        //Dont read file here
        /*CustomLogger.logme(this.getClass().getName(),"Saving file " + filename);
	    // This is a file
	    if (filename.equals(""))
	    {
		// empty filename, probably an "empty" file param
		filename = null;
	    }
        CustomLogger.logme(this.getClass().getName(),"Creating file with content type " + content_type);
	    UploadedFile	new_file = new UploadedFile(filename, content_type);
	    readAndSaveFile(new_file, fieldname);
	    UploadedFile[]	files = mFiles.get(fieldname);
	    UploadedFile[]	new_files = null;
	    if (null == files)
	    {
		new_files = new UploadedFile[1];
	    }
	    else
	    {
		new_files = new UploadedFile[files.length+1];
		System.arraycopy(files, 0, new_files, 0, files.length);
	    }
	    new_files[new_files.length-1] = new_file;
	    mFiles.put(fieldname, new_files); */

    }

	return true;

    }

    private boolean readNextPart()
    throws MultipartRequestException
    {
	// Read the headers; they look like this (not all may be present):
	// Content-Disposition: form-data; name="field1"; filename="file1.txt"
	// Content-Type: type/subtype
	// Content-Transfer-Encoding: binary
	ArrayList<String> headers = new ArrayList<String>();

	String line = readLine();
	// When no next line could be read, the end was reached.
	// IE4 on Mac sends an empty line at the end; treat that as the ending too.
	if (null == line ||
	    0 == line.length())
	{
	    // No parts left, we're done
	    return false;
	}

	// Read the following header lines we hit an empty line
	// A line starting with whitespace is considered a continuation;
	// that requires a little special logic.
	while (null != line &&
	       line.length() > 0)
	{
	    String	next_line = null;
	    boolean	obtain_next_line = true;
	    while (obtain_next_line)
	    {
		next_line = readLine();

		if (next_line != null &&
		    (next_line.startsWith(" ") ||
		     next_line.startsWith("\t")))
		{
		    line = line + next_line;
		}
		else
		{
		    obtain_next_line = false;
		}
	    }
	    // Add the line to the header list
	    headers.add(line);
	    line = next_line;
	}

	// If we got a null above, it's the end
	if (line == null)
	{
	    return false;
	}

	String fieldname = null;
	String filename = null;
	String content_type = "text/plain";  // rfc1867 says this is the default

	String[]	disposition_info = null;

	for (String headerline : headers)
	{
	    if (headerline.toLowerCase().startsWith(CONTENT_DISPOSITION_PREFIX))
	    {
		// Parse the content-disposition line
		disposition_info = extractDispositionInfo(headerline);

		fieldname = disposition_info[0];
		filename = disposition_info[1];
	    }
	    else if (headerline.toLowerCase().startsWith(CONTENT_TYPE_HEADER.toLowerCase()))
	    {
		// Get the content type, or null if none specified
		String type = extractContentType(headerline);
		if (type != null)
		{
		    content_type = type;
		}
	    }
	}

	if (null == filename)
	{
        CustomLogger.logme(this.getClass().getName(),"Parameter detected:");
        // This is a parameter
	    String		new_value = readParameter();
	    String[]	values = mParameters.get(fieldname);
	    String[]	new_values = null;
	    if (null == values)
	    {
		new_values = new String[1];
	    }
	    else
	    {
		new_values = new String[values.length+1];
		System.arraycopy(values, 0, new_values, 0, values.length);
	    }
	    new_values[new_values.length-1] = new_value;
	    mParameters.put(fieldname, new_values);
        String upid = mParameters.get("upid")[0];
        //Store parameters in SessionData object in session...
        if ( upid != null && mRequest.getSession().getAttribute(upid) == null ){
            SessionData sdata = new SessionData();
            sdata.setContentLength(mRequest.getContentLength());
            sdata.addParam(fieldname,new_value);
            sdata.setTmp_filename(tmp_file.getAbsolutePath());
            mRequest.getSession().setAttribute(upid,sdata);

        } else if ( upid != null) {
            SessionData sdata = (SessionData) mRequest.getSession().getAttribute(upid);
            sdata.addParam(fieldname,new_value);
            mRequest.getSession().setAttribute(upid,sdata);
        }
    }
	else
	{
	    CustomLogger.logme(this.getClass().getName(),"Saving file " + filename);
	    // This is a file
	    if (filename.equals(""))
	    {
		// empty filename, probably an "empty" file param
		filename = null;
	    }
        CustomLogger.logme(this.getClass().getName(),"Creating file with content type " + content_type);
	    UploadedFile	new_file = new UploadedFile(filename, content_type);
	    readAndSaveFile(new_file, fieldname);
	    UploadedFile[]	files = mFiles.get(fieldname);
	    UploadedFile[]	new_files = null;
	    if (null == files)
	    {
		new_files = new UploadedFile[1];
	    }
	    else
	    {
		new_files = new UploadedFile[files.length+1];
		System.arraycopy(files, 0, new_files, 0, files.length);
	    }
	    new_files[new_files.length-1] = new_file;
	    mFiles.put(fieldname, new_files);
	}

	return true;
    }

    private String[] extractDispositionInfo(String dispositionLine)
    throws MultipartRequestException
    {
	// Return the line's data as an array: disposition, name, filename, full filename
	String[]	result = new String[3];
	String		lowcase_line = dispositionLine.toLowerCase();
	String		fieldname = null;
	String		filename = null;
	String		filename_full = null;

	// Get the content disposition, should be "form-data"
	int start = lowcase_line.indexOf(CONTENT_DISPOSITION_PREFIX);
	int end = lowcase_line.indexOf(";");
	if (-1 == start ||
	    -1 == end)
	{
	    throw new MultipartCorruptContentDispositionException(dispositionLine);
	}
	String disposition = lowcase_line.substring(start+CONTENT_DISPOSITION_PREFIX_LENGTH, end);
	if (!disposition.equals(FORM_DATA_DISPOSITION))
	{
	    throw new MultipartInvalidContentDispositionException(dispositionLine);
	}

	// Get the field name, start at last semicolon
	start = lowcase_line.indexOf(FIELD_NAME_PREFIX, end);
	end = lowcase_line.indexOf(QUOTE, start+FIELD_NAME_PREFIX_LENGTH);
	if (-1 == start ||
	    -1 == end)
	{
	    throw new MultipartCorruptContentDispositionException(dispositionLine);
	}
	fieldname = dispositionLine.substring(start+FIELD_NAME_PREFIX_LENGTH, end);

	// Get the filename, if given
	start = lowcase_line.indexOf(FILENAME_PREFIX, end+2); // after quote and space)
	end = lowcase_line.indexOf(QUOTE, start+FILENAME_PREFIX_LENGTH);
	if (start != -1 &&
	    end != -1)
	{
	    filename_full = dispositionLine.substring(start+FILENAME_PREFIX_LENGTH, end);
	    filename = filename_full;

	    // The filename may contain a full path.  Cut to just the filename.
	    int last_slash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
	    if (last_slash > -1)
	    {
		// only take the filename (after the last slash)
		filename = filename.substring(last_slash + 1);
	    }
	}

	// Return a String array: name, filename, full filename
	// empty filename denotes no file posted!
	result[0] = fieldname;
	result[1] = filename;
	result[2] = filename_full;

	return result;
    }

    private String extractContentType(String contentTypeLine)
    throws MultipartRequestException
    {
	String	result = null;
	String	lowcase_line = contentTypeLine.toLowerCase();

	// Get the content type, if any
	if (lowcase_line.startsWith(CONTENT_TYPE_HEADER) || contentTypeLine.startsWith(CONTENT_TYPE_HEADER))
	{
	    int seperator_location = lowcase_line.indexOf(" ");
	    if (-1 == seperator_location)
	    {
		throw new MultipartCorruptContentTypeException(contentTypeLine);
	    }
	    result = lowcase_line.substring(seperator_location+1);
	}
	else if (lowcase_line.length() != 0)
	{
	    // no content type, so should be empty
	    throw new MultipartCorruptContentTypeException(contentTypeLine);
	}

	return result;
    }

    private String readParameter()
    throws MultipartRequestException
    {
        CustomLogger.logme(this.getClass().getName(),"readParameter(): Reading parameter");
    StringBuffer	result = new StringBuffer();
	String			line = null;

	synchronized (result) // speed increase by thread lock pre-allocation
	{
	    while ((line = readLine()) != null)
	    {
		if(line.startsWith(mBoundary))
		{
		    break;
		}
		// add the \r\n in case there are many lines
		result.append(line).append("\r\n");
	    }

	    // nothing read
	    if (0 == result.length())
	    {
		return null;
	    }

	    // cut off the last line's \r\n
	    result.setLength(result.length()-2);

	    return result.toString();
	}
    }


    private void createTmpFile(){
        try {
            tmp_file = File.createTempFile("upl", ".tmp", mUploadDirectory);
        } catch (IOException e) {
            CustomLogger.logme(this.getClass().getName(), e.toString(), true);
        }
    }

    private void readAndSaveFile(UploadedFile file, String name)
    throws MultipartRequestException
    {
	CustomLogger.logme(this.getClass().getName(),"Will save file " + file.getName() + " " + name);
        CustomLogger.logme(this.getClass().getName(), "The type is " + file.getType());
    assert file != null;

	//File					tmp_file = null;
	FileOutputStream		output_stream = null;
	BufferedOutputStream	output = null;
    if ( tmp_file == null ){
        try
        {
            CustomLogger.logme(this.getClass().getName(),"No tmp_file... creating one:");
            tmp_file = File.createTempFile("upl", ".tmp", mUploadDirectory);
        }
        catch (IOException e)
        {
            throw new MultipartFileErrorException("File " + name + " caused " + e.toString());
        }
    }
	try
	{
	    output_stream = new FileOutputStream(tmp_file);
	}
	catch (FileNotFoundException e)
	{
	    throw new MultipartFileErrorException("File " + name + " caused " + e.toString());
	}
	output = new BufferedOutputStream(output_stream, 8*1024); // 8K

	long	downloaded_size = 0;
	int		result = -1;
	String	line = null;
	int		line_length = 0;

	// ServletInputStream.readLine() has the annoying habit of
	// adding a \r\n to the end of the last line.
	// Since we want a byte-for-byte transfer, we have to cut those chars.
	boolean rnflag = false;
	try
	{
	    while ((result = mInput.readLine(mFileBuffer, 0, mFileBuffer.length)) != -1)
	    {
		// Check for boundary
		if (result > 2 &&
		    '-' == mFileBuffer[0] &&
		    '-' == mFileBuffer[1])
		{
		    // quick pre-check
		    try
		    {
			line = new String(mFileBuffer, 0, result, mEncoding);
		    }
		    catch (UnsupportedEncodingException e)
		    {
			throw new MultipartFileErrorException("File " + name + " caused " + e.toString());
		    }

		    if(line.startsWith(mBoundary))
		    {
			break;
		    }
		}

		// Are we supposed to write \r\n for the last iteration?
		if (rnflag &&
		    output != null)
		{
		    output.write('\r'); output.write('\n');
		    rnflag = false;
		}

		// postpone any ending \r\n
		if (result >= 2 &&
		    '\r' == mFileBuffer[result-2] &&
		    '\n' == mFileBuffer[result-1])
		{
		    line_length = result-2; // skip the last 2 chars
		    rnflag = true;  // make a note to write them on the next iteration
		}
		else
		{
		    line_length = result;
		}

		// increase size count
		if (output != null &&
		    Config.usizeCheck())
		{
		    downloaded_size += line_length;

		    if (downloaded_size > Config.getUsizeAllow())
		    {
			file.setSizeExceeded(true);
			output.close();
			output = null;
			tmp_file.delete();
			tmp_file = null;

			    throw new MultipartFileTooBigException("File " + name + " exceeds limit of " + Config.getUsizeAllow());

		    }
		}

		// write the content
		if (output != null)
		{
		    output.write(mFileBuffer, 0, line_length);
		}
	    }
	}
	catch (IOException e)
	{
	    throw new MultipartFileErrorException("File " + name + " caused " + e.toString());
	}
	finally
	{
	    try
	    {
		if (output != null)
		{
		    output.flush();
		    output.close();
		    output_stream.close();
		}
	    }
	    catch (IOException e)
	    {
		throw new MultipartFileErrorException("File " + name + " caused " + e.toString());
	    }
	}

	if (tmp_file != null)
	{
	    file.setTempFile(tmp_file);
	}
    }

    public String getTmpFile(){
        return this.tmp_file.getAbsolutePath();
    }


    public class SessionData{

        private int upid = -1;
        private String tmp_filename;
        private Hashtable<String,String> params = new Hashtable<String,String>();
        private int contentLength = -1;

        public int getUpid() {
            return upid;
        }

        public void setUpid(int upid) {
            this.upid = upid;
        }

        public String getTmp_filename() {
            return tmp_filename;
        }

        public void setTmp_filename(String tmp_filename) {
            this.tmp_filename = tmp_filename;
        }

        public Hashtable<String, String> getParams() {
            return params;
        }

        public void setParams(Hashtable<String, String> params) {
            this.params = params;
        }

        public void addParam(String param, String value){
            this.params.put(param,value);
        }

        public String toString(){
            StringBuffer sb = new StringBuffer();
            for ( String key : params.keySet() ){
                sb.append(key + ": " + params.get(key) + "\n");
            }

            if ( tmp_filename != null ) sb.append("TMP_FILENAME: " + tmp_filename);
            return sb.toString();


        }


        public int getContentLength() {
            return contentLength;
        }

        public void setContentLength(int contentLength) {
            this.contentLength = contentLength;
        }
    }
}
