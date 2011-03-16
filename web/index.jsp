<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="com.sectra.jfileshare.objects.Conf"%>
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

        <p>Welcome to the 
            <%= ((Conf) getServletContext().getAttribute("conf")).getBrandingCompany() %>
            File Sharing facility. This web application
            is intended for registered users
            only. If you're a
            <%= ((Conf) getServletContext().getAttribute("conf")).getBrandingCompany() %>
            corporate user, or if you already have an
            account you are welcome to login:</p>

        <form action="<%= request.getContextPath()%><%= request.getAttribute("urlPattern") == null ? "/user/view" : request.getAttribute("urlPattern") %>" method="post">
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

