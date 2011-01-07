<%@ page import="com.sectra.jfileshare.objects.UserItem" %>
<%@ page import="com.sectra.jfileshare.utils.Tab" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="/WEB-INF/sitemesh-page.tld" prefix="page"%>
<%@ taglib uri="/WEB-INF/sitemesh-decorator.tld" prefix="decorator"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
    "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=8" />

        <title>SECTRA file distribution facility <decorator:title /></title>
        <link rel="stylesheet" href="<%= request.getContextPath()%>/styles/main.css?v=1.3" type="text/css" />
        <%
                    UserItem user = (UserItem) session.getAttribute("user");
                    // Add javascript for session timeout
                    if (user != null) {
        %>
        <script type="text/javascript" xml:space="preserve" src="<%= request.getContextPath()%>/scripts/main.js?v=1.3"></script>
        <script type="text/javascript">
            var contextPath = "<%=request.getContextPath()%>";
            LogoutTimer.start();
        </script>
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
                <form name="logout" action="<%= request.getContextPath()%>/logout" method="post">
                    Currently logged in as <%=user.getUsername()%> ::
                    <a href="<%=request.getContextPath()%>/user/edit/<%=user.getUid()%>">Settings</a> ::
                    <a href="#" onclick="document.logout.submit()">Log out</a>
                </form>

            </div>

            <%
                        }
            %>

            <ul id="Menu">
                <%
                            for (Tab tab : tablist) {
                                if (tab.isEnabled()) {
                                    if (tab.isSelected()) {
                                        out.print("<li><div id=\"tabSelected\">" + tab.getTitle() + "</div></li>\n");
                                    } else {
                                        out.print("<li><a href=\"" + request.getContextPath() + tab.getLink() + "\">" + tab.getTitle() + "</a></li>\n");
                                    }
                                }
                            }
                %>
            </ul>

            <div id="content">
                <decorator:body />
            </div>

            <div id="footer">
                jfileshare version 1.4 beta
            </div>
        </div>

    </body>

</html>

