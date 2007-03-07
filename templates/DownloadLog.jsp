<%@ page import="objects.FileItem" %>
<%@ page import="java.util.Set" %>
<%--
  Created by IntelliJ IDEA.
  User: zoran
  Date: Mar 7, 2007
  Time: 9:13:42 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>DownloadLog</title>
      <link rel="stylesheet" href="/styles/admin.css" type="text/css" />
  </head>
  <body>
  <%
      if (request.getAttribute("downloadlogs") != null && ((Set<FileItem.DownloadLog>) request.getAttribute("downloadlogs")).size() > 0 ) {
          Set<FileItem.DownloadLog> logs = (Set<FileItem.DownloadLog>) request.getAttribute("downloadlogs");
          %>
            <table cellpadding="0" cellspacing="0" class="logs">
                <tr>
                    <td>Time</td><td>IP</td>
                </tr>
                <%
                    String order="even";
                    for ( FileItem.DownloadLog log: logs){
                        %>
                    <tr class="<%=order%>">
                        <td><%=utils.Helpers.formatDate(log.getTime())%></td><td><a href="http://www.ripe.net/perl/whois?form_type=simple&full_query_string=&searchtext=<%=log.getIp()%>&do_search=Search"><%=log.getIp()%></a></td>
                    </tr>
                <%
                        if ( order.equals("even")){
                            order = "odd";
                        } else {
                            order = "even";
                        }
                    }
                %>
            </table>
  <%

      } else {
  %>No logs found for this file<%
      }
  %>
  </body>
</html>