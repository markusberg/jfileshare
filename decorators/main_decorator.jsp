<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib uri="/WEB-INF/sitemesh-page.tld" prefix="page"%><%@ taglib uri="/WEB-INF/sitemesh-decorator.tld" prefix="decorator"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  <head><title>This is decorator <decorator:title /></title>
      <link rel="stylesheet" href="/styles/main.css" type="text/css" />
      <decorator:head />
  </head>
  <body>
  <div id="maincontainer">

      <div id="container">
          <decorator:body />
      </div>

  </div>


  </body>
</html>