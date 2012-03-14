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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="nu.kelvin.jfileshare.objects.Conf"%>
<%@page import="nu.kelvin.jfileshare.objects.FileItem"%>
<%@page import="nu.kelvin.jfileshare.objects.UserItem"%>
<%@page import="nu.kelvin.jfileshare.utils.Helpers"%>
<html>

    <head>
        <title>Download File</title>
        <link rel="stylesheet" href="<%= request.getContextPath()%>/styles/file.css" type="text/css" />
    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>
        <%
                    if (request.getAttribute("file") != null) {
                        FileItem file = (FileItem) request.getAttribute("file");
        %>
        <table id="singlefile">
            <tr>
                <th>Filename: </th><td><%= Helpers.htmlSafe(file.getName())%></td>
            </tr>
            <tr>
                <th>Size: </th><td><%= FileItem.humanReadable(file.getSize())%></td>
            </tr>
            <tr>
                <th>Type: </th><td><%= Helpers.htmlSafe(file.getType())%></td>
            </tr>
            <tr>
                <th>MD5: </th><td><%= file.getMd5sum()%></td>
            </tr>
            <tr>
                <th>Owner: </th>
                <td>
                    <a href="mailto:<%= Helpers.htmlSafe(file.getOwnerEmail())%>"><%= Helpers.htmlSafe(file.getOwnerEmail())%></a>
                </td>
            </tr>
            <tr>
                <th>Upload date: </th><td><%= Helpers.formatDate(file.getDateCreation())%></td>
            </tr>
            <%
                            if (session.getAttribute("user") != null) {
                                UserItem user = (UserItem) session.getAttribute("user");
                                if (user.hasEditAccessTo(file)) {
                                    int daysFileExpiration = ((Conf) getServletContext().getAttribute("conf")).getDaysFileExpiration();
                                    if (daysFileExpiration != 0 || !file.isPermanent()) {
            %>
            <tr class="admin">
                <th>File expiration: </th>
                <td><%= file.isPermanent() ? "No" : file.getDaysUntilExpiration() + " days"%></td>
            </tr>
            <%
                                    }
            %>

            <tr class="admin">
                <th>Password protected: </th>
                <td><%= file.getPwHash() == null ? "No" : "Yes"%></td>
            </tr>
            <tr class="admin">
                <th>Downloads enabled: </th><td><%= file.isEnabled() ? "Yes" : "No"%></td>
            </tr>
            <tr class="admin">
                <th>Number of downloads: </th>
                <td>
                    <%= file.getDownloads() == null ? "Unlimited" : file.getDownloads()%>
                </td>
            </tr>

            <%
                                }
                            }
            %>
        </table>

        <p><a href="<%= request.getContextPath()%>/file/download/<%=file.getFid()%>?md5=<%=file.getMd5sum()%>">Download file</a></p>
        <%
                    }
        %>
    </body>

</html>

