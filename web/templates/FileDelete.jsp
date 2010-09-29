<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.sectra.jfileshare.objects.FileItem"%>
<%@page import="com.sectra.jfileshare.objects.UserItem"%>
<%@page import="com.sectra.jfileshare.utils.Helpers"%>
<html>

    <head>
        <title>Delete File</title>
        <link rel="stylesheet" href="<%= request.getContextPath()%>/styles/file.css" type="text/css" />
    </head>

    <body>
        <%@ include file="/WEB-INF/jspf/MessageBoxes.jspf" %>

        <%
            if (request.getAttribute("oFile") != null) {
                FileItem oFile = (FileItem) request.getAttribute("oFile");
        %>
        <%@ include file="/WEB-INF/jspf/SingleFile.jspf" %>

        <form action="<%= request.getContextPath()%>/file/delete/<%= oFile.getFid()%>" method="post">
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

