<%--
  Created by IntelliJ IDEA.
  User: zoran
  Date: 2007-jan-06
  Time: 09:26:08
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>Uploader</title></head>
  <body>Please upload file<br />
  <form action="/upload" method="post" enctype="multipart/form-data">
      <input type="file" name="file" />
      <br />
      <input type="submit" name="submit" value="Send" />

  </form> </body>
</html>