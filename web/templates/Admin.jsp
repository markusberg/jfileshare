<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.sectra.jfileshare.objects.Conf"%>
<%@page import="com.sectra.jfileshare.objects.FileItem"%>
<html>
    <head>
        <title>Admin</title>
    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>

        <%
                    Conf conf = (Conf) getServletContext().getAttribute("conf");
        %>
        <p>Note that this is still only experimental. No changes made here will
            actually be saved anywhere.</p>
        <form action="<%= request.getContextPath()%>/admin" method="post">
            <table>
                <tr>
                    <th>Path to filestore: </th>
                    <td><input type="text" value="<%= conf.getPathStore()%>" /></td>
                </tr>
                <tr>
                    <th>Path to tempstore: </th>
                    <td><input type="text" value="<%= conf.getPathTemp()%>" /></td>
                </tr>
                <tr>
                    <th>Smtp server and port: </th>
                    <td><input type="text" value="<%= conf.getSmtpServer()%>" />
                        <input type="text" value="<%= Integer.toString(conf.getSmtpServerPort())%>" size="4" />
                    </td>
                </tr>
                <tr>
                    <th>File retention: </th>
                    <td><input type="text" value="<%= Integer.toString(conf.getDaysFileRetention())%>" size="4" /> days</td>
                </tr>
                <tr>
                    <th>Default user expiration: </th>
                    <td><input type="text" value="<%= Integer.toString(conf.getDaysUserExpiration())%>" size="4" /> days</td>
                </tr>
                <tr>
                    <th>Maximum allowed file size: </th>
                    <td><input type="text" value="<%= conf.getFileSizeMax()%>" /> bytes</td>
                </tr>
                <tr><td>&nbsp;</td></tr>
                <tr>
                    <td></td>
                    <td><input type="submit" name="submit" value="Save & apply" /></td>
                </tr>
            </table>
        </form>
    </body>
</html>

