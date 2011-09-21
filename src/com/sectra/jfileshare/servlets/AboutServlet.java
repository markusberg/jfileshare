/**
 *  Copyright 2011 SECTRA Imtec AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * @author      Markus Berg <markus.berg @ sectra.se>
 * @version     1.6
 * @since       2011-09-21
 */
package com.sectra.jfileshare.servlets;

import com.sectra.jfileshare.objects.Conf;
import com.sectra.jfileshare.objects.FileItem;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;

import javax.sql.DataSource;
import java.sql.PreparedStatement;

public class AboutServlet extends HttpServlet {

    private static final Logger logger =
            Logger.getLogger(AboutServlet.class.getName());
    private DataSource ds;

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            ds = (DataSource) env.lookup("jdbc/jfileshare");
        } catch (NamingException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletContext app = getServletContext();
        RequestDispatcher disp;
        req.setAttribute("tab", "About");
        disp = app.getRequestDispatcher("/templates/About.jsp");
        HttpSession session = req.getSession();
        Conf conf = (Conf) app.getAttribute("conf");

        req.setAttribute("daysLogRetention", conf.getDaysLogRetention());

        if (session.getAttribute("user") != null) {
            Connection dbConn = null;
            PreparedStatement st = null;
            try {
                dbConn = ds.getConnection();
                st = dbConn.prepareStatement("select cast(count(1) as char) as logins, cast(count(distinct payload) as char) as uniqueLogins from Logs where action=\"login\" and date > (now() - INTERVAL ? DAY)");
                st.setInt(1, conf.getDaysLogRetention());
                ResultSet rs = st.executeQuery();
                if (rs.first()) {
                    req.setAttribute("logins", rs.getString("logins"));
                    req.setAttribute("uniqueLogins", rs.getString("uniqueLogins"));
                }

                st = dbConn.prepareStatement("select cast(count(1) as char) as downloads, sum(cast(payload as unsigned)) as bytesDownloads from Logs where action=\"download\" and date > (now() - INTERVAL ? DAY)");
                st.setInt(1, conf.getDaysLogRetention());
                rs = st.executeQuery();
                if (rs.first()) {
                    req.setAttribute("downloads", rs.getString("downloads"));
                    req.setAttribute("bytesDownloads", FileItem.humanReadable(rs.getLong("bytesDownloads")));
                }

                st = dbConn.prepareStatement("select cast(count(1) as char) as uploads, sum(cast(payload as unsigned)) as bytesUploads from Logs where action=\"upload\" and date > (now() - INTERVAL ? DAY)");
                st.setInt(1, conf.getDaysLogRetention());
                rs = st.executeQuery();
                if (rs.first()) {
                    req.setAttribute("uploads", rs.getString("uploads"));
                    req.setAttribute("bytesUploads", FileItem.humanReadable(rs.getLong("bytesUploads")));
                }
                st.close();
            } catch (SQLException e) {
                logger.severe(e.toString());
            } finally {
                if (dbConn != null) {
                    try {
                        dbConn.close();
                    } catch (SQLException ignore) {
                    }
                }
            }
        }
        disp.forward(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException,
            IOException {
        doGet(req, resp);
    }
}
