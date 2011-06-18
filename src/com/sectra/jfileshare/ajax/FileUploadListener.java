package com.sectra.jfileshare.ajax;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.fileupload.ProgressListener;

/**
 * This is a File Upload Listener that is used by Apache
 * Commons File Upload to monitor the progress of the
 * uploaded file.
 *
 * @author markus
 */
@XmlRootElement()
public class FileUploadListener implements ProgressListener {

    private volatile long bytesRead = 0L,
            contentLength = 0L,
            item = 0L;

    public FileUploadListener() {
        super();
    }

    @Override
    public void update(long aBytesRead, long aContentLength,
            int anItem) {
        bytesRead = aBytesRead;
        contentLength = aContentLength;
        item = anItem;
    }

    @XmlElement()
    public long getBytesRead() {
        return bytesRead;
    }

    @XmlElement()
    public long getContentLength() {
        return contentLength;
    }

    public long getItem() {
        return item;
    }
}
