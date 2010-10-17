package com.sectra.jfileshare.servlets;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.sql.DataSource;

public class StartupServlet extends HttpServlet {

    private DataSource datasource;
    private static final Logger logger =
            Logger.getLogger(StartupServlet.class.getName());

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);

        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            datasource = (DataSource) env.lookup("jdbc/jfileshare");
        } catch (NamingException excep) {
            throw new ServletException(excep);
        }
        TableInit();
        dbUpdate1();
    }

    private void TableInit() {
        Connection dbConn = null;
        try {
            dbConn = datasource.getConnection();
            PreparedStatement st = dbConn.prepareStatement("show tables like 'UserItems'");
            st.execute();
            logger.info("Tables exist");
        } catch (SQLException e) {
            logger.info("Database tables not found");
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    private void dbUpdate1() {
        Connection dbConn = null;
        try {
            dbConn = datasource.getConnection();
            PreparedStatement st = dbConn.prepareStatement("show tables like 'UserTypeItems'");
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.next()) {
                logger.info("Need to update database");
                alterDatabase(dbConn, "alter table FileItems add column allowTinyUrl tinyint(1) NOT NULL default 0");
                alterDatabase(dbConn, "alter table FileItems CHANGE COLUMN name name varchar(255) NOT NULL");
                alterDatabase(dbConn, "alter table FileItems CHANGE COLUMN type type varchar(100) NOT NULL");
                alterDatabase(dbConn, "alter table FileItems CHANGE COLUMN size size double NOT NULL DEFAULT 0");
                alterDatabase(dbConn, "alter table FileItems CHANGE COLUMN md5sum md5sum varchar(255) NOT NULL");
                alterDatabase(dbConn, "update FileItems SET downloads=null where downloads=-1");
                alterDatabase(dbConn, "alter table FileItems CHANGE COLUMN password pwHash varchar(255) NULL DEFAULT NULL");

                alterDatabase(dbConn, "alter table FileItems CHANGE COLUMN ddate dateCreation timestamp NOT NULL default CURRENT_TIMESTAMP");
                alterDatabase(dbConn, "alter table FileItems CHANGE COLUMN expiration dateExpiration timestamp NULL default NULL");

                // owner -> uid and all dependecies
                alterDatabase(dbConn, "alter table FileItems drop foreign key `FileItems_ibfk_1`");
                alterDatabase(dbConn, "alter table FileItems drop key `owner`");
                alterDatabase(dbConn, "alter table FileItems CHANGE COLUMN owner uid int(5) NOT NULL");
                alterDatabase(dbConn, "alter table FileItems add constraint foreign key (`uid`) references `UserItems`(`uid`) on delete cascade");

                alterDatabase(dbConn, "alter table FileItems CHANGE COLUMN enabled enabled tinyint(1) NOT NULL default 1");

                alterDatabase(dbConn, "alter table FileItems drop key `md5sum`");
                alterDatabase(dbConn, "update FileItems set dateExpiration=null where permanent != 1");
                alterDatabase(dbConn, "alter table FileItems drop column permanent");

                alterDatabase(dbConn, "alter table UserItems CHANGE COLUMN uid uid int(5) NOT NULL AUTO_INCREMENT");
                alterDatabase(dbConn, "alter table UserItems CHANGE COLUMN usertype usertype int(2) NOT NULL DEFAULT 1");
                alterDatabase(dbConn, "alter table UserItems CHANGE COLUMN username username varchar(255) NOT NULL");
                alterDatabase(dbConn, "alter table UserItems CHANGE COLUMN password pwHash varchar(255) NULL DEFAULT NULL");
                alterDatabase(dbConn, "alter table UserItems CHANGE COLUMN created dateCreation timestamp default CURRENT_TIMESTAMP");
                alterDatabase(dbConn, "alter table UserItems CHANGE COLUMN lastlogin dateLastLogin timestamp NULL default NULL");
                alterDatabase(dbConn, "alter table UserItems ADD COLUMN dateExpiration timestamp NULL default NULL AFTER daystoexpire");
                alterDatabase(dbConn, "update UserItems SET dateExpiration=dateCreation + INTERVAL daystoexpire DAY where expires=1");
                alterDatabase(dbConn, "alter table UserItems drop column expires");
                alterDatabase(dbConn, "alter table UserItems drop column daystoexpire");
                alterDatabase(dbConn, "alter table UserItems drop foreign key `UserItems_ibfk_1`");
                alterDatabase(dbConn, "alter table UserItems drop foreign key `UserItems_ibfk_2`");
                alterDatabase(dbConn, "alter table UserItems drop key `usertype`");
                alterDatabase(dbConn, "alter table UserItems drop key `creator`");

                alterDatabase(dbConn, "alter table UserItems CHANGE COLUMN creator uidCreator int(5) DEFAULT NULL");
                alterDatabase(dbConn, "alter table UserItems add constraint foreign key (`uidCreator`) references `UserItems`(`uid`) on delete set NULL");

                alterDatabase(dbConn, "update UserItems set dateLastLogin=null where pwHash=\"****NO*PASSORD*SET***\"");
                alterDatabase(dbConn, "update UserItems set pwHash=null where pwHash=\"****NO*PASSORD*SET***\"");

                alterDatabase(dbConn, "drop table UserTypeItems");
                alterDatabase(dbConn, "drop table test");

                // Fix character encodings 
                alterDatabase(dbConn, "alter database jfileshare character set=utf8");
                alterDatabase(dbConn, "alter table DownloadLogs CONVERT TO CHARACTER SET utf8");
                alterDatabase(dbConn, "alter table UserItems CONVERT TO CHARACTER SET utf8");
                alterDatabase(dbConn, "alter table FileItems CONVERT TO CHARACTER SET utf8");
                alterDatabase(dbConn, "update FileItems set name='Beräkning.zip' where fid=21");

                // create views
                alterDatabase(dbConn, "create VIEW viewUserFiles as select uid, count(fid) as sumFiles, sum(size) as sumFilesize from FileItems group by uid");
                alterDatabase(dbConn, "create VIEW viewUserChildren as select uidCreator as uid, count(uid) as sumChildren from UserItems where uidCreator is not null group by uidCreator");

                // create table for password reset/recovery
                alterDatabase(dbConn, "CREATE TABLE `PasswordReset` ("
                        + "`dateRequest` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "`username` varchar(255) NOT NULL, "
                        + "`emailaddress` varchar(255) NOT NULL, "
                        + "`key` varchar(255) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8");

            } else {
                logger.info("Database is already at level 1");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }

        }
    }

    private void alterDatabase(Connection dbConn, String sqlStatement) {
        try {
            Statement st = dbConn.createStatement();
            int i = st.executeUpdate(sqlStatement);

            logger.info("db update returned: " + Integer.toString(i));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
