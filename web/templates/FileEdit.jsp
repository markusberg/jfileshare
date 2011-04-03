<%@page import="com.sectra.jfileshare.utils.Helpers"%>
<%@page import="com.sectra.jfileshare.objects.Conf"%>
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
                    if (request.getAttribute("file") != null) {
                        FileItem file = (FileItem) request.getAttribute("file");
        %>
        <form action="<%= request.getContextPath()%>/file/edit/<%= file.getFid()%>" method="post">
            <table id="singleentry">
                <tr>
                    <th>Filename: </th>
                    <td><a href="<%= file.getURL(request.getContextPath())%>"><%= Helpers.htmlSafe(file.getName())%></a></td>
                </tr>
                <tr>
                    <th>Content-Type: </th>
                    <td><%= Helpers.htmlSafe(file.getType())%></td>
                </tr>
                <tr>
                    <th>Md5sum: </th>
                    <td><%= file.getMd5sum()%></td>
                </tr>
                <tr>
                    <th>Enabled: </th>
                    <td>
                        <input type="checkbox" name="bEnabled" value="true" <%= file.isEnabled() ? " checked=\"checked\"" : ""%> />
                    </td>
                </tr>
                <%
                                        int daysFileExpiration = ((Conf) getServletContext().getAttribute("conf")).getDaysFileExpiration();
                                        if (daysFileExpiration != 0 || !file.isPermanent()) {
                %>
                <tr>
                    <th>Permanent: </th>
                    <td><input type="checkbox" name="bPermanent" value="true" <%= file.isPermanent() ? " checked=\"checked\"" : ""%> />
                        <%
                                                                    if (daysFileExpiration != 0) {
                        %>
                        <span class="note">Non-permanent files will be automatically removed after
                            <%= daysFileExpiration%>
                            days</span>
                            <%
                                                                        }
                            %>
                    </td>
                </tr>
                <%
                                        }
                %>
                <tr>
                    <th>Downloads allowed: </th>
                    <td><input type="text" class="intentry" name="iDownloads" value="<%= file.getDownloads() == null ? "" : file.getDownloads()%>" />
                        <span class="note">Integer to specify maximum number of allowed downloads or leave blank for no limit</span>
                    </td>
                </tr>
                <tr>
                    <%
                                            String checked = "";
                                            String displaystyle = "";
                                            if (file.getPwHash() != null && !file.getPwHash().isEmpty()) {
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
                            <input type="text" class="textentry" name="sPassword" />
                            <%
                                                    if (file.getPwHash() != null) {
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
                <tr><td>&nbsp;</td></tr>
                <tr>
                    <td></td>
                    <td>
                        <input type="hidden" name="action" value="savefile" />
                        <input type="submit" name="submit" value="Save" />
                    </td>
                </tr>
            </table>
        </form>
        <%
                    }
        %>
    </body>
</html>

