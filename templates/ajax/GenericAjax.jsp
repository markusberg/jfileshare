<%@ page contentType="text/xml;charset=UTF-8" language="java" %><%
	if ( request.getAttribute("ajaxresponse") != null ){
			out.println(request.getAttribute("ajaxresponse"));
		} else {
			out.println("OK");
	}
%>