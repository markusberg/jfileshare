<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.sectra.jfileshare.objects.FileItem"%>
<%@page import="com.sectra.jfileshare.utils.Helpers"%>
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
                        ArrayList<FileItem.DownloadLog> downloadLogs = (ArrayList<FileItem.DownloadLog>) request.getAttribute("downloadLogs");
                        if (!downloadLogs.isEmpty()) {
                            boolean even = false;
        %>
        <p>The file <strong>"<%= file.getName()%>"</strong> has been downloaded <%=downloadLogs.size()%> <%=downloadLogs.size() == 1 ? "time" : "times"%>.</p>
        <table id="files">
            <tr>
                <th>Time</th><th>IP</th>
            </tr>
            <%
                                        for (FileItem.DownloadLog log : downloadLogs) {
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

