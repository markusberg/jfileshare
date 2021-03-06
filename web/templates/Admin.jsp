<%--
   Copyright 2011 SECTRA Imtec AB

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--%>
<%@page import="nu.kelvin.jfileshare.utils.Helpers"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="nu.kelvin.jfileshare.objects.Conf"%>
<%@page import="nu.kelvin.jfileshare.objects.FileItem"%>
<%@page import="nu.kelvin.jfileshare.objects.UserItem"%>
<html>
    <head>
        <title>Admin</title>
    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>

        <%
                    Conf conf = (Conf) getServletContext().getAttribute("conf");
                    UserItem currentUser = (UserItem) session.getAttribute("user");
        %>
        <form action="<%= request.getContextPath()%>/admin" method="post">
            <input type="hidden" name="CSRFToken" value="<%=currentUser.getCSRFToken()%>" />
            <table>
                <tr>
                    <td><h3>Branding</h3></td>
                </tr>
                <tr>
                    <th>Organization name: </th>
                    <td>
                        <input type="text" class="textentry" name="brandingOrg" value="<%= Helpers.htmlSafe(conf.getBrandingOrg())%>" />
                    </td>
                    <td>
                        <span class="note">
                            The organization name will be displayed in the user interface, and in
                            emails sent from the jfileshare app
                        </span>
                    </td>
                </tr>
                <tr>
                    <th>Domain name: </th>
                    <td>
                        <input type="text" class="textentry" name="brandingDomain" value="<%= Helpers.htmlSafe(conf.getBrandingDomain())%>" />
                    </td>
                    <td>
                        <span class="note">
                            This domain name will be used during auto-creation of accounts. If user <strong>"xyz"</strong>
                            requests a password reset, and that user doesn't exist in the database, the reset instructions are
                            sent to xyz@&lt;domain-name&gt;.
                        </span>
                    </td>
                </tr>
                <tr>
                    <th>Logo url: </th>
                    <td>
                        <input type="text" class="textentry" name="brandingLogo" value="<%= conf.getBrandingLogo() == null ? "" : Helpers.htmlSafe(conf.getBrandingLogo())%>" />
                    </td>
                    <td>
                        <span class="note">
                            Leave blank to use the default logo
                        </span>
                    </td>
                </tr>
                <tr>
                    <td colspan="2"><h3>Storage settings</h3></td>
                </tr>
                <tr>
                    <th>Path to filestore: </th>
                    <td><input type="text" class="textentry" name="pathStore" value="<%= Helpers.htmlSafe(conf.getPathStore())%>" /></td>
                </tr>
                <tr>
                    <th>Path to tempstore: </th>
                    <td><input type="text" class="textentry" name="pathTemp" value="<%= Helpers.htmlSafe(conf.getPathTemp())%>" /></td>
                </tr>
                <tr>
                    <th>Files expire after: </th>
                    <td><input type="text" class="intentry" name="daysFileExpiration" value="<%= conf.getDaysFileExpiration() == 0 ? "" : conf.getDaysFileExpiration()%>" /> days</td>
                    <td><span class="note">Leave blank to disable file expiration</span></td>
                </tr>
                <tr>
                    <th>Maximum allowed file size: </th>
                    <td>
                        <%
                                    int[] filesize = FileItem.getFileSize(conf.getFileSizeMax());
                        %>
                        <input type="text" class="intentry" name="fileSizeMax" value="<%= filesize[1]%>" />
                        <select name="fileSizeUnit">
                            <option value="1" <%= filesize[0] == 1 ? "selected=\"selected\"" : ""%>>KiB</option>
                            <option value="2" <%= filesize[0] == 2 ? "selected=\"selected\"" : ""%>>MiB</option>
                            <option value="3" <%= filesize[0] == 3 ? "selected=\"selected\"" : ""%>>GiB</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <th>File auto-expiration after: </th>
                    <td><input type="text" class="intentry" name="monthsFileAutoExpiration" value="<%= conf.getMonthsFileAutoExpiration() == 0 ? "" : conf.getMonthsFileAutoExpiration()%>" /> months</td>
                    <td><span class="note">A file which hasn't been downloaded or modified in this many months will have its expiration flag automatically set, and an email will be sent to its owner informing about this. Leave blank to disable file auto-expiration</span></td>
                </tr>
                <tr>
                    <td colspan="2"><h3>Mail server settings</h3></td>
                </tr>
                <tr>
                    <th>Smtp server and port: </th>
                    <td><input class="textentry" type="text" name="smtpServer" value="<%= conf.getSmtpServer()%>" /></td>
                    <td><input class="intentry" type="text" name="smtpServerPort" value="<%= conf.getSmtpServerPort()%>" /></td>
                </tr>
                <tr>
                    <th>Smtp sender: </th>
                    <td><input type="text" class="textentry" name="smtpSender" value="<%= conf.getSmtpSender()%>" /></td>
                </tr>
                <tr>
                    <td colspan="2"><h3>User settings</h3></td>
                </tr>
                <tr>
                    <th>Default user expiration: </th>
                    <td>
                        <select name="daysUserExpiration">
                            <%
                                        int daysUserExpiration = conf.getDaysUserExpiration();
                                        for (int day : UserItem.DAY_MAP.keySet()) {
                                            out.print("<option value=\"" + day + "\"");
                                            out.print(day == daysUserExpiration ? " selected=\"selected\"" : "");
                                            out.print(">" + UserItem.DAY_MAP.get(day) + "</option>\n");
                                        }
                            %>
                        </select>
                    </td>
                </tr>
                <tr>
                    <th>User password expires after: </th>
                    <td><input type="text" class="intentry" name="daysPasswordExpiration" value="<%= conf.getDaysPasswordExpiration() == 0 ? "" : conf.getDaysPasswordExpiration()%>" /> days</td>
                    <td><span class="note">Leave blank to disable password expiration</span></td>
                </tr>
                <tr>
                    <td colspan="2"><h3>Other</h3></td>
                </tr>
                <tr>
                    <th>Debug:</th>
                    <td><input type="checkbox" name="debug" value="true" <%= conf.getDebug() ? "checked=\"checked\"" : ""%>/></td>
                </tr>
                <tr>
                    <th>Database version:</th>
                    <td><%= conf.getDbVersion()%></td>
                </tr>
                <tr><td>&nbsp;</td></tr>
                <tr>
                    <td></td>
                    <td><input type="submit" name="submit" value="Save & apply" /></td>
                </tr>
            </table>
        </form>
    </body>
</html>

