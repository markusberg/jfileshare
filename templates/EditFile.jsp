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
          <td>Downloads allowed</td><td><input type="text" name="downloads" value="<%=file.getDownloads()==-1?"unlimited":file.getDownloads()%>"></td>
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