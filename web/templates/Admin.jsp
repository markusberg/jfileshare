<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.sectra.jfileshare.objects.Conf"%>
<%@page import="com.sectra.jfileshare.objects.FileItem"%>
<%@page import="com.sectra.jfileshare.objects.UserItem"%>
<html>
    <head>
        <title>Admin</title>
    </head>

    <body>
        <%@include file="/WEB-INF/jspf/MessageBoxes.jspf"%>

        <%
                    Conf conf = (Conf) getServletContext().getAttribute("conf");
        %>
        <form action="<%= request.getContextPath()%>/admin" method="post">
            <table>
                <tr>
                    <td><h3>Branding</h3></td>
                </tr>
                <tr>
                    <th>Organization name: </th>
                    <td>
                        <input type="text" class="textentry" name="brandingOrg" value="<%= request.getAttribute("validatedBrandingOrg")%>" />
                    </td>
                    <td>
                        <span class="note">
                            Note: The organization name will be displayed in the user interface, and in
                            emails sent from the jfileshare app
                        </span>
                    </td>
                </tr>
                <tr>
                    <th>Domain name: </th>
                    <td>
                        <input type="text" class="textentry" name="brandingDomain" value="<%= request.getAttribute("validatedBrandingDomain")%>" />
                    </td>
                    <td>
                        <span class="note">
                            Note: This domain name will be used during auto-creation of accounts. If user <strong>"xyz"</strong>
                            requests a password reset, and that user doesn't exist in the database, the reset instructions are
                            sent to xyz@&lt;domain-name&gt;.
                        </span>
                    </td>
                </tr>
                <tr>
                    <th>Logo url: </th>
                    <td>
                        <input type="text" class="textentry" name="brandingLogo" value="<%= request.getAttribute("validatedBrandingLogo") == null ? "" : request.getAttribute("validatedBrandingLogo")%>" />
                    </td>
                    <td>
                        <span class="note">
                            Note: Leave blank to use the default logo
                        </span>
                    </td>
                </tr>
                <tr>
                    <td colspan="2"><h3>Storage settings</h3></td>
                </tr>
                <tr>
                    <th>Path to filestore: </th>
                    <td><input type="text" class="textentry" name="pathStore" value="<%= request.getAttribute("validatedPathStore")%>" /></td>
                </tr>
                <tr>
                    <th>Path to tempstore: </th>
                    <td><input type="text" class="textentry" name="pathTemp" value="<%= request.getAttribute("validatedPathTemp")%>" /></td>
                </tr>
                <tr>
                    <th>File retention: </th>
                    <td><input type="text" class="intentry" name="daysFileRetention" value="<%= request.getAttribute("validatedDaysFileRetention")%>" /> days</td>
                </tr>
                <tr>
                    <th>Maximum allowed file size: </th>
                    <td>
                        <input type="text" class="intentry" name="fileSizeMax" value="<%= request.getAttribute("validatedFileSizeMax")%>" />
                        <%
                        Integer fileSizeUnit = (Integer) request.getAttribute("validatedFileSizeUnit");
                        %>
                        <select name="fileSizeUnit">
                            <option value="1" <%= fileSizeUnit == 1 ? "selected=\"selected\"" : ""%>>KiB</option>
                            <option value="2" <%= fileSizeUnit == 2 ? "selected=\"selected\"" : ""%>>MiB</option>
                            <option value="3" <%= fileSizeUnit == 3 ? "selected=\"selected\"" : ""%>>GiB</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td colspan="2"><h3>Mail server settings</h3></td>
                </tr>
                <tr>
                    <th>Smtp server and port: </th>
                    <td><input class="textentry" type="text" name="smtpServer" value="<%= request.getAttribute("validatedSmtpServer")%>" /></td>
                    <td><input style="width: 4em;" type="text" name="smtpServerPort" value="<%= request.getAttribute("validatedSmtpServerPort")%>" /></td>
                </tr>
                <tr>
                    <th>Smtp sender: </th>
                    <td><input type="text" class="textentry" name="smtpSender" value="<%= request.getAttribute("validatedSmtpSender")%>" /></td>
                </tr>
                <tr>
                    <td colspan="2"><h3>Other settings</h3></td>
                </tr>
                <tr>
                    <th>Default user expiration: </th>
                    <td>
                        <select name="daysUserExpiration">
                            <%
                                        Integer daysUserExpiration = (Integer) request.getAttribute("validatedDaysUserExpiration");
                                        for (Integer day : UserItem.DAY_MAP.keySet()) {
                                            out.print("<option value=\"" + day + "\"");
                                            out.print(day.equals(daysUserExpiration) ? " selected=\"selected\"" : "");
                                            out.print(">" + UserItem.DAY_MAP.get(day) + "</option>\n");
                                        }
                            %>
                        </select>
                    </td>
                </tr>
                <tr>
                    <th>Database version:</th>
                    <td><%= request.getAttribute("dbVersion")%></td>
                </tr>
                <tr><td>&nbsp;</td></tr>
                <tr>
                    <td></td>
                    <td><input type="submit" name="submit" value="Save & apply" /></td>
                </tr>
            </table>
        </form>
    </body>
</html>

