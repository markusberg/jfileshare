<%-- 
    Document   : about
    Created on : 2011-mar-21, 13:04:59
    Author     : markus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%request.setAttribute("tab", "About");%>
<html>
    <head>
        <title>About</title>
    </head>
    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>
        <h1>About jfileshare</h1>
        <p>The jfileshare application enables organizations (large and small)
            to share files with others over the internet. It also has built-in
            support for creating time-limited accounts for external users.
        </p>
        
    </body>
</html>