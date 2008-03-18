<%@ page import="objects.UserItem" %>
<%@ page import="config.Config" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib uri="/WEB-INF/sitemesh-page.tld" prefix="page"%><%@ taglib uri="/WEB-INF/sitemesh-decorator.tld" prefix="decorator"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  <head><title>SECTRA file distribution facility <decorator:title /></title>
      <link rel="stylesheet" href="/styles/main.css" type="text/css" />
      <script type="text/javascript" xml:space="preserve" src="/scripts/main.js"></script>
      <decorator:head />
  </head>
  <body>
  <div id="maincontainer">

      <div id="container">
          <div id="header">
              <img src="/images/sectralogo.gif" alt="sectralogo" /> 
              </div>
          <div id="content">
              <%
                  if (session.getAttribute("user") != null) {
                      UserItem user = (UserItem) session.getAttribute("user");
                      
              %>
              <div id="menudiv">
                  <ul id="menuul">
                      <%
                          if (user.getUserType() <= Config.getRequiredLevel("/admin")){
                              %><li><a href="/admin">ADMINISTRATION</a></li><%
                          }
                      %>
                      <%
                          if (user.getUserType() <= Config.getRequiredLevel("/upload")){
                              %><li><a href="/upload">UPLOAD</a></li><%
                          }
                      %>
                      <%
                          if (user.getUserType() <= Config.getRequiredLevel("/register")){
                              %><li><a href="/register">REGISTER</a></li><%
                          }
                      %>
                      <%
                          if (user.getUserType() <= Config.getRequiredLevel("/mainadmin")){
                              %><li><a href="/mainadmin">MAIN ADMINISTRATION</a></li><%
                          }

                          if (user.getUserType() <= objects.UserItem.TYPE_ADMIN ){
                            boolean fullfiles = session.getAttribute("fullfiles")!=null;
                      %>
                      <li>
                          <script type="text/javascript" xml:space="preserve">
                              function submitthis(box){
                                  if ( box.checked ){
                                      ajaxRequest("sessionadm","subaction=set&name=fullfiles&value","on");
                                      alert("You are running with full file access");
                                  } else {
                                      ajaxRequest("sessionadm","subaction=unset&name","fullfiles");
                                      alert("You are running with not file access");
                                  }

                              }

                              function triggered(){

                              }
                          </script>
                          <form action="<%=request.getAttribute("urlPattern")%>" method="POST">
                            <input type="checkbox" name="fullfiles" onchange="submitthis(this);"<%=fullfiles?" checked=\"checked\" ":""%>/>(Full file administration)
                        </form>

                      </li><%} %>
                  </ul>
              </div>
              <br /><br />
              <%
                  }
              %>
            <decorator:body />
          </div>
          <%
              if ( session.getAttribute("user") != null ){
          %>
          <br /><span style="font-size: 11px; font-family: Helvetica,Arial,sans-serif"><a href="/logout">Logout</a></span>
          <%
              }
          %>
      </div>

  </div>


  </body>
</html>