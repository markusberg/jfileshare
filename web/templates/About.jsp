<%-- 
   Copyright 2011 SECTRA Imtec AB

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
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

        <%
                    if (session.getAttribute("user") != null) {
        %>
        <h1>Stats</h1>
        <p>In the last <%=request.getAttribute("daysLogRetention")%> days, there have been:</p>
        <ul>
            <li><%=request.getAttribute("logins")%> logins by
                <%=request.getAttribute("uniqueLogins")%> different users</li>
            <li><%=request.getAttribute("uploads")%> files uploaded to the server
                (<%=request.getAttribute("bytesUploads")%>)</li>
            <li><%=request.getAttribute("downloads")%> downloads from the server
                (<%=request.getAttribute("bytesDownloads")%>)</li>
        </ul>
        <%                        }
        %>

    </body>
</html>
