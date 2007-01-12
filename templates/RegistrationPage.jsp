<%--
  Created by IntelliJ IDEA.
  User: zoran
  Date: 2007-jan-06
  Time: 10:03:40
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>Registration</title>
    <link rel="stylesheet" href="/styles/registration.css" type="text/css" />
  </head>
  <body>
    <div id="registrationbox">
        <span class="title">Remember, it's imperative that all fields are filled in correctly</span><br />
        <%
            if ( request.getAttribute("message") != null && ! request.getAttribute("message").equals("")){
                %>
        <div id="errorbox">
            <%=request.getAttribute("message")%>
        </div>
        <%
            }
        %><%
        if ( request.getAttribute("success") == null ){
        %>
        <form action="/register" method="post">
            <table id="regtable" cellpadding="0" cellspacing="0">
                <tr>
                    <td>Username</td><td><input type="text" name="username" /></td>
                </tr>
                <tr>
                    <td>Password</td><td><input type="password" name="password" /></td>
                </tr>
                <tr>
                    <td>Verify password</td><td><input type="password" name="password2" /></td>
                </tr>
                <tr>
                    <td>Email</td><td><input type="text" name="email" /></td>
                </tr>
                <tr>
                    <td colspan="2"><input type="hidden" name="action" value="register" /><input type="submit" name="submit" value="Register" /></td>
                </tr>
            </table>
        </form>
        <%
            }
        %>

    </div>


  </body>
</html>