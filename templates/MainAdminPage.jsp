<%--
  Created by IntelliJ IDEA.
  User: zoran
  Date: Mar 16, 2008
  Time: 10:23:05 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>Main administration</title></head>
  <body>
  <table>
      <tr>
          <td><a href="/mainadmin/users">USERS</a></td>
          <td><a href="/mainadmin/files">FILES</a></td>
      </tr>
      <tr>

      </tr>
  </table>
    <%=request.getAttribute("subhandler")%>
  </body>
</html>