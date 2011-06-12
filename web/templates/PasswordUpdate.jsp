<%-- 
    Document   : PasswordUpdate
    Created on : 2011-mar-26, 18:09:58
    Author     : markus
--%>

<%@page import="com.sectra.jfileshare.utils.Helpers"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<html>
    <head>
        <title>Update password</title>

        <script type="text/javascript">
            function setfocus() {
                domUsername = document.getElementById("focusme");
                domUsername.focus();
            }
            window.onload=setfocus;
        </script>
    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>
        <form action="<%= request.getContextPath()%><%= request.getAttribute("urlPattern") == null ? "/user/view" : request.getAttribute("urlPattern")%>" method="post">
            <table>
                <tr>
                    <th>Password: </th>
                    <td><input type="password" class="textentry" name="password1" value="" id="focusme" /></td>
                </tr>
                <tr>
                    <th>Verify password: </th>
                    <td><input type="password" class="textentry" name="password2" value="" /></td>
                </tr>

                <tr><td>&nbsp;</td></tr>
                <tr>
                    <td></td>
                    <td>
                        <input type="hidden" name="action" value="forcedPasswordUpdate" />
                        <input type="hidden" name="urlPattern" value="<%= request.getAttribute("urlPattern")%>" />
                        <input type="submit" name="update" value="Change password" />
                    </td>
                </tr>
            </table>
        </form>
    </body>
</html>

