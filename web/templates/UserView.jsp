<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.sectra.jfileshare.objects.UserItem"%>
<%@page import="com.sectra.jfileshare.objects.FileItem"%>
<%@page import="java.util.ArrayList"%>
<html>

    <head>
        <title>Administration</title>
        <link rel="stylesheet" href="<%= request.getContextPath()%>/styles/file.css" type="text/css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/styles/user.css" type="text/css" />
    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>

        <%
                    if (request.getAttribute("user") != null) {
                        UserItem user = (UserItem) request.getAttribute("user");
                        UserItem currentUser = (UserItem) session.getAttribute("user");
                        if (!user.getUid().equals(currentUser.getUid())) {
        %>

        <table>
            <tr>
                <th>Username:</th>
                <td><%= user.getUsername()%>
                    ( <a href="<%= request.getContextPath()%>/user/edit/<%= user.getUid()%>"><img src="<%= request.getContextPath()%>/images/pencil.gif" alt="edit" /> Edit User</a> )
                </td>
            </tr>
            <tr>
                <th>Email:</th>
                <td><a href="mailto:<%=user.getEmail()%>"><%=user.getEmail()%></a></td>
            </tr>
            <tr>
                <th>User Level:</th>
                <td>
                    <%= user.isAdmin() ? "Administrator" : (user.isExternal() ? "External" : "Sectra Corporate")%>
                </td>
            </tr>
            <tr>
                <th>User expiration:</th>
                <td><%= user.getDateExpiration() == null ? "N/A" : user.getDaysUntilExpiration() + " days"%></td>
            </tr>
        </table>
        <%
                                }
                                ArrayList<FileItem> files = (ArrayList<FileItem>) request.getAttribute("files");
                                if (files.isEmpty()) {
        %>
        <p>This user has no files.</p>
        <%                      } else {
        %>
        <%@ include file="/WEB-INF/jspf/FileList.jspf" %>
        <%                                }
                                if (!currentUser.isExternal()) {
        %>
        <h2>Users administered by <%= user.getUsername()%></h2>
        <%
                                            ArrayList<UserItem> users = (ArrayList<UserItem>) request.getAttribute("users");
                                            if (users.isEmpty()) {
        %>
        <p>This user has no child users.</p>
        <%                 } else {
        %>
        <%@include file="/WEB-INF/jspf/UserList.jspf"%>
        <%                 }
                        }
                    }
        %>
    </body>
</html>
