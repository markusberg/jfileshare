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
package nu.kelvin.jfileshare.objects;

import java.util.Date;

public class FileLog {
    private String ipAddress;
    private Date dateAccess;

    FileLog(Date dateAccess, String ipAddress) {
        this.dateAccess = dateAccess;
        this.ipAddress = ipAddress;
    }

    public String getIp() {
        return this.ipAddress;
    }

    public Date getTime() {
        return this.dateAccess;
    }
}
