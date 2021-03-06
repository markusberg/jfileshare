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
 * @author      Markus Berg <markus.berg @ sectra.se>
 * @version     1.6
 * @since       2011-09-21
 */
package nu.kelvin.jfileshare.ajax;

// import javax.xml.bind.annotation.XmlAttribute;
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
