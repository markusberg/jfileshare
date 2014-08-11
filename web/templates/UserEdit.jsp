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
<html>

    <head>
        <title>Edit User</title>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/styles/user.css" />
    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>
        <%
                    UserItem currentUser = (UserItem) session.getAttribute("user");
                    UserItem user = (UserItem) request.getAttribute("user");
                    Conf conf = (Conf) getServletContext().getAttribute("conf");
        %>

        <form action="<%= request.getContextPath()%>/user/edit/<%=user.getUid()%>" method="post">
            <input type="hidden" name="CSRFToken" value="<%=currentUser.getCSRFToken()%>" />
            <table id="singleentry">
                <tr>
                    <th>Userid: </th><td><%= user.getUid()%></td>
                </tr>
                <tr>
                    <th>Username: </th>
                    <td>
                        <%
                                    if (currentUser.isAdmin()) {

                        %>
                        <input type="text" class="textentry" name="username" value="<%= Helpers.htmlSafe(user.getUsername())%>" />
                        <%
                                    } else {
                                        out.print(Helpers.htmlSafe(user.getUsername()));
                                    }
                        %>
                    </td>
                </tr>
                <tr>
                    <th>Email: </th>
                    <td><input type="text" class="textentry" name="email" value="<%= Helpers.htmlSafe(user.getEmail())%>" /></td>
                </tr>
                <tr>
                    <th>Password: </th>
                    <td><input type="password" class="textentry" name="password1" value="<%= Helpers.htmlSafe((String) request.getAttribute("password1"))%>"/>
                        <span class="note">Note: leave blank in order to keep existing password unchanged</span></td>
                </tr>
                <tr>
                    <th>Verify password: </th>
                    <td><input type="password" class="textentry" name="password2" value="<%= Helpers.htmlSafe((String) request.getAttribute("password2"))%>"/></td>
                </tr>

                <%
                            boolean bExpiration = user.getDateExpiration() != null;
                            int daysUntilExpiration = bExpiration
                                    ? user.getDaysUntilExpiration()
                                    : conf.getDaysUserExpiration();

                            // Disallow setting expiration on yourself
                            // Unless you're an admin
                            if (currentUser.isAdmin() || currentUser.isParentTo(user)) {
                %>
                <tr>
                    <th>Expiration:</th>
                    <td>
                        <input id="bExpiration" type="checkbox" name="bExpiration" value="true"<%=bExpiration ? " checked=\"checked\"" : ""%> onchange="ToggleVisibility('ExpirationBlock', 'table-row-group');"/>
                    </td>
                </tr>
                <tr>
                    <td></td>
                    <td>
                        <div id="ExpirationBlock" style="display: <%= bExpiration ? "block" : "none"%>;">
                            Account will expire in
                            <input type="text" class="intentry" name="daysUntilExpiration" value="<%= daysUntilExpiration%>"> days
                            <br />
                            <span class="note">Note: When the account expires, it will be deleted along with any and all uploaded files</span>
                        </div>
                    </td>
                </tr>
                <%
                                            } else {
                %>
                <tr>
                    <th>Expiration:</th>
                    <td><%= bExpiration ? "Account will expire in " + daysUntilExpiration + " days" : "Account does not expire"%></td>
                </tr>
                <%
                            }
                            if (currentUser.isAdmin()) {
                                int usertype = user.getUserType();
                %>
                <tr>
                    <th>User Type:</th>
                    <td>
                        <select name="usertype">
                            <option value="<%=UserItem.TYPE_ADMIN%>"<%=usertype == UserItem.TYPE_ADMIN ? " selected=\"selected\"" : ""%>>Administrator</option>
                            <option value="<%=UserItem.TYPE_INTERNAL%>"<%=usertype == UserItem.TYPE_INTERNAL ? " selected=\"selected\"" : ""%>>
                                <%= Helpers.htmlSafe(conf.getBrandingOrg())%>
                                internal</option>
                            <option value="<%=UserItem.TYPE_EXTERNAL%>"<%=usertype == UserItem.TYPE_EXTERNAL ? " selected=\"selected\"" : ""%>>External</option>
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
                        <input type="hidden" name="action" value="updateuser" />
                        <input type="submit" name="update" value="Update user" />
                    </td>
                </tr>
            </table>
        </form>
    </body>
</html>

