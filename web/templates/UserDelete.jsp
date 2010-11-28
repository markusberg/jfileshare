<%@page contentType="text/html" pageEncoding="UTF-8"%>
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
                    <th>Username: </th><td><%=user.getUsername()%></td>
                </tr>
                <tr>
                    <th>Email: </th><td><%=user.getEmail()%></td>
                </tr>
                <tr>
                    <th>User Level:</th>
                    <td>
                        <%= user.isAdmin() ? "Administrator" : (user.isExternal() ? "External" : "Sectra Corporate")%>
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

