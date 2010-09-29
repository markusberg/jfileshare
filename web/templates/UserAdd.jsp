<%@page contentType="text/html" pageEncoding="UTF-8"%>
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
        <p>It's important that all fields are filled out correctly</p>

        <form action="<%= request.getContextPath()%>/user/add" method="post">

            <table id="singleentry">
                <tr>
                    <th>Username:</th>
                    <td><input type="text" class="textentry" name="username" value="<%=Helpers.htmlSafe(request.getParameter("username"))%>"/></td>
                </tr>
                <tr>
                    <th>Email:</th>
                    <td><input type="text" class="textentry" name="email" value="<%=Helpers.htmlSafe(request.getParameter("email"))%>" /></td>
                </tr>
                <tr>
                    <th>Password:</th>
                    <td><input type="password" class="textentry" name="password1" value="<%=Helpers.htmlSafe(request.getParameter("password1"))%>" /></td>
                </tr>
                <tr>
                    <th>Verify password:</th>
                    <td><input type="password" class="textentry" name="password2" value="<%=Helpers.htmlSafe(request.getParameter("password2"))%>" /></td>
                </tr>

                <tr>
                    <%
                                boolean bExpiration = true;
                                if (request.getAttribute("bExpiration") != null
                                        && request.getAttribute("bExpiration").toString().equals("false")) {
                                    bExpiration = false;
                                }
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
                                            Integer daysUntilExpiration = (Integer) Integer.parseInt(getServletContext().getInitParameter("DAYS_USER_EXPIRATION").toString());
                                            if (request.getAttribute("daysUntilExpiration") != null) {
                                                daysUntilExpiration = (Integer) request.getAttribute("daysUntilExpiration");
                                            }
                                            out.print(daysUntilExpiration);
                                            for (Integer day : UserItem.dayMap.keySet()) {
                                                out.print("<option value=\"" + day + "\"");
                                                out.print(day.equals(daysUntilExpiration) ? " selected=\"selected\"" : "");
                                                out.print(">" + UserItem.dayMap.get(day) + "</option>\n");
                                            }

                                %>
                            </select>
                            <br />
                            <span class="note">Note: When the account expires, it will be deleted along with any and all uploaded files</span>
                        </div>
                    </td>
                </tr>
                <%
                            com.sectra.jfileshare.objects.UserItem oCurrentUser = (UserItem) session.getAttribute("user");
                            if (oCurrentUser.isAdmin()) {
                                Integer usertype = request.getParameter("usertype") == null ? oCurrentUser.TYPE_EXTERNAL : Integer.parseInt(request.getParameter("usertype"));
                %>
                <tr>
                    <th>User Type:</th>
                    <td>
                        <select name="usertype">
                            <option value="<%=oCurrentUser.TYPE_ADMIN%>"<%=usertype.equals(oCurrentUser.TYPE_ADMIN) ? " selected=\"selected\"" : ""%>>Administrator</option>
                            <option value="<%=oCurrentUser.TYPE_INTERNAL%>"<%=usertype.equals(oCurrentUser.TYPE_INTERNAL) ? " selected=\"selected\"" : ""%>>Sectra corporate</option>
                            <option value="<%=oCurrentUser.TYPE_EXTERNAL%>"<%=usertype.equals(oCurrentUser.TYPE_EXTERNAL) ? " selected=\"selected\"" : ""%>>External</option>
                        </select>
                    </td>
                </tr>
                <%
                            }
                %>

                <tr>
                    <td colspan="2">
                        <input type="hidden" name="action" value="adduser" />
                        <input type="submit" name="submit" value="Add user" />
                    </td>
                </tr>

            </table>
        </form>

    </body>
</html>

