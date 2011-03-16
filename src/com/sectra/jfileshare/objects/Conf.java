package com.sectra.jfileshare.objects;

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

    private String brandingCompany;
    private String brandingDomain;
    private String brandingLogo;
    private String contextPath;
    private Integer daysFileRetention;
    private Integer daysUserExpiration;
    private Double dbVersion;
    private Long fileSizeMax;
    private String pathStore;
    private String pathTemp;
    private String smtpServer = "localhost";
    private Integer smtpServerPort = 25;
    private InternetAddress smtpSender;
    private String urlPrefix;
    private static final Logger logger =
            Logger.getLogger(Conf.class.getName());

    public Conf(DataSource ds) {
        Connection dbConn = null;
        PreparedStatement st = null;
        try {
            dbConn = ds.getConnection();
            st = dbConn.prepareStatement("select value from Conf where `key`=?");
            setBrandingCompany(fetchValueFromDatabase(st, "brandingCompany"));
            setBrandingDomain(fetchValueFromDatabase(st, "brandingDomain"));
            setBrandingLogo(fetchValueFromDatabase(st, "brandingLogo"));
            setDbVersion(Double.parseDouble(fetchValueFromDatabase(st, "dbVersion")));
            setDaysFileRetention(Integer.parseInt(fetchValueFromDatabase(st, "daysFileRetention")));
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

    public String getBrandingCompany() {
        return brandingCompany;
    }

    public void setBrandingCompany(String value) {
        brandingCompany = value;
    }

    public String getBrandingDomain() {
        return brandingDomain;
    }

    public void setBrandingDomain(String value) {
        brandingDomain = value;
    }

    public String getBrandingLogo() {
        return brandingLogo;
    }

    public void setBrandingLogo(String value) {
        brandingLogo = value;
    }

    public Integer getDaysFileRetention() {
        return daysFileRetention;
    }

    public void setDaysFileRetention(Integer value) {
        daysFileRetention = value;
    }

    public Integer getDaysUserExpiration() {
        return daysUserExpiration;
    }

    public void setDaysUserExpiration(Integer value) {
        daysUserExpiration = value;
    }

    public Double getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(Double value) {
        dbVersion = value;
    }

    public Long getFileSizeMax() {
        return fileSizeMax;
    }

    public void setFileSizeMax(Long value) {
        fileSizeMax = value;
    }

    public String getPathStore() {
        return pathStore;
    }

    public void setPathStore(String value) {
        pathStore = value;
    }

    public String getPathTemp() {
        return pathTemp;
    }

    public void setPathTemp(String value) {
        pathTemp = value;
    }

    public InternetAddress getSmtpSender() {
        return smtpSender;
    }

    public void setSmtpSender(String value) {
        try {
            smtpSender = new InternetAddress(value);
            smtpSender.validate();
        } catch (AddressException e) {
            logger.info("Smtp sender address doesn't validate");
        }
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(String value) {
        smtpServer = value;
    }

    public Integer getSmtpServerPort() {
        return smtpServerPort;
    }

    public void setSmtpServerPort(Integer value) {
        smtpServerPort = value;
    }

    public boolean save(DataSource ds) {
        Connection dbConn = null;
        PreparedStatement st = null;
        try {
            dbConn = ds.getConnection();
            st = dbConn.prepareStatement("update Conf set `value`=? where `key`=?");
            commitKeyValuePair(st, "brandingCompany", brandingCompany);
            commitKeyValuePair(st, "brandingDomain", brandingDomain);
            commitKeyValuePair(st, "brandingLogo", brandingLogo);
            commitKeyValuePair(st, "daysFileRetention", Integer.toString(daysFileRetention));
            commitKeyValuePair(st, "daysUserExpiration", Integer.toString(daysUserExpiration));
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
     * @return
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
