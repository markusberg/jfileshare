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

import com.sectra.jfileshare.objects.UserItem;

import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;

import javax.sql.DataSource;

public class UserAdminServlet extends HttpServlet {
    static final long serialVersionUID = 1L;

    private DataSource ds;
    private static final Logger logger =
            Logger.getLogger(UserAdminServlet.class.getName());

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
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        UserItem currentUser = (UserItem) session.getAttribute("user");
        ServletContext app = getServletContext();
        RequestDispatcher disp;

        if (currentUser.isAdmin()) {
            req.setAttribute("users", getAllUsers());
            disp = app.getRequestDispatcher("/templates/UserAdmin.jsp");
        } else {
            req.setAttribute("message_critical", "You are not authorized to administer users");
            disp = app.getRequestDispatcher("/templates/AccessDenied.jsp");
        }
        disp.forward(req, res);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    private ArrayList<UserItem> getAllUsers() {
        ArrayList<UserItem> allUsers = new ArrayList<UserItem>();
        Connection dbConn = null;

        try {
            dbConn = ds.getConnection();
            PreparedStatement st = dbConn.prepareStatement("SELECT * FROM UserItems LEFT OUTER JOIN viewUserFiles USING (uid) LEFT OUTER JOIN viewUserChildren USING (uid) ORDER BY sumFileSize DESC");
            st.execute();
            ResultSet rs = st.getResultSet();

            while (rs.next()) {
                UserItem user = new UserItem();

                user.setUid(rs.getInt("UserItems.uid"));

                user.setUsername(rs.getString("UserItems.username"));
                user.setPwHash(rs.getString("UserItems.pwHash"));
                user.setEmail(rs.getString("UserItems.email"));
                user.setUserType(rs.getInt("UserItems.usertype"));
                user.setDateCreation(rs.getTimestamp("UserItems.dateCreation"));
                user.setDateLastLogin(rs.getTimestamp("UserItems.dateLastLogin"));
                user.setDateExpiration(rs.getTimestamp("UserItems.dateExpiration"));
                user.setSumFileSize(rs.getLong("sumFileSize"));
                user.setSumFiles(rs.getInt("sumFiles"));
                user.setSumChildren(rs.getInt("sumChildren"));
                allUsers.add(user);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Exception: {0}", e.toString());
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }
        }

        logger.log(Level.INFO, "Found {0} users ", allUsers.size());
        return allUsers;
    }
}
