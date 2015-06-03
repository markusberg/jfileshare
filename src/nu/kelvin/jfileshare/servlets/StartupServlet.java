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
 * @version     1.16
 * @since       2011-09-21
 */
package nu.kelvin.jfileshare.servlets;

// import com.sectra.jfileshare.objects.Conf;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import javax.sql.DataSource;

public class StartupServlet extends HttpServlet {
    static final long serialVersionUID = 1L;

    // private Conf conf;
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
        dbUpdate2();
        dbUpdate3();
        dbUpdate4();
        dbUpdate5();
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
            createTables(dbConn);
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    private void createTables(Connection dbConn) {
        try {
            dbConn.setAutoCommit(false);
            // Create table for application configuration
            logger.info("Creating Conf table");
            alterDatabase(dbConn, "CREATE TABLE `Conf` ("
                    + "`key` varchar(64) NOT NULL, "
                    + "`value` varchar(128) NOT NULL, "
                    + "PRIMARY KEY (`key`)) "
                    + "ENGINE=InnoDB DEFAULT CHARSET=utf8");
            alterDatabase(dbConn, "insert into Conf values('daysLogRetention', '7')");
            alterDatabase(dbConn, "update Conf set `value`='4' where `key`='dbVersion'");
            PreparedStatement st = dbConn.prepareStatement("insert into Conf (`value`, `key`) values(?,?)");
            commitKeyValuePair(st, "daysFileExpiration", "14");
            commitKeyValuePair(st, "daysUserExpiration", "60");
            commitKeyValuePair(st, "fileSizeMax", "10485760");
            commitKeyValuePair(st, "pathStore", "/jfileshare/store");
            commitKeyValuePair(st, "pathTemp", "/jfileshare/temp");
            commitKeyValuePair(st, "smtpServer", "localhost");
            commitKeyValuePair(st, "smtpServerPort", "25");
            commitKeyValuePair(st, "smtpSender", "noreply@example.com");
            commitKeyValuePair(st, "brandingOrg", "jfileshare");
            commitKeyValuePair(st, "brandingDomain", "example.com");
            commitKeyValuePair(st, "brandingLogo", "");
            commitKeyValuePair(st, "debug", "false");

            // Create UserItems table
            logger.info("Creating UserItems table");
            alterDatabase(dbConn, "CREATE TABLE `UserItems` ( "
                    + "`uid` int(10) NOT NULL auto_increment, "
                    + "`usertype` int(2) NOT NULL default '1', "
                    + "`username` varchar(255) NOT NULL, "
                    + "`pwHash` varchar(255) default NULL, "
                    + "`datePasswordChange` timestamp NULL default NULL, "
                    + "`email` varchar(255) default NULL, "
                    + "`dateCreation` timestamp NOT NULL default CURRENT_TIMESTAMP, "
                    + "`dateLastLogin` timestamp NULL default NULL, "
                    + "`dateExpiration` timestamp NULL default NULL, "
                    + "`uidCreator` int(5) default NULL, "
                    + "PRIMARY KEY  (`uid`), "
                    + "UNIQUE KEY `username` (`username`), "
                    + "KEY `uidCreator` (`uidCreator`), "
                    + "CONSTRAINT `UserItems_ibfk_1` FOREIGN KEY (`uidCreator`) REFERENCES `UserItems` (`uid`) ON DELETE SET NULL "
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");

            // Create FileItems table
            logger.info("Creating FileItems table");
            alterDatabase(dbConn, "CREATE TABLE `FileItems` ("
                    + "`fid` int(10) NOT NULL auto_increment, "
                    + "`name` varchar(255) NOT NULL, "
                    + "`type` varchar(100) NOT NULL, "
                    + "`size` bigint(20) unsigned NOT NULL default '0', "
                    + "`md5sum` varchar(255) NOT NULL, "
                    + "`downloads` int(5) default NULL, "
                    + "`pwHash` varchar(255) default NULL, "
                    + "`dateCreation` timestamp NOT NULL default CURRENT_TIMESTAMP, "
                    + "`dateExpiration` timestamp NULL default NULL, "
                    + "`uid` int(5) NOT NULL, "
                    + "`enabled` tinyint(1) NOT NULL default '1', "
                    + "`allowTinyUrl` tinyint(1) NOT NULL default '0', "
                    + "PRIMARY KEY  (`fid`), "
                    + "KEY `uid` (`uid`), "
                    + "CONSTRAINT `FileItems_ibfk_1` FOREIGN KEY (`uid`) REFERENCES `UserItems` (`uid`) ON DELETE CASCADE "
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");

            // Create Logs table
            logger.info("Creating Logs table");
            alterDatabase(dbConn, "CREATE TABLE `Logs` ( "
                    + "`date` timestamp NOT NULL default CURRENT_TIMESTAMP, "
                    + "`ipAddress` varchar(39) NOT NULL default '0.0.0.0', "
                    + "`id` int(10) NOT NULL, "
                    + "`action` varchar(32) NOT NULL, "
                    + "`payload` varchar(256) NOT NULL "
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");

            // Create views
            logger.info("Creating views");
            alterDatabase(dbConn, "create VIEW viewUserFiles as select uid, count(fid) as sumFiles, sum(size) as sumFilesize from FileItems group by uid");
            alterDatabase(dbConn, "create VIEW viewUserChildren as select uidCreator as uid, count(uid) as sumChildren from UserItems where uidCreator is not null group by uidCreator");

            // Create table for password reset/recovery
            logger.info("Creating PasswordReset table");
            alterDatabase(dbConn, "CREATE TABLE `PasswordReset` ("
                    + "`dateRequest` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "`username` varchar(255) NOT NULL, "
                    + "`emailaddress` varchar(255) NOT NULL, "
                    + "`key` varchar(255) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8");

            // Commit transaction
            logger.info("Committing transaction");
            dbConn.commit();
        } catch (SQLException e) {
            try {
                dbConn.rollback();
                logger.warning("Unable to create tables. Rolling back.");
                logger.warning(e.toString());
            } catch (SQLException eRollback) {
                logger.warning("Unable to roll back.");
                logger.warning(eRollback.toString());
            }
        }
    }



    private int getDbVersion(Connection dbConn) throws SQLException, Exception {
        PreparedStatement st = dbConn.prepareStatement("select `value` from Conf where `key`='dbVersion'");
        st.execute();
        ResultSet rs = st.getResultSet();
        if (rs.first()) {
            return (Integer.parseInt(rs.getString("Conf.value")));
        }
        throw new Exception("Unable to find db version");
    }


    /***
     * Update log table to store uid and fid in separate columns
     * This will allow for more detailed logs in the future
     */
    private void dbUpdate5() {
        Connection dbConn = null;
        try {
            dbConn = datasource.getConnection();
            if (getDbVersion(dbConn) < 5) {

                alterDatabase(dbConn, "ALTER TABLE `Logs` CHANGE `id` `uid` int(10) NULL default NULL");
                alterDatabase(dbConn, "ALTER TABLE `Logs` ADD `fid` int(10) NULL DEFAULT NULL AFTER `uid`");
                alterDatabase(dbConn, "UPDATE `Logs` SET `fid`=`uid` where `action` in ('file edit', 'file delete', 'download', 'upload')");
                alterDatabase(dbConn, "UPDATE `Logs` SET `uid`=null where `action` in ('file edit', 'file delete', 'download', 'upload')");
                alterDatabase(dbConn, "INSERT INTO `Logs` (`date`, `uid`, `fid`, `action`, `payload`) SELECT `dateCreation`, `uid`, `fid`, 'upload', `name` from `FileItems`");
                alterDatabase(dbConn, "update Conf set `value`='5' where `key`='dbVersion'");
                logger.info("Database format updated to level 5");
            } else {
                logger.info("Database is already at level 5 or higher");
            }

        } catch (SQLException e) {
            logger.warning("SQLException: " + e.toString());
        } catch (Exception e) {
            logger.warning("Exception: " + e.toString());
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                    logger.warning("SQLException: " + e.toString());
                }
            }

        }
    }

    private void dbUpdate4() {
        Connection dbConn = null;
        try {
            dbConn = datasource.getConnection();
            PreparedStatement st = dbConn.prepareStatement("select `value` from Conf where `key`='dbVersion'");
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.first() && Integer.parseInt(rs.getString("Conf.value")) < 4) {
                logger.info("Need to update database to db level 4");
                logger.info("Fixing the uid column of UserItems table");
                alterDatabase(dbConn, "alter table UserItems CHANGE COLUMN `uid` "
                        + "`uid` int(10) NOT NULL AUTO_INCREMENT");

                logger.info("Transforming DownloadLogs to more generic Logs table");
                alterDatabase(dbConn, "alter table DownloadLogs DROP "
                        + "FOREIGN KEY `DownloadLogs_ibfk_1`");
                alterDatabase(dbConn, "alter table DownloadLogs DROP "
                        + "KEY `fid`");
                alterDatabase(dbConn, "alter table DownloadLogs RENAME TO Logs");
                alterDatabase(dbConn, "alter table Logs CHANGE COLUMN `time` `date` "
                        + "timestamp NOT NULL default CURRENT_TIMESTAMP");
                alterDatabase(dbConn, "alter table Logs CHANGE COLUMN `remote_addr` "
                        + "`ipAddress` varchar(39) NOT NULL DEFAULT '0.0.0.0' AFTER `date`");
                alterDatabase(dbConn, "alter table Logs CHANGE COLUMN `fid` "
                        + "`id` int(10) NOT NULL");
                alterDatabase(dbConn, "alter table Logs ADD COLUMN "
                        + "`action` varchar(32) NOT NULL");
                alterDatabase(dbConn, "alter table Logs ADD COLUMN "
                        + "`payload` varchar(256) NOT NULL");
                alterDatabase(dbConn, "update Logs set `action`='download'");
                alterDatabase(dbConn, "update Logs set Logs.payload=(select size from FileItems where fid=Logs.id) where `action`='download'");
                alterDatabase(dbConn, "insert into Conf values('daysLogRetention', '7')");
                alterDatabase(dbConn, "update Conf set `value`='4' where `key`='dbVersion'");
            } else { 
                logger.info("Database is already at level 4 or higher");
            }
        } catch (SQLException ignored) {
            logger.info(ignored.toString());
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }

        }
    }

    private void dbUpdate3() {
        Connection dbConn = null;
        try {
            dbConn = datasource.getConnection();
            PreparedStatement st = dbConn.prepareStatement("select `value` from Conf where `key`='dbVersion'");
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.first() && Integer.parseInt(rs.getString("Conf.value")) < 3) {
                logger.info("Need to update database to db level 3");
                // Add column to hold the date of the last password change
                alterDatabase(dbConn, "alter table UserItems ADD COLUMN datePasswordChange "
                        + "timestamp NULL default NULL AFTER pwHash");
                // Force users with crypt password to change
                alterDatabase(dbConn, "update UserItems set datePasswordChange=dateCreation "
                        + "where length(pwHash)<20");
                alterDatabase(dbConn, "update UserItems set datePasswordChange=dateLastLogin "
                        + "where length(pwHash)>20");
                alterDatabase(dbConn, "update UserItems set datePasswordChange=dateCreation "
                        + "where datePasswordChange is null");
                alterDatabase(dbConn, "insert into Conf values('daysPasswordExpiration', '0')");
                alterDatabase(dbConn, "update Conf set `value`='3' where `key`='dbVersion'");
            } else {
                logger.info("Database is already at level 3 or higher");
            }
        } catch (SQLException ignored) {
            logger.info(ignored.toString());
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }

        }
    }

    private void dbUpdate2() {
        Connection dbConn = null;
        try {
            dbConn = datasource.getConnection();
            PreparedStatement st = dbConn.prepareStatement("show tables like 'Conf'");
            st.execute();
            ResultSet rs = st.getResultSet();
            if (!rs.next()) {
                logger.info("Need to update database to db level 2");
                // fix the filesize column to be a bigint instead of a double
                alterDatabase(dbConn, "alter table FileItems CHANGE COLUMN size "
                        + "size bigint UNSIGNED NOT NULL DEFAULT 0;");
                // Recreate the viewUserFiles to use correct capitalization
                alterDatabase(dbConn, "drop view viewUserFiles");
                alterDatabase(dbConn, "create VIEW viewUserFiles as select uid, "
                        + "count(fid) as sumFiles, sum(size) as sumFileSize from "
                        + "FileItems group by uid");

                // create table for application configuration
                alterDatabase(dbConn, "CREATE TABLE `Conf` ("
                        + "`key` varchar(64) NOT NULL, "
                        + "`value` varchar(128) NOT NULL, "
                        + "PRIMARY KEY (`key`)) "
                        + "ENGINE=InnoDB DEFAULT CHARSET=utf8");
                // populate the new table with init-values
                confCreate();
            } else {
                logger.info("Database is already at level 2");
            }
        } catch (SQLException ignored) {
            logger.info(ignored.toString());
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }

        }
    }

    /***
     * Seed the newly created Config table with initial key/values
     * @return
     */
    private void confCreate() {
        Connection dbConn = null;
        PreparedStatement st = null;
        try {
            dbConn = datasource.getConnection();
            ServletContext app = getServletContext();
            st = dbConn.prepareStatement("insert into Conf (`value`, `key`) values(?,?)");
            commitKeyValuePair(st, "daysFileExpiration", app.getInitParameter("DAYS_FILE_RETENTION"));
            commitKeyValuePair(st, "daysUserExpiration", app.getInitParameter("DAYS_USER_EXPIRATION"));
            commitKeyValuePair(st, "fileSizeMax", app.getInitParameter("FILESIZE_MAX"));
            commitKeyValuePair(st, "pathStore", app.getInitParameter("PATH_STORE"));
            commitKeyValuePair(st, "pathTemp", app.getInitParameter("PATH_TEMP"));
            commitKeyValuePair(st, "smtpServer", app.getInitParameter("SMTP_SERVER"));
            commitKeyValuePair(st, "smtpServerPort", app.getInitParameter("SMTP_SERVER_PORT"));
            commitKeyValuePair(st, "smtpSender", app.getInitParameter("SMTP_SENDER"));
            commitKeyValuePair(st, "brandingOrg", "jfileshare");
            commitKeyValuePair(st, "brandingDomain", "example.com");
            commitKeyValuePair(st, "brandingLogo", "");
            commitKeyValuePair(st, "debug", "false");
            commitKeyValuePair(st, "dbVersion", "2");
        } catch (SQLException e) {
            logger.severe(e.toString());
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    private void commitKeyValuePair(PreparedStatement st, String key, String value)
            throws SQLException {
        st.setString(1, (value == null ? "" : value));
        st.setString(2, key);
        st.executeUpdate();
    }

    private void dbUpdate1() {
        Connection dbConn = null;
        try {
            dbConn = datasource.getConnection();
            PreparedStatement st = dbConn.prepareStatement("show tables like 'UserTypeItems'");
            st.execute();
            ResultSet rs = st.getResultSet();
            if (rs.next()) {
                logger.info("Need to update database to level 1");
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
                alterDatabase(dbConn, "update FileItems set dateExpiration=NULL where permanent=1");
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
        } catch (SQLException ignored) {
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }

        }
    }

    private void alterDatabase(Connection dbConn, String sqlStatement)
            throws SQLException {
        Statement st = dbConn.createStatement();
        int i = st.executeUpdate(sqlStatement);
        logger.log(Level.INFO, "db update returned: {0}", Integer.toString(i));
    }
}
