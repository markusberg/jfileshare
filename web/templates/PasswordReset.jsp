<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>

    <head>
        <title>Reset your password</title>

        <script type="text/javascript">
            function setfocus() {
                domUsername = document.getElementById("FocusMe");
                domUsername.focus();
            }
            window.onload=setfocus;
        </script>

    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>
        <%
                    String username = (String) request.getAttribute("username");
                    if (username != null) {
        %>
        <form action="<%= request.getContextPath()%>/passwordreset/<%=request.getAttribute("key")%>" method="post">
            <p>Reset password for user <strong><%=username%></strong>:</p>
            <table>
                <tr>
                    <td style="text-align: right;">New password: </td>
                    <td><input type="password" name="password1" id="FocusMe" /></td>
                </tr>
                <tr>
                    <td style="text-align: right;">Verify new password: </td>
                    <td><input type="password" name="password2" /></td>
                </tr>
            </table>
            <input type="hidden" name="action" value="PasswordReset" />
            <input type="submit" value="Set new password" />
        </form>
        <%
                            } else {
        %>

        <form action="<%= request.getContextPath()%>/passwordreset" method="post">
            <p>Use this form if you've forgotten your password.
                If you're a Sectra corporate user, your username is the same as 
                your regular login username.
            </p>
            <p>Instructions will be sent to your email address.
            </p>
            <p>Your username: <input type="text" name="username" id="FocusMe" />
                <input type="submit" value="Reset my password" />
                <input type="hidden" name="action" value="PasswordResetRequest" />
            </p>
        </form>
        <%
                    }
        %>
    </body>
</html>

