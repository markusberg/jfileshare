<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.sectra.jfileshare.objects.FileItem"%>
<%@page import="com.sectra.jfileshare.objects.UserItem"%>
<%@page import="com.sectra.jfileshare.utils.Helpers"%>
<html>

    <head>
        <title>Download File</title>
        <link rel="stylesheet" href="<%= request.getContextPath()%>/styles/file.css" type="text/css" />
    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>
        <%
            if (request.getAttribute("oFile") != null) {
                FileItem oFile = (FileItem) request.getAttribute("oFile");
        %>
        <%@include file="/WEB-INF/jspf/SingleFile.jspf"%>
        <p><a href="<%= request.getContextPath()%>/file/download/<%=oFile.getFid()%>?md5=<%=oFile.getMd5sum()%>">Download file</a></p>
        <%
            }
        %>
    </body>

</html>

