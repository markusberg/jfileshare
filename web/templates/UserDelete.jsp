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
<%@page import="com.sectra.jfileshare.objects.Conf"%>
<%@page import="com.sectra.jfileshare.utils.Helpers"%>
<%@page import="com.sectra.jfileshare.objects.UserItem"%>
<%@page import="java.util.ArrayList"%>
<html>
    <head>
        <title>Delete User</title>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/styles/user.css" />
    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>
        <%
            if (request.getAttribute("user") != null) {
                UserItem user = (UserItem) request.getAttribute("user");
        %>
        <h2>Confirm deletion</h2>
        <form action="<%= request.getContextPath()%>/user/delete/<%= user.getUid()%>" method="post">
            <table id="singleentry">
                <tr>
                    <th>Userid: </th><td><%=user.getUid()%></td>
                </tr>
                <tr>
                    <th>Username: </th><td><%= Helpers.htmlSafe(user.getUsername()) %></td>
                </tr>
                <tr>
                    <th>Email: </th><td><%= Helpers.htmlSafe(user.getEmail())%></td>
                </tr>
                <tr>
                    <th>User Level:</th>
                    <td>
                        <%= user.isAdmin() ? "Administrator" : (user.isExternal() ? "External" : ((Conf) getServletContext().getAttribute("conf")).getBrandingOrg() + " Internal")%>
                    </td>
                </tr>
                <tr>
                    <th>Files: </th>
                    <td><%=user.getSumFiles()%> <span class="note"><%=user.getSumFiles()>0 ? "(will be deleted)" : "" %></span></td>
                </tr>
                <tr>
                    <th>Children: </th>
                    <td><%=user.getSumChildren()%> <span class="note"><%=user.getSumChildren()>0 ? "(will be orphaned)" : "" %></span></td>
                </tr>
            </table>

            <p>
                <input type="hidden" name="action" value="confirmdelete" />
                <input type="submit" name="confirm" value="Confirm Delete" />
            </p>
        </form>

        <%
            }
        %>

    </body>
</html>

