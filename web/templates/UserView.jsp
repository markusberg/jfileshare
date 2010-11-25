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
                    if (request.getAttribute("oUser") != null) {
                        UserItem oUser = (UserItem) request.getAttribute("oUser");
                        UserItem oCurrentUser = (UserItem) session.getAttribute("user");
                        if (!oUser.getUid().equals(oCurrentUser.getUid())) {
        %>

        <table>
            <tr>
                <th>Username:</th>
                <td><%= oUser.getUsername()%>
                    ( <a href="<%= request.getContextPath()%>/user/edit/<%= oUser.getUid()%>"><img src="<%= request.getContextPath()%>/images/pencil.gif" alt="edit" /> Edit User</a> )
                </td>
            </tr>
            <tr>
                <th>Email:</th>
                <td><a href="mailto:<%=oUser.getEmail()%>"><%=oUser.getEmail()%></a></td>
            </tr>
            <tr>
                <th>User Level:</th>
                <td>
                    <%= oUser.isAdmin() ? "Administrator" : (oUser.isExternal() ? "External" : "Sectra Corporate")%>
                </td>
            </tr>
            <tr>
                <th>User expiration:</th>
                <td><%= oUser.getDateExpiration() == null ? "N/A" : oUser.getDaysUntilExpiration() + " days"%></td>
            </tr>
        </table>
        <%
                                }
                                ArrayList<FileItem> files = (ArrayList<FileItem>) request.getAttribute("aFiles");
                                if (files.isEmpty()) {
        %>
        <p>This user has no files.</p>
        <%                      } else {
        %>
        <%@ include file="/WEB-INF/jspf/FileList.jspf" %>
        <%                                }
                                if (!oCurrentUser.isExternal()) {
        %>
        <h2>Users administered by <%= oUser.getUsername()%></h2>
        <%
                                            ArrayList<UserItem> users = (ArrayList<UserItem>) request.getAttribute("aUsers");
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
