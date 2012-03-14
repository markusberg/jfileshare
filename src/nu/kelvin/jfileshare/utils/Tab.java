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
package nu.kelvin.jfileshare.utils;

/**
 * Helper class for the tabbed interface
 * @author markus
 */
public class Tab {

    private String sTitle;
    private String urlLink;
    private boolean bSelected;
    private boolean bEnabled;

    public void Tab() {
    }

    public Tab(String sTitle, String urlLink, boolean bSelected, boolean bEnabled) {
        this.sTitle = sTitle;
        this.urlLink = urlLink;
        this.bSelected = bSelected;
        this.bEnabled = bEnabled;
    }

    public String getTitle() {
        return this.sTitle;
    }

    public String getLink() {
        return this.urlLink;
    }

    public boolean isSelected() {
        return this.bSelected;
    }

    public boolean isEnabled() {
        return this.bEnabled;
    }
}


