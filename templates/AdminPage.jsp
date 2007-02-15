<%@ page import="objects.UserItem" %>
<%@ page import="objects.FileItem" %>
<%@ page import="java.util.Map" %>
<%--
  Created by IntelliJ IDEA.
  User: zoran
  Date: Jan 11, 2007
  Time: 10:22:53 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>Administration</title>
      <link rel="stylesheet" href="/styles/admin.css" type="text/css" />
  </head>
  <body>

    Administration page

  <%

      if (request.getSession().getAttribute("user") != null) {
          UserItem user = (UserItem) request.getSession().getAttribute("user");
          %>
    <table cellpadding="0" cellspacing="0" id="userinfo">
        <tr>
            <td>Username: </td><td><%=user.getUsername()%></td>
        </tr>
        <tr>
            <td>Email: </td><td><%=user.getEmail()%></td>
        </tr>
        <tr>
            <td colspan="2"><a href="/upload/">Upload new file</a></td>
        </tr>
    </table>
    <%
            if (request.getAttribute("files") != null) {
                Map<Integer, FileItem> files = (Map<Integer,FileItem>) request.getAttribute("files");
                if ( files.size() > 0 ){
                                        %>
                        <table cellpadding="0" cellspacing="0" id="files">
                            <%
                                for ( Integer key: files.keySet() ){
                                    FileItem file = files.get(key);
                                    %>
                            <tr class="filename">
                                <td>Filename: </td><td colspan="2"><%=file.getName()%></td>
                            </tr>
                            <tr class="filedata">
                                <td>&nbsp;</td><td colspan="2" class="delete"><a href="?action=edit&fid=<%=file.getFid()%>">EDIT</a><a href="?action=delete&fid=<%=file.getFid()%>">DELETE</a></td>
                            </tr>
                            <tr class="filedata">
                                <td>&nbsp;</td><td>Size:</td><td><%=new Double(file.getSize()/1024).intValue()%> Kb</td>
                            </tr>
                            <tr class="filedata">
                                <td>&nbsp;</td><td>Md5sum</td><td><%=file.getMd5sum()%></td>
                            </tr>
                            <tr class="filedata">
                                <td>&nbsp;</td><td>Permanent</td><td><%=file.isPermanent()?"yes":"no"%></td>
                            </tr>
                            <tr class="filedata">
                                <td>&nbsp;</td><td>Downloads</td><td><%=file.getDownloads()==-1?"unlimited":file.getDownloads()%></td>
                            </tr>
                            <tr class="filedata">
                                <td>&nbsp;</td><td>Url</td><td><a href="http://<%=request.getServerName()%>/download/view/<%=file.getMd5sum()%>">url</a></td>
                            </tr>
                            <tr class="filedatal">
                                <td>&nbsp;</td><td>Notify email</td><td><form action="/admin/" method="post"><input type="text" name="email"><input type="hidden" name="fid" value="<%=file.getFid()%>"><input type="hidden" name="action" value="notify">&nbsp;<input class="notify" type="submit" name="submit" value="NOTIFY"></form> </td>
                            </tr>
                            <tr class="spacer">
                                <td colspan="3">&nbsp;</td>
                            </tr>

                            <%
                                }
                            %>
                        </table>
                        <%
                } else {
                    %>
        This user has no files
    <%
                }
            }

        } else {
                    %>This user seams to be nonexistent<%
                }
    %>

  </body>
</html>