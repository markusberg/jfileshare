<%@ page import="objects.FileItem" %>
<%@ page import="config.Config" %>
<%--
  Created by IntelliJ IDEA.
  User: zoran
  Date: Jan 11, 2007
  Time: 1:37:24 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
      <%
          FileItem file = (FileItem) request.getAttribute("file");
      %><title>Edit: <%=file.getName()%></title>
      <link rel="stylesheet" href="/styles/editfile.css" type="text/css" />
      <script type="text/javascript">
          function disable(){

              var select = document.getElementById("pwfield");
              var expiresc = document.getElementById("pwswitch");
              var plabel = document.getElementById("pwlabel");
              if ( expiresc.checked ){
                select.disabled=false;
                  plabel.style.color="black";
              } else {
                  select.disabled = true;
                  plabel.style.color="gray";
              }

          }
      </script>
      </head>
  <body>

  <form action="/admin/" method="post">
  <table cellpadding="0" cellspacing="0" id="fileedit">
      <tr>
          <td>
              Filename:
          </td>
          <td>
              <%=file.getName()%>
          </td>
       </tr>
      <tr>
          <td>Md5sum:</td>
          <td><%=file.getMd5sum()%></td>
      </tr>
      <tr>
          <td>URL:</td>
          <td>https://<%=request.getServerName()%>/download/view/<%=file.getMd5sum()%>_SECTRA_<%=file.getFid()%></td>
      </tr>
      <tr>
          <td>Enabled</td><td><select name="enabled">
          <option value="yes"<%=file.isEnabled()?" selected=\"selected\"":""%>>yes</option>
          <option value="no"<%=file.isEnabled()?"":" selected=\"selected\""%>>no</option>
          </select></td>
      </tr>
      <tr>
          <td>Permanent</td><td><select name="permanent">
          <option value="yes"<%=file.isPermanent()?" selected=\"selected\"":""%>>yes</option>
          <option value="no"<%=file.isPermanent()?"":" selected=\"selected\""%>>no</option>
          </select><i style="font-size: 11px;">Non permanent files will be automatically removed after <%=Config.getKeepForDays()%> days</i></td>
      </tr>
      <tr>
          <td>Downloads allowed</td><td><input style="width: 90px;" type="text" name="downloads" value="<%=file.getDownloads()==-1?"unlimited":file.getDownloads()%>"><i style="font-size:11px;">Integer to specify max. allowed downloads or "unlimited" for no limit</i></td>
      </tr>
      <tr><%
          String checked="";
          String disabled=" disabled=\"disabled\"";
          String password="";
          if ( file.getPassword() != null && file.getPassword().length() > 0 ){
              checked = " checked=\"checked\"";
              disabled = "";
              password = " value=\"<ENCRYPTED>\"";

          }
      %>
          <td>Require password</td><td><input id="pwswitch" type="checkbox" name="pwsw" onclick="disable();"<%=checked%> /></td>
      </tr>
      <tr>
          <td id="pwlabel">Password</td><td><input id="pwfield" type="text" name="password"<%=disabled%><%=password%> /></td>
      </tr>
      <tr>

          <td id="tinyurllabel">Allow tiny-url</td><td>
          <script type="text/javascript">
              function warnme(){
                  var checkid = document.getElementById("tinyurl");
                  if ( checkid.checked ){
                      alert("ALLOWING TINY-URL ACCESS MAKES IT EASIER TO GUESS THE URL OF YOUR FILE. USE ONLY WITH PUBLIC DOCUMENTS OR TOGETHER WITH PASSWORD. YOU HAVE BEEN WARNED!!!");
                  }
              }
          </script>

          <input id="tinyurl" type="checkbox" name="tinyurl" onclick="warnme();" /></td>
      </tr>
      <tr>
          <td colspan="2">
              <input type="hidden" name="fid" value="<%=file.getFid()%>" />
              <input type="hidden" name="action" value="savefile" />
              <input type="submit" name="submit" value="Save" /></td>
      </tr>
  </table>
  </form>

  </body>
</html>