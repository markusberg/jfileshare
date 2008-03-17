<%@ page import="objects.UserItem" %>
<%@ page import="java.util.Map" %>
<%--
  Created by IntelliJ IDEA.
  User: zoran
  Date: Mar 16, 2008
  Time: 2:04:16 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>User administration</title>
      <link rel="stylesheet" href="/styles/useradmin.css" />
  </head>
  <body>
  <% if ( request.getAttribute("message") != null && ! request.getAttribute("message").equals("")){ %>
  <div>
        <%=request.getAttribute("message")%>      
  </div>
  <%
      }
      if ( (request.getParameter("page") != null && request.getParameter("page").equals("search")) || ( request.getAttribute("page") != null && request.getAttribute("page").equals("search"))){
          %>
  <form action="/mainadmin/users" method="POST">
      <table cellpadding="0" cellspacing="0" id="search">
      <tr><td>Username </td><td><input type="text" name="username" /></td></tr>
          <tr><td>&nbsp;<input type="hidden" name="action" value="dosearch" /></td><td><input type="submit" value="Search" /></td></tr>
      </table>
  </form>

  <%

      } else if ( request.getAttribute("user") != null && request.getAttribute("page") != null && request.getAttribute("page").equals("userdetail")){
              UserItem user = (UserItem) request.getAttribute("user");
          %>
  <table cellpadding="0" cellspacing="0" id="userdetail">
      <form action="/mainadmin/users" method="POST">
          <tr>
              <th>Userid: </th><th><%=user.getUid()%></th>
          </tr>
          <tr>
              <td>Username:</td><td><input type="text" name="username" value="<%=user.getUsername()%>" /></td>
          </tr>
          <tr>
              <td>Email: </td><td><input type="text" name="email" value="<%=user.getEmail()%>" /></td>
          </tr>
          <tr>
              <td>Expires: </td><td><input type="checkbox" name="expires"<%=user.expires()?" checked=\"checked\"":""%></td>
          </tr>
          <tr>
              <td>User-type:</td><td><select name="usertype">
                    <option value="<%=objects.UserItem.TYPE_ADMIN%>"<%=user.getUserType()==objects.UserItem.TYPE_ADMIN?" selected=\"selected\"":""%>>TYPE_ADMIN</option>
                    <option value="<%=objects.UserItem.TYPE_SECTRA%>"<%=user.getUserType()==objects.UserItem.TYPE_SECTRA?" selected=\"selected\"":""%>>TYPE_SECTRA</option>
                    <option value="<%=objects.UserItem.TYPE_EXTERNAL%>"<%=user.getUserType()==objects.UserItem.TYPE_EXTERNAL?" selected=\"selected\"":""%>>TYPE_EXTERNAL</option>
                    </select></td>
          </tr>
          <tr>
              <td colspan="2">&nbsp;</td>
          </tr>
          <tr>
              <td class="note" cols="2">(Leave password fiels empty to keep the old password)</td>
          </tr>
          <tr>
              <td>Password: </td><td><input type="password" name="password" /></td>
          </tr>
          <tr>
              <td>Password (again): </td><td><input type="password" name="password2" /></td>
          </tr>
          <tr>
              <td>&nbsp;<input type="hidden" name="uid" value="<%=user.getUid()%>" /><input type="hidden" name="action" value="doupdateuser" /></td><td><input type="submit" name="update" value="Update" /></td>
          </tr>
      </form>
      <form action="/mainadmin/users" method="POST">
          <tr>
              <td colspan="2"><hr /></td>
          </tr>
          <tr>
              <td colspan="2"><input type="submit" value="Delete this user" name="delete" /><input type="hidden" name="uid" value="<%=user.getUid()%>" /><input type="hidden" name="action" value="confirmdelete" /><i>(Note: All users files will be deleted too)</i></td>
          </tr>
      </form>
  </table>

  <%
      } else if ( request.getAttribute("user") != null && request.getAttribute("page") != null && request.getAttribute("page").equals("confirmdelete")){
              UserItem user = (UserItem) request.getAttribute("user");
          %>
  <table cellpadding="0" cellspacing="0" id="userdetail">
      <form action="/mainadmin/users" method="POST">
          <tr>
              <th>Userid: </th><th><%=user.getUid()%></th>
          </tr>
          <tr>
              <td>Username:</td><td><input disabled="disabled" type="text" name="username" value="<%=user.getUsername()%>" /></td>
          </tr>
          <tr>
              <td>Email: </td><td><input disabled="disabled" type="text" name="email" value="<%=user.getEmail()%>" /></td>
          </tr>
          <tr>
              <td>Expires: </td><td><input disabled="disabled" type="checkbox" name="expires"<%=user.expires()?" checked=\"checked\"":""%></td>
          </tr>
          <tr>
              <td>User-type:</td><td><select disabled="disabled" name="usertype">
                    <option value="<%=objects.UserItem.TYPE_ADMIN%>"<%=user.getUserType()==objects.UserItem.TYPE_ADMIN?" selected=\"selected\"":""%>>TYPE_ADMIN</option>
                    <option value="<%=objects.UserItem.TYPE_SECTRA%>"<%=user.getUserType()==objects.UserItem.TYPE_SECTRA?" selected=\"selected\"":""%>>TYPE_SECTRA</option>
                    <option value="<%=objects.UserItem.TYPE_EXTERNAL%>"<%=user.getUserType()==objects.UserItem.TYPE_EXTERNAL?" selected=\"selected\"":""%>>TYPE_EXTERNAL</option>
                    </select></td>
          </tr>
          <tr>
              <td colspan="2">&nbsp;</td>
          </tr>
          <tr>
              <td class="note" cols="2">(Leave password fiels empty to keep the old password)</td>
          </tr>
          <tr>
              <td>Password: </td><td><input disabled="disabled" type="password" name="password" /></td>
          </tr>
          <tr>
              <td>Password (again): </td><td><input disabled="disabled" type="password" name="password2" /></td>
          </tr>
          <tr>
              <td>&nbsp;<input type="hidden" name="uid" value="<%=user.getUid()%>" /><input type="hidden" name="action" value="doupdateuser" /></td><td>&nbsp;</td>
          </tr>
      </form>
      <form action="/mainadmin/users" method="POST">
      <tr>
          <td colspan="2" align="center">ARE YOU SURE THAT YOU WANT TO DELETE THIS USER?</td>
      </tr>
      <tr>
          <td align="right" style="padding-right: 3px;"><input type="submit" name="YES" value="YES" /><input type="hidden" name="uid" value="<%=user.getUid()%>" /><input type="hidden" name="action" value="dodelete" /></td>
          <td align="left" style="padding-left: 3px;"><a href="/mainadmin/users" class="NO">NO</a></td>
      </tr>
          </form>
  </table>

            <%
      }else {
  %>

  This is user administration
  <%
      if (request.getAttribute("users") !=null ){
  %>
  <table id="users" cellpadding="0" cellspacing="0">
      <%
          Map<Integer, UserItem> users = (Map<Integer,UserItem>) request.getAttribute("users");
          boolean isfirst = true;
          for ( Integer uid: users.keySet()){
              UserItem user = users.get(uid);
              %>
      <tr<%=isfirst?" class=\"first\"":""%>>
          <td><%=user.getUid()%></td>
          <td><%=user.getUsername()%></td>
          <td><%=user.getEmail()%></td>
          <td class="last"><%=user.getLastlogin()%></td>
          
      </tr>
      <%
              isfirst = false;
          }
      %>

  </table>

  <%
        }
      }
  %>

  </body>
</html>