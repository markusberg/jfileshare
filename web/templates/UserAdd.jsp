<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.sectra.jfileshare.objects.Conf"%>
<%@page import="com.sectra.jfileshare.objects.UserItem"%>
<%@page import="com.sectra.jfileshare.utils.Helpers"%>
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

            <table id="singleentry">
                <tr>
                    <th>Username:</th>
                    <td><input type="text" class="textentry" name="username" 
                               value="<%= Helpers.htmlSafe((String) request.getAttribute("validatedUsername"))%>" /></td>
                </tr>
                <tr>
                    <th>Email:</th>
                    <td><input type="text" class="textentry" name="email"
                               value="<%= Helpers.htmlSafe((String) request.getAttribute("validatedEmail"))%>" /></td>
                </tr>
                <tr>
                    <th>Password:</th>
                    <td><input type="password" class="textentry" name="password1"
                               value="<%= Helpers.htmlSafe((String) request.getAttribute("validatedPassword1"))%>" /></td>
                </tr>
                <tr>
                    <th>Verify password:</th>
                    <td><input type="password" class="textentry" name="password2"
                               value="<%= Helpers.htmlSafe((String) request.getAttribute("validatedPassword2"))%>" /></td>
                </tr>

                <tr>
                    <%
                                boolean bExpiration = (Boolean) request.getAttribute("validatedBExpiration");
                                int daysUserExpiration = (Integer) request.getAttribute("validatedDaysUserExpiration");
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
                                                out.print(day.equals(daysUserExpiration) ? " selected=\"selected\"" : "");
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
                            if (currentUser.isAdmin()) {
                                Integer usertype = (Integer) request.getAttribute("validatedUsertype");
                %>
                <tr>
                    <th>User Type:</th>
                    <td>
                        <select name="usertype">
                            <option value="<%=UserItem.TYPE_ADMIN%>"<%=usertype.equals(UserItem.TYPE_ADMIN) ? " selected=\"selected\"" : ""%>>Administrator</option>
                            <option value="<%=UserItem.TYPE_INTERNAL%>"<%=usertype.equals(UserItem.TYPE_INTERNAL) ? " selected=\"selected\"" : ""%>>
                                <%= ((Conf) getServletContext().getAttribute("conf")).getBrandingOrg() %>
                                Corporate</option>
                            <option value="<%=UserItem.TYPE_EXTERNAL%>"<%=usertype.equals(UserItem.TYPE_EXTERNAL) ? " selected=\"selected\"" : ""%>>External</option>
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

