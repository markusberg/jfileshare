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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="nu.kelvin.jfileshare.objects.Conf"%>
<html>

    <head>
        <title>Login</title>

        <script type="text/javascript">
            function setfocus() {
                domUsername = document.getElementById("focusme");
                domUsername.focus();
            }
            window.onload=setfocus;
        </script>
    </head>

    <body>
        <%@ include file="/WEB-INF/jspf/MessageBoxes.jspf" %>

        <p>Welcome to the 
            <%= ((Conf) getServletContext().getAttribute("conf")).getBrandingOrg()%>
            File Sharing facility. This web application
            is intended for registered users
            only. If you're a
            <%= ((Conf) getServletContext().getAttribute("conf")).getBrandingOrg()%>
            internal user, or if you already have an
            account you are welcome to login:</p>

        <form action="<%= request.getContextPath()%><%= request.getAttribute("urlPattern") == null ? "/user/view" : request.getAttribute("urlPattern")%>" method="post">
            <table>
                <tr>
                    <th>Username: </th>
                    <td><input type="text" class="textentry" name="login_username" id="focusme" /></td>
                </tr>
                <tr>
                    <th>Password: </th>
                    <td><input type="password" class="textentry" name="login_password" /></td>
                </tr>
                <tr><td>&nbsp;</td></tr>
                <tr>
                    <td>&nbsp;</td>
                    <td>
                        <input type="hidden" name="action" value="login" />
                        <input type="submit" name="submit" value="Login" />
                    </td>
                </tr>
            </table>
        </form>
    </body>
</html>

