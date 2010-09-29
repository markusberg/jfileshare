<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%request.setAttribute("tab", "404");%>
<html>
    <head>
        <title>Resource not found</title>
    </head>
    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>
    </body>
</html>