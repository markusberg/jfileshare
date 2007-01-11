<%
    String address = (String) request.getAttribute("address");
    response.sendRedirect(address);
%>