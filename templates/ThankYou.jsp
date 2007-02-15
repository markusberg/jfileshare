<%@ page import="config.Config" %>
<%--
  Created by IntelliJ IDEA.
  User: zoran
  Date: Feb 14, 2007
  Time: 2:12:34 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>Thank you</title>
      <link rel="stylesheet" href="/styles/uploader.css" type="text/css" />
  </head>
  <body>
  Thank you for the upload.
  <br />
  You can view your uploaded files in your <a href="/admin/">administration area</a><br />
  or <a href="/upload/">upload another file</a>.
  <br />
  <br />
  <div id="note">
  Note: Your file will be removed in <%=Config.getKeepForDays()%> days unless you change this parameter
  in the administration area
  </div>
  </body>
</html>