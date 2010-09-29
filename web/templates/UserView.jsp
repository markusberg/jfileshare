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
        %>

        <h2>User information page</h2>
        <table id="singleentry">
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

        <h2>Files owned by <%= oUser.getUsername()%></h2>
        <%
                                ArrayList<FileItem> aFiles = (ArrayList<FileItem>) request.getAttribute("aFiles");
                                if (aFiles.size() > 0) {
        %>
        <%@ include file="/WEB-INF/jspf/FileList.jspf" %>
        <%                      } else {
        %>
        <p>This user has no files.</p>
        <%                                }

                                if (!oCurrentUser.isExternal()) {
        %>
        <h2>Users administered by <%= oUser.getUsername()%></h2>
        <%
                                            ArrayList<UserItem> aUsers = (ArrayList<UserItem>) request.getAttribute("aUsers");
                                            if (aUsers.size() > 0) {
        %>
        <%@include file="/WEB-INF/jspf/UserList.jspf"%>
        <%                 } else {
        %>
        <p>This user has no child users.</p>
        <%                 }
                        }
                    }
        %>

    </body>
</html>
