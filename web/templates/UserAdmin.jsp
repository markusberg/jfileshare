<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.sectra.jfileshare.objects.UserItem"%>
<%@page import="com.sectra.jfileshare.objects.FileItem"%>
<%@page import="java.util.ArrayList"%>
<html>

    <head>
        <title>User Administration</title>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/styles/user.css" />
    </head>

    <body>
        <%@ include file="/WEB-INF/jspf/MessageBoxes.jspf" %>
        <%
                    ArrayList<UserItem> aUsers = (ArrayList<UserItem>) request.getAttribute("aUsers");
                    if (aUsers.size() > 0) {
        %>
        <%@ include file="/WEB-INF/jspf/UserList.jspf" %>
        <%          } else {
        %>
        <p>No users found. That's very strange. Who are you?</p>
        <%          }
        %>

    </body>
</html>

