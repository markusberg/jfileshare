<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
    <head>
        <title>Please supply password</title>

        <script type="text/javascript">
            function setfocus() {
                domPw = document.getElementById("pw");
                domPw.focus();
            }
            window.onload=setfocus;
        </script>
    </head>

    <body>
        <%@ include file="/WEB-INF/jspf/MessageBoxes.jspf"%>
        <form action="<%= request.getContextPath() + request.getAttribute("urlPattern")%>" method="post">
            <p>Please enter the file password to access this file:
                <input type="password" name="FilePassword" id="pw" />
                <input type="submit" name="sendpassword" value="Submit" />
            </p>
        </form>
    </body>
</html>
