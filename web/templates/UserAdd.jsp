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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="nu.kelvin.jfileshare.objects.Conf"%>
<%@page import="nu.kelvin.jfileshare.objects.UserItem"%>
<%@page import="nu.kelvin.jfileshare.utils.Helpers"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.TreeMap"%>
<html>

    <head>
        <title>Add User</title>
    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>
        <p>Please ensure that all fields are filled out correctly</p>

        <form action="<%= request.getContextPath()%>/user/add" method="post">
            <%
                        UserItem user = (UserItem) request.getAttribute("user");
            %>

            <table id="singleentry">
                <tr>
                    <th>Username:</th>
                    <td><input type="text" class="textentry" name="username" 
                               value="<%= Helpers.htmlSafe(user.getUsername())%>" /></td>
                </tr>
                <tr>
                    <th>Email:</th>
                    <td><input type="text" class="textentry" name="email"
                               value="<%= Helpers.htmlSafe(user.getEmail())%>" /></td>
                </tr>
                <tr>
                    <th>Password:</th>
                    <td><input type="password" class="textentry" name="password1"
                               value="<%= Helpers.htmlSafe((String) request.getAttribute("password1"))%>" /></td>
                </tr>
                <tr>
                    <th>Verify password:</th>
                    <td><input type="password" class="textentry" name="password2"
                               value="<%= Helpers.htmlSafe((String) request.getAttribute("password2"))%>" /></td>
                </tr>

                <tr>
                    <%
                                boolean bExpiration = user.getDateExpiration() != null;
                    %>
                    <th>Expiration:</th>
                    <td><input id="bExpiration" type="checkbox" name="bExpiration" value="true"<%=bExpiration ? " checked=\"checked\"" : ""%> onchange="ToggleVisibility('ExpirationBlock', 'table-row-group');"/></td>
                </tr>
                <tr>
                    <td></td>
                    <td>
                        <div id="ExpirationBlock" style="display: <%= bExpiration ? "block" : "none"%>;">
                            <strong>Account expires in:</strong>
                            <select name="daysUserExpiration">
                                <%
                                            for (Integer day : UserItem.DAY_MAP.keySet()) {
                                                out.print("<option value=\"" + day + "\"");
                                                out.print(day.equals(user.getDaysUntilExpiration()) ? " selected=\"selected\"" : "");
                                                out.print(">" + UserItem.DAY_MAP.get(day) + "</option>\n");
                                            }
                                %>
                            </select>
                            <br />
                            <span class="note">Note: When the account expires, it will be deleted along with any and all uploaded files</span>
                        </div>
                    </td>
                </tr>
                <%
                            UserItem currentUser = (UserItem) session.getAttribute("user");
                            Conf conf = (Conf) getServletContext().getAttribute("conf");
                            if (currentUser.isAdmin()) {
                %>
                <tr>
                    <th>User Type:</th>
                    <td>
                        <select name="usertype">
                            <option value="<%=UserItem.TYPE_ADMIN%>"<%=user.getUserType() == UserItem.TYPE_ADMIN ? " selected=\"selected\"" : ""%>>Administrator</option>
                            <option value="<%=UserItem.TYPE_INTERNAL%>"<%=user.getUserType() == UserItem.TYPE_INTERNAL ? " selected=\"selected\"" : ""%>>
                                <%= Helpers.htmlSafe(conf.getBrandingOrg())%>
                                internal</option>
                            <option value="<%=UserItem.TYPE_EXTERNAL%>"<%=user.getUserType() == UserItem.TYPE_EXTERNAL ? " selected=\"selected\"" : ""%>>External</option>
                        </select>
                    </td>
                </tr>
                <%
                            }
                %>

                <tr><td>&nbsp;</td></tr>
                <tr>
                    <td></td>
                    <td>
                        <input type="hidden" name="action" value="adduser" />
                        <input type="submit" name="submit" value="Add user" />
                    </td>
                </tr>

            </table>
        </form>

    </body>
</html>

