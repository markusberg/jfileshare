<%@ page import="nu.kelvin.jfileshare.objects.Conf"%>
<%@ page import="nu.kelvin.jfileshare.objects.UserItem" %>
<%@ page import="nu.kelvin.jfileshare.utils.Tab" %>
<%@ page import="nu.kelvin.jfileshare.utils.Helpers" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="/WEB-INF/sitemesh-page.tld" prefix="page"%>
<%@ taglib uri="/WEB-INF/sitemesh-decorator.tld" prefix="decorator"%>
<!DOCTYPE html>

<html>
    <head>
        <meta charset="utf-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=9" />

        <%
                    Conf conf = (Conf) getServletContext().getAttribute("conf");
        %>
        <title><%=conf.getBrandingOrg()%> file distribution facility <decorator:title /></title>
        <link rel="stylesheet" href="<%= request.getContextPath()%>/styles/main.css?v=<%= conf.getVersion()%>" type="text/css" />
        <%
                    UserItem user = (UserItem) session.getAttribute("user");
                    // Add javascript for session timeout
                    if (user != null) {
        %>
        <script type="text/javascript" xml:space="preserve" src="<%= request.getContextPath()%>/scripts/main.js?v=<%= conf.getVersion()%>"></script>
        <script type="text/javascript">
            var contextPath = "<%=request.getContextPath()%>";
            LogoutTimer.start();
        </script>
        <%
                    }
                    if (!conf.getBrandingLogo().equals("")) {
        %>
        <style type="text/css">
            #container {
                background: #ddd url(<%= conf.getBrandingLogo()%>) no-repeat top right;
            }
        </style>
        <%
                    }
        %>
        <decorator:head />
    </head>

    <%
                /* Define the tabs in the GUI */
                ArrayList<Tab> tablist = new ArrayList<Tab>();
                String tabExplicit;

                tabExplicit = request.getAttribute("tab") == null ? "" : request.getAttribute("tab").toString();
                if ("".equals(tabExplicit) && request.getServletPath().equals("/file/view")) {
                    tabExplicit = "File";
                }

                if (user == null) {
                    /**
                     * Not logged in. Add a login tab, and select it
                     * unless there's an explicitly defined tab
                     **/
                    tablist.add(new Tab("Login",
                            "/user/view",
                            !request.getServletPath().equals("/passwordreset") && tabExplicit.equals(""),
                            true));
                    tablist.add(new Tab("Password Recovery",
                            "/passwordreset",
                            request.getServletPath().equals("/passwordreset"),
                            true));
                } else {
                    tablist.add(new Tab("Home",
                            "/user/view",
                            (request.getServletPath().equals("/user/view") || request.getServletPath().equals("/file/delete")) && tabExplicit.equals(""),
                            true));

                    tablist.add(new Tab("Upload",
                            "/file/upload",
                            request.getServletPath().equals("/file/upload"),
                            true));

                    tablist.add(new Tab("Add User",
                            "/user/add",
                            request.getServletPath().equals("/user/add"),
                            !user.isExternal()));

                    tablist.add(new Tab("User Admin",
                            "/user/admin",
                            request.getServletPath().equals("/user/admin"),
                            user.isAdmin()));

                    tablist.add(new Tab("Admin",
                            "/admin",
                            request.getServletPath().equals("/admin"),
                            user.isAdmin()));
                }

                tablist.add(new Tab(tabExplicit,
                        "Explicitly defined tab",
                        true,
                        !tabExplicit.equals("")));

    %>

    <body>
        <div id="container">

            <%
                        if (user != null) {
            %>
            <div id="userinfo">
                <form id="logout" action="<%= request.getContextPath()%>/logout" method="post"></form>
                Currently logged in as <%=user.getUsername()%> ::
                <a href="<%=request.getContextPath()%>/user/edit/<%=user.getUid()%>">Settings</a> ::
                <a href="#" onclick="document.getElementById('logout').submit()">Log out</a>
            </div>

            <%
                        }
            %>

            <ul id="Menu">
                <%
                            for (Tab tab : tablist) {
                                if (tab.isEnabled()) {
                                    if (tab.isSelected()) {
                                        out.print("<li><div id=\"tabSelected\">" + Helpers.htmlSafe(tab.getTitle()) + "</div></li>\n");
                                    } else {
                                        out.print("<li><a href=\"" + request.getContextPath() + tab.getLink() + "\">" + Helpers.htmlSafe(tab.getTitle()) + "</a></li>\n");
                                    }
                                }
                            }
                %>
            </ul>

            <div id="content">
                <decorator:body />
            </div>

            <div id="footer">
                <a href="<%= conf.getContextPath()%>/about">jfileshare</a> version <%= conf.getVersion()%><br/>
            </div>

            <%
                        if (user != null && conf.getDebug()) {
            %>
            <div id="dbgConsole">
                <div style="font-weight: bold; margin-bottom: 1em;">Debug console</div>
                <div>
            <%
                out.print("CSRF token: " + user.getCSRFToken());
            %>
                </div>
                <div id="dbgLogoutTimer"></div>
            </div>
            <script type="text/javascript">
                var domTimer = document.getElementById("dbgLogoutTimer");
                function updateTimer() {
                domTimer.innerHTML = Math.round(LogoutTimer.getTimeUntilLogout()/1000) + " seconds until logout";
                }
                var tempTimer = setInterval("updateTimer()", 1000);
            </script>
            <%                    }
            %>
        </div>
    </body>

</html>

