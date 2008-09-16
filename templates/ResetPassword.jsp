<%--
  Created by IntelliJ IDEA.
  User: zoran
  Date: Sep 16, 2008
  Time: 9:59:59 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>Reset your password</title></head>
  <body>
    <form action="/admin" method="POST">
        <table cellpadding="0" cellspacing="0">
            <tr>
                <td>Your username</td>
                <td><input type="text" name="username" /></td>
            </tr>
            <tr>
                <td colspan="2"><input type="submit" name="action" value="Reset" /></td>
            </tr>
        </table>
    </form>
    
  </body>
</html>