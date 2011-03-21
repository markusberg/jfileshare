<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%request.setAttribute("tab", "About");%>
<html>
    <head>
        <title>About</title>
    </head>
    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>
        <h1>About jfileshare</h1>
        <p>Jfileshare enables simple and secure file transfers over the internet.
        There is built-in functionality for creating time-limited accounts for
        external parties to transfer files to you as well.</p>
    </body>
</html>
