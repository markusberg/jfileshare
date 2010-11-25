<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.sectra.jfileshare.objects.FileItem"%>
<html>
    <head>
        <title>Edit file</title>
        <%
                    /*
                    <script type="text/javascript">
                    function warnme() {
                    var domTinyUrl = document.getElementById("TinyUrl");
                    if ( domTinyUrl.checked ) {
                    alert("Allowing tinyurl access makes it much easier to guess the URL of your file. Use only with public documents or in combination with a password. You have been warned!");
                    }
                    }
                    </script>
                     */
        %>
    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>

        <%
                    if (request.getAttribute("oFile") != null) {
                        FileItem oFile = (FileItem) request.getAttribute("oFile");
        %>
        <form action="<%= request.getContextPath()%>/file/edit/<%= oFile.getFid()%>" method="post">
            <table id="singleentry">
                <tr>
                    <th>Filename: </th>
                    <td><a href="<%= oFile.getURL(request.getContextPath())%>"><%= oFile.getName()%></a></td>
                </tr>
                <tr>
                    <th>Content-Type: </th>
                    <td><%= oFile.getType()%></td>
                </tr>
                <tr>
                    <th>Md5sum: </th>
                    <td><%= oFile.getMd5sum()%></td>
                </tr>
                <tr>
                    <th>Enabled: </th>
                    <td>
                        <input type="checkbox" name="bEnabled" value="true" <%= oFile.isEnabled() ? " checked" : ""%> />
                    </td>
                </tr>
                <tr>
                    <th>Permanent: </th>
                    <td><input type="checkbox" name="bPermanent" value="true" <%= oFile.isPermanent() ? " checked" : ""%> />
                        <span class="note">Non-permanent files will be automatically removed after <%= Integer.parseInt(getServletContext().getInitParameter("DAYS_FILE_RETENTION").toString())%> days</span>
                    </td>
                </tr>
                <tr>
                    <th>Downloads allowed: </th>
                    <td><input style="width: 4em; text-align: right;" type="text" name="iDownloads" value="<%= oFile.getDownloads() == null ? "" : oFile.getDownloads()%>" />
                        <span class="note">Integer to specify maximum number of allowed downloads or leave blank for no limit</span>
                    </td>
                </tr>
                <tr>
                    <%
                                            String checked = "";
                                            String displaystyle = "";
                                            if (oFile.getPwHash() != null && oFile.getPwHash().length() > 0) {
                                                checked = " checked";
                                                displaystyle = " style=\"display: block;\"";
                                            } else {
                                                displaystyle = " style=\"display: none;\"";
                                            }
                    %>
                    <th>Require password: </th>

                    <td><input type="checkbox" name="bUsePw" value="true" onclick="ToggleVisibility('Password');"<%=checked%> /></td>
                </tr>
                <tr>
                    <td></td>
                    <td>
                        <div id="Password"<%=displaystyle%>>
                            <input type="text" name="sPassword" />
                            <%
                                                    if (oFile.getPwHash() == null) {
                            %>
                            <span class="note">Note: leave blank in order to keep existing password unchanged</span>
                            <%                                                    }
                            %>
                        </div>
                    </td>
                </tr>
                <%
                                        /*
                                        <tr>
                                        <th>Allow tiny-url: </th>
                                        <td><input id="TinyUrl" type="checkbox" name="tinyurl" onclick="warnme();" /></td>
                                        </tr>
                                         */
                %>
            </table>
            <p>
                <input type="hidden" name="action" value="savefile" />
                <input type="submit" name="submit" value="Save" />
            </p>
        </form>
        <%
                    }
        %>
    </body>
</html>

