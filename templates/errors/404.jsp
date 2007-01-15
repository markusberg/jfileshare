<%@ page session="true" isErrorPage="true" contentType="text/html;charset=UTF-8" language="java" %><%@ taglib uri="/WEB-INF/sitemesh-page.tld" prefix="page"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"><page:applyDecorator name="maindecorator"><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <%
                Object request_uri = request.getAttribute("javax.servlet.error.request_uri");

        %>
<title>Sida kunde inte hittas <%=request_uri.toString()%></title>


</head>


<body >


            Vi beklagar, men sida <code><%=request_uri.toString()%></code> kunde inte hittas på vår server. <br />
            Klicka <a href="/">HÄR</a> för att komma till huvudsidan.


<!--
Making 404 big enough for IE
 Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam vulputate nisl. Donec venenatis massa sit amet ligula.
 Donec elit dui, mollis imperdiet, blandit vel, tempor ultricies, metus. Ut pellentesque justo sed nunc. Proin nisl nisi, ornare
 sit amet, sagittis eu, fringilla viverra, libero. Curabitur egestas pretium tellus. Morbi lorem nisi, commodo nec, blandit vel,
 dapibus non, ipsum. Fusce nisl. In commodo, odio at hendrerit bibendum, risus tellus hendrerit lorem, in dictum justo
 enim nec pede. Praesent eget urna. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis
 egestas. Maecenas neque quam, venenatis sit amet, euismod eget, rhoncus sit amet, neque. Phasellus posuere ante accumsan
 diam. Nulla elit turpis, vestibulum eu, fermentum vitae, luctus id, dolor. Vestibulum urna odio, sodales id, vestibulum ac,
 congue sit amet, ligula. Duis in tortor ac neque ornare faucibus. Aliquam sit amet felis eget orci blandit tristique. Integer
 placerat arcu eu velit. Praesent tempor, orci eu vestibulum eleifend, metus tellus accumsan est, sed rhoncus leo orci sed magna.
-->

</body>

</html>
</page:applyDecorator>