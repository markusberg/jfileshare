<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>

    <head>
        <title>Login</title>

        <script type="text/javascript">
            function setfocus() {
                domUsername = document.getElementById("username");
                domUsername.focus();
            }
            window.onload=setfocus;
        </script>
    </head>

    <body>
        <%@ include file="/WEB-INF/jspf/MessageBoxes.jspf" %>

        <form action="<%= request.getContextPath()%><%=request.getAttribute("urlPattern")%>" method="post">
            <table>
                <tr>
                    <td>Username: </td><td><input type="text" class="textentry" name="login_username" id="username" /></td>
                </tr>
                <tr>
                    <td>Password: </td><td><input type="password" class="textentry" name="login_password" /></td>
                </tr>
                <tr>
                    <td colspan=2>
                        <input type="hidden" name="action" value="login" />
                        <input type="submit" name="submit" value="Login" />
                    </td>
                </tr>
            </table>
        </form>
    </body>
</html>

