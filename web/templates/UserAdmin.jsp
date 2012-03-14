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
<%@page import="nu.kelvin.jfileshare.objects.UserItem"%>
<%@page import="nu.kelvin.jfileshare.objects.FileItem"%>
<%@page import="java.util.ArrayList"%>
<html>
    <head>
        <title>User Administration</title>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/styles/user.css" />
    </head>

    <body>
        <%@ include file="/WEB-INF/jspf/MessageBoxes.jspf" %>
        <%
                    ArrayList<UserItem> users = (ArrayList<UserItem>) request.getAttribute("users");
                    if (users.size() > 0) {
        %>
        <%@ include file="/WEB-INF/jspf/UserList.jspf" %>
        <%          } else {
        %>
        <p>No users found. That's very strange. Who are you?</p>
        <%          }
        %>

    </body>
</html>

