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
<%@page import="nu.kelvin.jfileshare.utils.Helpers"%>
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
        <form action="<%= request.getContextPath()%><%= request.getAttribute("urlPattern") == null ? "/user/view" : request.getAttribute("urlPattern")%>" method="post" autocomplete="off">
            <table>
                <tr>
                    <th>Password: </th>
                    <td><input type="password" class="textentry" name="password1" id="focusme" /></td>
                </tr>
                <tr>
                    <th>Verify password: </th>
                    <td><input type="password" class="textentry" name="password2" /></td>
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

