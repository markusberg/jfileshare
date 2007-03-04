<%@ page import="objects.UserItem" %>
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
      <script type="text/javascript">
          function disable_days(){

              var select = document.getElementById("daysselect");
              var expiresc = document.getElementById("expires");
              if ( expiresc.checked ){
                select.disabled=false;
              } else {
                  select.disabled = true;
              }

          }
      </script>

      <%
          UserItem user = ( UserItem ) session.getAttribute("user");


      %>
  </head>
  <body>
    <div id="registrationbox">
        <br /><div class="title">Remember, it's imperative that all fields are filled in correctly</div><br />
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
                    <td>Username</td><td><input type="text" name="username" value="<%=request.getParameter("username")!=null?request.getParameter("username"):""%>"/></td>
                </tr>
                <tr>
                    <td>Password</td><td><input type="password" name="password" value="<%=request.getParameter("password")!=null?request.getParameter("password"):""%>" /></td>
                </tr>
                <tr>
                    <td>Verify password</td><td><input type="password" name="password2" value="<%=request.getParameter("password2")!=null?request.getParameter("password2"):""%>" /></td>
                </tr>
                <tr>
                    <td>Email</td><td><input type="text" name="email" value="<%=request.getParameter("email")!=null?request.getParameter("email"):""%>" /></td>
                </tr>
                <tr>
                    <td>Expires</td><td><input id="expires" type="checkbox" name="expires" checked="checked" onchange="disable_days();"/><span class="note">When account expires, it will be removed together with all uploaded files</span></td>
                </tr>
                <tr>
                    <td>Days before expiry</td><td><select id="daysselect" name="daystoexpire">
                    <option value="15">15 days</option>
                    <option value="30">30 days</option>
                    <option value="60">2 months</option>
                    <option value="180">6 months</option>
                    <option value="365">1 year</option>
                    </select></td>
                </tr>
                <%
                    if ( user.getUserType() == objects.UserItem.TYPE_ADMIN ){
                %>
                <tr>
                    <td>User Type</td><td><select name="usertype">
                    <option value="<%=objects.UserItem.TYPE_ADMIN%>">TYPE_ADMIN</option>
                    <option value="<%=objects.UserItem.TYPE_SECTRA%>">TYPE_SECTRA</option>
                    <option value="<%=objects.UserItem.TYPE_EXTERNAL%>" selected="selected">TYPE_EXTERNAL</option>
                    </select></td>
                </tr>
                <%}%>
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