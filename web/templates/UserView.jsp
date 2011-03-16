<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.sectra.jfileshare.objects.Conf"%>
<%@page import="com.sectra.jfileshare.objects.UserItem"%>
<%@page import="com.sectra.jfileshare.objects.FileItem"%>
<%@page import="java.util.ArrayList"%>
<html>

    <head>
        <title>Administration</title>
        <link rel="stylesheet" href="<%= request.getContextPath()%>/styles/file.css" type="text/css" />
        <link rel="stylesheet" href="<%= request.getContextPath()%>/styles/user.css" type="text/css" />

        <script type="text/javascript">

            function processXMLResponse() {
                if ( oAjax.readyState == 4 && oAjax.status == 200 ) {
                    var xml = oAjax.responseXML;
                    var status = xml.getElementsByTagName("status")[0].firstChild.data;
                    if (status=="sessionexpired") {
                        LogoutTimer.forceLogout();
                    } else {
                        var msg = xml.getElementsByTagName("msg")[0].firstChild.data;
                        var stacktrace = '';
                        var xmlStacktrace = xml.getElementsByTagName("stacktrace")[0];
                        if (xmlStacktrace != undefined) {
                            stacktrace = xmlStacktrace.firstChild.data;
                        }
                        generateMessageBox(status, msg, stacktrace);
                    }
                    oAjax = null;
                }
            }

            function generateMessageBox(status, msg, stacktrace) {
                var messageBox = document.createElement('div');
                messageBox.setAttribute('id', 'messagebox_'+status);
                messageBox.innerHTML = msg;
                if (stacktrace != '') {
                    var strace = document.createElement('pre');
                    strace.innerHTML = stacktrace;
                    messageBox.appendChild(strace);
                }
                domMessageBoxes.appendChild(messageBox);
                LogoutTimer.restart();
            }

            function fileNotify(fid) {
                // Clear any previous status messages
                domMessageBoxes = document.getElementById("MessageBoxes");
                domMessageBoxes.innerHTML = "";
                var domEmailRecipient = document.getElementById("email_"+fid);

                oAjax = getAjaxObject();
                oAjax.onreadystatechange = processXMLResponse;
                var url = "<%= request.getContextPath()%>/ajax/file/notification";
                var params = "iFid="+fid+"&emailRecipient="+domEmailRecipient.value;
                oAjax.open("POST", url, true);
                oAjax.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
                oAjax.send(params);
            }

            var domMessageBoxes;
            var oAjax;

        </script>

    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>
        <%
                    if (request.getAttribute("user") != null) {
                        UserItem user = (UserItem) request.getAttribute("user");
                        UserItem currentUser = (UserItem) session.getAttribute("user");
                        if (!user.getUid().equals(currentUser.getUid())) {
        %>

        <table>
            <tr>
                <th>Username:</th>
                <td><%= user.getUsername()%>
                    ( <a href="<%= request.getContextPath()%>/user/edit/<%= user.getUid()%>"><img src="<%= request.getContextPath()%>/images/pencil.gif" alt="edit" /> Edit User</a> )
                </td>
            </tr>
            <tr>
                <th>Email:</th>
                <td><a href="mailto:<%=user.getEmail()%>"><%=user.getEmail()%></a></td>
            </tr>
            <tr>
                <th>User Level:</th>
                <td>
                    <%= user.isAdmin() ? "Administrator" : (user.isExternal() ? "External" : ((Conf) getServletContext().getAttribute("conf")).getBrandingCompany() + " Corporate")%>
                </td>
            </tr>
            <tr>
                <th>User expiration:</th>
                <td><%= user.getDateExpiration() == null ? "N/A" : user.getDaysUntilExpiration() + " days"%></td>
            </tr>
        </table>
        <%
                                }
                                ArrayList<FileItem> files = (ArrayList<FileItem>) request.getAttribute("files");
                                if (files.isEmpty()) {
        %>
        <p>This user has no files.</p>
        <%                      } else {
        %>
        <%@ include file="/WEB-INF/jspf/FileList.jspf" %>
        <%                                }
                                if (!currentUser.isExternal()) {
        %>
        <h2>Users administered by <%= user.getUsername()%></h2>
        <%
                                            ArrayList<UserItem> users = (ArrayList<UserItem>) request.getAttribute("users");
                                            if (users.isEmpty()) {
        %>
        <p>This user has no child users.</p>
        <%                 } else {
        %>
        <%@include file="/WEB-INF/jspf/UserList.jspf"%>
        <%                 }
                        }
                    }
        %>
    </body>
</html>
