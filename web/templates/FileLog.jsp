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
<%@page import="nu.kelvin.jfileshare.objects.FileItem"%>
<%@page import="nu.kelvin.jfileshare.objects.FileLog"%>
<%@page import="nu.kelvin.jfileshare.utils.Helpers"%>
<%@page import="java.util.ArrayList"%>

<html>

    <head>
        <title>File Download Log</title>
        <link rel="stylesheet" href="<%= request.getContextPath()%>/styles/file.css" type="text/css" />
    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>

        <%
                    if (request.getAttribute("file") != null) {
                        FileItem file = (FileItem) request.getAttribute("file");
                        ArrayList<FileLog> downloadLogs = (ArrayList<FileLog>) request.getAttribute("downloadLogs");
                        if (!downloadLogs.isEmpty()) {
                            boolean even = false;
        %>
        <p>The file <strong>"<%= Helpers.htmlSafe(file.getName())%>"</strong> has been downloaded <%=downloadLogs.size()%> <%=downloadLogs.size() == 1 ? "time" : "times"%>.</p>
        <table id="files">
            <tr>
                <th>Time</th><th>IP</th>
            </tr>
            <%
                                        for (FileLog log : downloadLogs) {
            %>
            <tr class="<%= even ? "even" : "odd"%>">
                <td><%=Helpers.formatDate(log.getTime())%></td>
                <td><a href="http://www.ripe.net/perl/whois?form_type=simple&amp;full_query_string=&amp;searchtext=<%=log.getIp()%>&amp;do_search=Search"><%=log.getIp()%></a></td>
            </tr>
            <%
                                            even = !even;
                                        }
            %>
        </table>
        <%
                        }
                    }
        %>
    </body>

</html>

