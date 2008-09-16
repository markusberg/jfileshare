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
      <script type="text/javascript">
          function chpw(){
              var row1 = document.getElementById("row1");
              var row2 = document.getElementById("row2");
              var row3 = document.getElementById("row3");
              row1.style.display = "block";
              row2.style.display = "block";
              row3.style.display = "block";

              
          }

          function hiderows(){
              var row1 = document.getElementById("row1");
              var row2 = document.getElementById("row2");
              var row3 = document.getElementById("row3");
              row1.style.display = "none";
              row2.style.display = "none";
              row3.style.display = "none";
          }

          function savepw(){
              var pw1 = document.getElementById("pw1");
              var pw2 = document.getElementById("pw2");
              if ( pw1.value == pw2.value ){
                  if ( pw1.value.length < 6 ){
                      alert("Your password has to be at least 6 characters")
                  } else {

                      var url="/ajax/?action=chpw&password=" + pw1.value + "&password2=" + pw2.value;

                     try {

                        xmlhttp = window.XMLHttpRequest?new XMLHttpRequest():
                             new ActiveXObject("Microsoft.XMLHTTP");

                      }
                      catch (e) {
                             alert(e);
                      }

                      xmlhttp.onreadystatechange = trigg;
                      xmlhttp.open("GET", url);
                      xmlhttp.send(null);
                  }
              } else {
                  alert("You must type your password twice");
              }


          }

          function trigg(){
              if ((xmlhttp.readyState == 4) && (xmlhttp.status == 200)) {

                response = xmlhttp.responseXML;
                status =  response.getElementsByTagName('status')[0].firstChild.data;
                message = response.getElementsByTagName('message')[0].firstChild.data;
                if ( status ){

                    hiderows();

                }
                  alert(message);
              }

          }
      </script>
  </head>
  <body>
  <%
      if ( request.getAttribute("message") != null ){
          %><div id="message">
      <%=request.getAttribute("message")%>
          </div><%
      }
  %>

    Administration page

  <%
      boolean childedit = false;
      if (request.getSession().getAttribute("user") != null) {
          UserItem user = (UserItem) request.getSession().getAttribute("user");
          if ( request.getAttribute("edited") != null ){
              user = (UserItem) request.getAttribute("edited");
              childedit = true;
              %>Editing child<%
          }
          %>
    <table cellpadding="0" cellspacing="0" id="userinfo">
        <tr>
            <td>Username: </td><td><%=user.getUsername()%></td>
        </tr>
        <tr>
            <td>Email: </td><td><%=user.getEmail()%></td>
        </tr>
        <tr>
            <td colspan="2"><a href="#" onclick="chpw(); return false;" />Change your password</td>
        </tr>
        <tr class="newpw" id="row1">
            <td>Your password</td>
            <td><input type="password" name="password" id="pw1" /></td>
        </tr>
        <tr class="newpw" id="row2">
            <td>Repeat your password</td>
            <td><input type="password" name="password2" id="pw2" /></td>
        </tr>
        <tr class="newpw" id="row3">
            <td colspan="2"><input type="button" name="Save" value="Save" id="savepw" onclick="savepw();" /></td>
        </tr>

        <%if ( childedit ){ %>
        <tr>
            <td colspan="2"><a href="/admin/?action=delch&uid=<%=user.getUid()%>">DELETE THIS USER</a></td>
        </tr>

        <%}%>
    </table>
    <%
        if ( user.getChildren() != null && user.getChildren().size() > 0 && user.getUserType() != objects.UserItem.TYPE_ADMIN ){
            %>
    <table cellpadding="0" cellspacing="0" id="childdata">
        <tr>
            <td>Uid</td><td>Username</td><td>Email</td><td>UserType</td><td>Files</td><td>Lastlogin</td>
        </tr>
        <%
        Map<Integer,UserItem> children = user.getChildren();
            for ( Integer key: children.keySet()){
                UserItem child = children.get(key);
                %>
            <tr>
                <td><%=child.getUid()%></td>
                <td><%=child.isExpired()?"(*) ":""%><a href="/admin/?action=editch&uid=<%=child.getUid()%>"><%=child.getUsername()%></a></td>
                <td><%=child.getEmail()%></td>
                <td><%=child.getUserType()==1?"ADMIN":child.getUserType()==2?"SECTRA":"EXTERNAL"%></td>
                <td><%=child.getFiles().size()%></td>

                <td><%=utils.Helpers.formatDate(child.getLastlogin())%></td>
                <td><%=child.expires()?child.getDaysUntillExpiration():"never"%></td>
            </tr>
        <%
            }

        %>
    </table>

    <%
        }
    %>
    <%
            if (request.getAttribute("files") != null) {
                Map<Integer, FileItem> files = (Map<Integer,FileItem>) request.getAttribute("files");
                if ( request.getAttribute("edited") != null ){
                    files = user.getFiles();
                }
                if ( files.size() > 0 ){
                                        %>
                        <table cellpadding="0" cellspacing="0" id="files">
                            <tr>
                                <th>Name</th><th>Size</th><th>Perm.</th><th>Downloads</th><th>PW</th><th>Url</th><th></th>
                            </tr>
                            <%
                                boolean even = true;
                                boolean first = true;
                                for ( Integer key: files.keySet() ){
                                    FileItem file = files.get(key);
                                    %>
                            <tr class="<%=first?"first":""%><%=even?"even":"odd"%>">
                                <td><%=file.getName()%></td>
                                <td><%=new Double(file.getSize()/1024).intValue()%> Kb</td>
                                
                                <td><%=file.isPermanent()?"YES":"NO"%><%=file.isExpired()?"*":""%></td>
                                <td><%=file.getDownloads()==-1?"unlimited":file.getDownloads()%></td>
                                <td><%=file.getPassword()!=null&&file.getPassword().length()>0?"yes":"no"%></td>
                                <td><a href="/download/view/<%=file.getMd5sum()%>_SECTRA_<%=file.getFid()%>">url</a></td>
                                <td class="lastcol"><a href="?action=edit&fid=<%=file.getFid()%>"><img src="/images/pencil.gif" alt="edit" /></a>&nbsp;&nbsp;<a href="?action=viewlog&fid=<%=file.getFid()%>"><img src="/images/stock_log.png" alt="delete" width="15" height="15"/></a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="?action=delete&fid=<%=file.getFid()%>" onclick="return confirm('Are you sure that you want to delete this file?');"><img src="/images/stock_delete.png" alt="delete" width="13" height="13"/></a></td>
                            </tr>
                           
                            <%
                                    even = !even;
                                    first = false;
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
