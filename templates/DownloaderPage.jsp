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
          <td class="label">Filename: </td><td><%=file.getName()%></td>

      </tr>
      <tr>
          <td class="label">Size: </td><td><%=file.getSize().intValue()%> k</td>
      </tr>
      <tr>
          <td class="label">Type: </td><td><%=file.getType()%></td>
      </tr>
      <tr>
          <td class="label">MD5: </td><td><%=file.getMd5sum()%></td>
      </tr>
      <tr>
          <td class="label">Uploader: </td><td><%=file.getOwner().getUsername()%></td>
      </tr>
      <tr>
          <td class="label">Uploader email:</td><td><%=file.getOwner().getEmail()%></td>
      </tr>
      <tr>
          <td class="label">Date: </td><td><%=file.getDdate()%></td>
      </tr>
      <tr>
          <td id="download" colspan="2">
              <%
                  if ( file.getDownloads() == -1 || file.getDownloads() > 0 ){
              %>
              <a href="/download/get/<%=file.getMd5sum()%>/">Download file</a>
              <%
                  } else {
                      %>
              The file you are viewing has exceeded it's allowed downloads. Please contact the uploader to enable more downloads.
              <%
                  }
              %>

              </td>
      </tr>

  </table>
  <%
      }
  %>
  </body>
</html>