<%@ page import="objects.FileItem" %>
<%--
  Created by IntelliJ IDEA.
  User: zoran
  Date: 2007-jan-06
  Time: 09:30:34
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>SECTRA Download</title></head>
  <body>

  <%
      if ( request.getAttribute("message") != null && ! request.getAttribute("message").equals("") ){
          %>
    <%=request.getAttribute("message")%>
  <%
      }

      if (request.getAttribute("file") != null) {
          FileItem file = (FileItem) request.getAttribute("file");
  %>
  <table cellpadding="0" cellspacing="0" id="filet">
      <tr>
          <td>Filename: </td><td><%=file.getName()%></td>

      </tr>
      <tr>
          <td>Size: </td><td><%=file.getSize().intValue()%> k</td>
      </tr>
      <tr>
          <td>Type: </td><td><%=file.getType()%></td>
      </tr>
      <tr>
          <td>Uploader: </td><td><%=file.getOwner().getUsername()%></td>
      </tr>
      <tr>
          <td>Uploader email:</td><td><%=file.getOwner().getEmail()%></td>
      </tr>
      <tr>
          <td>Date: </td><td><%=file.getDdate()%></td>
      </tr>
      <tr>
          <td colspan="2"><a href="/download/get/<%=file.getMd5sum()%>/">Download file</a></td>
      </tr>

  </table>
  <%
      }
  %>
  </body>
</html>