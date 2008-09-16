<%--
  Created by IntelliJ IDEA.
  User: zoran
  Date: 2007-jan-06
  Time: 14:39:48
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>Simple jsp page</title></head>
  <body>
    
  <%
      if ( request.getAttribute("message") != null ){
          %>
  <div class="message"><%=request.getAttribute("message")%></div>
  <%
      }
  %>
  <form action="<%=request.getAttribute("urlPattern")%>/" method="post">
  <table cellpadding="0" cellspacing="0">
      <tr>
          <td>Username</td><td><input type="text" name="username" /></td>
      </tr>
      <tr>
          <td>Password</td><td><input type="password" name="password" /></td>
      </tr>
      <tr>
          <td><input type="hidden" name="action" value="login" /><input type="submit" name="submit" value="Login" /></td>
          <td style="padding-left: 10px"><a href="?action=resetpw">Reset your password</a></td>
      </tr>
  </table>
      </form>
  </body>
</html>