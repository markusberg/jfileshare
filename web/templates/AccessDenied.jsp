<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%request.setAttribute("tab", "Access denied");%>
<html>
    <head>
        <title>Access Denied</title>
    </head>
    <body>
        <%@ include file="/WEB-INF/jspf/MessageBoxes.jspf" %>
    </body>
</html>