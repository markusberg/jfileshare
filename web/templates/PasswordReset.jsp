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
<%@page import="nu.kelvin.jfileshare.objects.Conf"%>
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
                    <th>New password: </th>
                    <td><input type="password" class="textentry" name="password1" id="FocusMe" /></td>
                </tr>
                <tr>
                    <th>Verify new password: </th>
                    <td><input type="password" class="textentry" name="password2" /></td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td>
                        <input type="hidden" name="action" value="PasswordReset" />
                        <input type="submit" value="Set new password" />
                    </td>
                </tr>
            </table>
        </form>
        <%
                            } else {
        %>

        <form action="<%= request.getContextPath()%>/passwordreset" method="post">
            <p>Use this form if you've forgotten your password.
                If you're a 
                <%= ((Conf) getServletContext().getAttribute("conf")).getBrandingOrg()%>
                internal user, your username is the same as
                your regular login username.
                Instructions will be sent to your email address.
            </p>
            <p><strong>Your username: </strong><input type="text" class="textentry" name="username" id="FocusMe" />
                <input type="submit" value="Reset my password" />
                <input type="hidden" name="action" value="PasswordResetRequest" />
            </p>
        </form>
        <%
                    }
        %>
    </body>
</html>

