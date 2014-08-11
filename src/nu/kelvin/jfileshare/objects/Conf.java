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
 * @version     1.8
 * @since       2011-09-21
 */
package nu.kelvin.jfileshare.objects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import javax.servlet.ServletRequest;

import javax.sql.DataSource;

/**
 * Simple config object for the webapp
 * @author markus
 */
public class Conf {

    private String brandingOrg;
    private String brandingDomain;
    private String brandingLogo;
    private String contextPath;
    private int daysFileExpiration;
    private int daysLogRetention;
    private int daysPasswordExpiration;
    private int daysUserExpiration;
    private int dbVersion;
    private boolean debug;
    private long fileSizeMax;
    private String pathStore;
    private String pathTemp;
    private String smtpServer = "localhost";
    private int smtpServerPort = 25;
    private InternetAddress smtpSender;
    private String urlPrefix;
    private static final String VERSION = "1.10";
    private static final Logger logger =
            Logger.getLogger(Conf.class.getName());

    public Conf(DataSource ds) {
        Connection dbConn = null;
        PreparedStatement st = null;
        try {
            dbConn = ds.getConnection();
            st = dbConn.prepareStatement("select value from Conf where `key`=?");
            setBrandingOrg(fetchValueFromDatabase(st, "brandingOrg"));
            setBrandingDomain(fetchValueFromDatabase(st, "brandingDomain"));
            setBrandingLogo(fetchValueFromDatabase(st, "brandingLogo"));
            setDbVersion(Integer.parseInt(fetchValueFromDatabase(st, "dbVersion")));
            setDebug(Boolean.parseBoolean(fetchValueFromDatabase(st, "debug")));
            setDaysFileExpiration(Integer.parseInt(fetchValueFromDatabase(st, "daysFileExpiration")));
            setDaysLogRetention(Integer.parseInt(fetchValueFromDatabase(st, "daysLogRetention")));
            setDaysPasswordExpiration(Integer.parseInt(fetchValueFromDatabase(st, "daysPasswordExpiration")));
            setDaysUserExpiration(Integer.parseInt(fetchValueFromDatabase(st, "daysUserExpiration")));
            setFileSizeMax(Long.parseLong(fetchValueFromDatabase(st, "fileSizeMax")));
            setPathStore(fetchValueFromDatabase(st, "pathStore"));
            setPathTemp(fetchValueFromDatabase(st, "pathTemp"));
            setSmtpServer(fetchValueFromDatabase(st, "smtpServer"));
            setSmtpServerPort(Integer.parseInt(fetchValueFromDatabase(st, "smtpServerPort")));
            setSmtpSender(fetchValueFromDatabase(st, "smtpSender"));
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

    private String fetchValueFromDatabase(PreparedStatement st, String key)
            throws SQLException {
        st.setString(1, key);
        ResultSet rs = st.executeQuery();
        if (rs.first()) {
            return rs.getString("Conf.value");
        } else {
            return null;
        }
    }

    public String getBrandingOrg() {
        return this.brandingOrg;
    }

    public void setBrandingOrg(String brandingOrg) {
        this.brandingOrg = brandingOrg;
    }

    public String getBrandingDomain() {
        return this.brandingDomain;
    }

    public void setBrandingDomain(String brandingDomain) {
        this.brandingDomain = brandingDomain;
    }

    public String getBrandingLogo() {
        return this.brandingLogo;
    }

    public void setBrandingLogo(String logo) {
        this.brandingLogo = (logo != null && logo.equals("")) ? "" : logo;
    }

    public int getDaysFileExpiration() {
        return daysFileExpiration;
    }

    public void setDaysFileExpiration(int daysFileExpiration) {
        this.daysFileExpiration = daysFileExpiration;
    }

    public int getDaysLogRetention() {
        return daysLogRetention;
    }

    public void setDaysLogRetention(int daysLogRetention) {
        this.daysLogRetention = daysLogRetention;
    }

    public int getDaysPasswordExpiration() {
        return this.daysPasswordExpiration;
    }

    public void setDaysPasswordExpiration(int daysPasswordExpiration) {
        this.daysPasswordExpiration = daysPasswordExpiration;
    }

    public int getDaysUserExpiration() {
        return this.daysUserExpiration;
    }

    public void setDaysUserExpiration(int daysUserExpiration) {
        this.daysUserExpiration = daysUserExpiration;
    }

    public int getDbVersion() {
        return this.dbVersion;
    }

    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }

    public boolean getDebug() {
        return this.debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public long getFileSizeMax() {
        return this.fileSizeMax;
    }

    public void setFileSizeMax(long fileSizeMax) {
        this.fileSizeMax = fileSizeMax;
    }

    public String getPathStore() {
        return this.pathStore;
    }

    public void setPathStore(String pathStore) {
        this.pathStore = pathStore;
    }

    public String getPathTemp() {
        return this.pathTemp;
    }

    public void setPathTemp(String pathTemp) {
        this.pathTemp = pathTemp;
    }

    public InternetAddress getSmtpSender() {
        return this.smtpSender;
    }

    public void setSmtpSender(String smtpSender) {
        try {
            InternetAddress temp = new InternetAddress(smtpSender);
            temp.validate();
            this.smtpSender = temp;
        } catch (AddressException e) {
            logger.info("Smtp sender address doesn't validate");
        }
    }

    public String getSmtpServer() {
        return this.smtpServer;
    }

    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    public int getSmtpServerPort() {
        return this.smtpServerPort;
    }

    public void setSmtpServerPort(int smtpServerPort) {
        this.smtpServerPort = smtpServerPort;
    }

    public boolean save(DataSource ds) {
        Connection dbConn = null;
        PreparedStatement st = null;
        try {
            dbConn = ds.getConnection();
            st = dbConn.prepareStatement("update Conf set `value`=? where `key`=?");
            commitKeyValuePair(st, "brandingOrg", brandingOrg);
            commitKeyValuePair(st, "brandingDomain", brandingDomain);
            commitKeyValuePair(st, "brandingLogo", brandingLogo);
            commitKeyValuePair(st, "daysFileExpiration", Integer.toString(daysFileExpiration));
            commitKeyValuePair(st, "daysLogRetention", Integer.toString(daysLogRetention));
            commitKeyValuePair(st, "daysPasswordExpiration", Integer.toString(daysPasswordExpiration));
            commitKeyValuePair(st, "daysUserExpiration", Integer.toString(daysUserExpiration));
            commitKeyValuePair(st, "debug", debug ? "true" : "false");
            commitKeyValuePair(st, "fileSizeMax", Long.toString(fileSizeMax));
            commitKeyValuePair(st, "pathStore", pathStore);
            commitKeyValuePair(st, "pathTemp", pathTemp);
            commitKeyValuePair(st, "smtpServer", smtpServer);
            commitKeyValuePair(st, "smtpServerPort", Integer.toString(smtpServerPort));
            commitKeyValuePair(st, "smtpSender", smtpSender.toString());

            st.close();
            return true;
        } catch (SQLException e) {
            logger.severe(e.toString());
            return false;
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
        if (value == null) {
            st.setNull(1, java.sql.Types.VARCHAR);
        } else {
            st.setString(1, value);
        }
        st.setString(2, key);
        st.executeUpdate();
    }

    /**
     * Figure out the absolute path to the server in order to construct proper links
     * @param req
     */
    public void setUrlPrefix(ServletRequest req) {
        String httpScheme = req.getScheme();
        String serverName = req.getServerName();
        Integer serverPort = (Integer) req.getServerPort();
        if ((serverPort == 80 && httpScheme.equals("http"))
                || (serverPort == 443 && httpScheme.equals("https"))) {
            serverPort = null;
        }

        urlPrefix = httpScheme + "://"
                + serverName
                + (serverPort != null ? ":" + serverPort.toString() : "");
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public String getVersion() {
        return VERSION;
    }

    public void setContextPath(String value) {
        contextPath = value;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getBaseUrl() {
        return urlPrefix + contextPath;
    }
}
