<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
    <head>
        <title>Ajax dummy page</title>
    </head>
    <body>
        <p>This page should never be displayed. It will likely be loaded
            in a hidden iframe, and execute some javascript on the parent
            window.</p>
        <div id="msg">
            <%
                        if (request.getAttribute("msg") != null) {
                            out.print(request.getAttribute("msg"));
                        }
            %>
        </div>
        <script type="text/javascript">
            <%
                        if (request.getAttribute("javascript") != null) {
                            out.print(request.getAttribute("javascript"));
                        }
            %>
        </script>
    </body>
</html>