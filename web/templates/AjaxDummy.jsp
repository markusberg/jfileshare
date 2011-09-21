<%--
   Copyright 2011 SECTRA Imtec AB

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--%>
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