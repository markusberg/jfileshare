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
package com.sectra.jfileshare.objects;

import com.sectra.jfileshare.utils.Sha512Crypt;

import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Level;

import java.util.logging.Logger;

import javax.sql.DataSource;

public class FileItem implements Serializable {

    private Integer fid;
    private String name;
    private String mimetype;
    private long size;
    private String md5sum;
    private Integer downloads;
    private String pwHash;
    private Timestamp dateCreation;
    private Timestamp dateExpiration;
    private Integer ownerUid;
    private String ownerUsername;
    private String ownerEmail;
    private boolean allowTinyUrl = false;
    private boolean enabled = true;
    private static final Logger logger =
            Logger.getLogger(FileItem.class.getName());

    public FileItem() {
    }

    public void fetch(DataSource ds, int fid)
            throws NoSuchFileException, SQLException {
        Connection dbConn = null;
        PreparedStatement st = null;
        try {
            dbConn = ds.getConnection();
            st = dbConn.prepareStatement("select FileItems.*, UserItems.* from FileItems, UserItems where FileItems.uid=UserItems.uid and FileItems.fid=?");
            st.setInt(1, fid);
            ResultSet rs = st.executeQuery();
            if (rs.first()) {
                setFid(rs.getInt("FileItems.fid"));
                setName(rs.getString("FileItems.name"));
                setType(rs.getString("FileItems.type"));
                setSize(rs.getLong("FileItems.size"));
                setMd5sum(rs.getString("FileItems.md5sum"));
                setEnabled(rs.getBoolean("FileItems.enabled"));
                setAllowTinyUrl(rs.getBoolean("FileItems.allowTinyUrl"));
                setDownloads(rs.getInt("FileItems.downloads"));
                if (rs.wasNull()) {
                    setDownloads(null);
                }

                setPwHash(rs.getString("FileItems.pwHash"));
                setDateCreation(rs.getTimestamp("FileItems.dateCreation"));
                setDateExpiration(rs.getTimestamp("dateExpiration"));
                if (rs.wasNull()) {
                    setDateExpiration(null);
                }

                setOwnerUid(rs.getInt("UserItems.uid"));
                setOwnerUsername(rs.getString("UserItems.username"));
                setOwnerEmail(rs.getString("UserItems.email"));
            } else {
                logger.info("File not found in database");
                throw new NoSuchFileException("File not found");
            }
        } catch (SQLException e) {
            logger.severe(e.toString());
            throw new SQLException("Unable to connect to the database. Please contact the system administrator.");
        } finally {
            if (st != null) {
                st.close();
            }
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * Number of days that files are kept by default
     * @param iDays
     */
    public void setDaysToKeep(int iDays) {
        long millis = (long) iDays * 1000 * 60 * 60 * 24;
        setDateExpiration(new Timestamp(System.currentTimeMillis() + millis));
    }

    public boolean isPermanent() {
        return dateExpiration == null ? true : false;
    }

    public Integer getFid() {
        return this.fid;
    }

    public void setFid(Integer fid) {
        this.fid = fid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        // Internet Explorer sends the complete file path of the uploaded
        // file iff the server is in "Trusted Sites". We strip this info out.
        if (name.contains("\\")) {
            name = name.substring(name.lastIndexOf("\\") + 1);
        }
        this.name = name;
    }

    public String getType() {
        return this.mimetype;
    }

    public void setType(String mimetype) {
        this.mimetype = mimetype;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMd5sum() {
        return this.md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public Integer getDownloads() {
        return this.downloads;
    }

    public void setDownloads(Integer downloads) {
        this.downloads = downloads;
    }

    public String getPwHash() {
        return this.pwHash;
    }

    public void setPwHash(String pwHash) {
        this.pwHash = pwHash;
    }

    public void setPwPlainText(String pwProvided) {
        this.pwHash = Sha512Crypt.Sha512_crypt(pwProvided, null, 0);
        // com.sectra.jfileshare.utils.Jcrypt.crypt(pwPlainText);
    }

    public Timestamp getDateCreation() {
        return this.dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Timestamp getDateExpiration() {
        return this.dateExpiration;
    }

    public void setDateExpiration(Timestamp dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public void setOwnerUid(Integer ownerUid) {
        this.ownerUid = ownerUid;
    }

    public Integer getOwnerUid() {
        return this.ownerUid;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getOwnerUsername() {
        return this.ownerUsername;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerEmail() {
        return this.ownerEmail;
    }

    public String getURL(String urlPrefix) {
        return urlPrefix + "/file/view/" + getFid() + "?md5=" + getMd5sum();
    }

    public boolean getAllowTinyUrl() {
        return this.allowTinyUrl;
    }

    public void setAllowTinyUrl(boolean allowTinyUrl) {
        this.allowTinyUrl = allowTinyUrl;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean create(DataSource ds, String ipAddress) {
        Connection dbConn = null;
        PreparedStatement st = null;
        try {
            dbConn = ds.getConnection();
            st = dbConn.prepareStatement("insert into FileItems values(NULL,?,?,?,?,?,?,now(),?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            st.setString(1, this.name);
            st.setString(2, this.mimetype);
            st.setDouble(3, this.size);
            st.setString(4, this.md5sum);
            if (this.downloads == null) {
                st.setNull(5, java.sql.Types.INTEGER);
            } else {
                st.setInt(5, this.downloads);
            }
            if (this.pwHash == null) {
                st.setNull(6, java.sql.Types.VARCHAR);
            } else {
                st.setString(6, this.pwHash);
            }
            if (this.dateExpiration == null) {
                st.setNull(7, java.sql.Types.TIMESTAMP);
            } else {
                st.setTimestamp(7, this.dateExpiration);
            }
            st.setInt(8, this.ownerUid);
            st.setBoolean(9, this.enabled);
            st.setBoolean(10, this.allowTinyUrl);
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            while (rs.next()) {
                this.fid = rs.getInt(1);
            }

            st = dbConn.prepareStatement("INSERT INTO Logs VALUES(now(),?,?,'upload',?)");
            st.setString(1, ipAddress);
            st.setInt(2, this.fid);
            st.setString(3, Long.toString(this.size));
            st.executeUpdate();

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

    public boolean update(DataSource ds, String ipAddress) {
        Connection dbConn = null;
        PreparedStatement st = null;
        try {
            dbConn = ds.getConnection();

            st = dbConn.prepareStatement("update FileItems set downloads=?,dateExpiration=?,enabled=?,pwHash=?,allowTinyUrl=? where fid=?");
            if (this.downloads == null) {
                st.setNull(1, java.sql.Types.INTEGER);
            } else {
                st.setInt(1, this.downloads);
            }
            if (dateExpiration == null) {
                st.setNull(2, java.sql.Types.TIMESTAMP);
            } else {
                st.setTimestamp(2, new Timestamp(this.dateExpiration.getTime()));
            }
            st.setBoolean(3, this.enabled);
            if (this.pwHash == null) {
                st.setNull(4, java.sql.Types.VARCHAR);
            } else {
                st.setString(4, this.pwHash);
            }
            st.setBoolean(5, this.allowTinyUrl);
            st.setInt(6, this.fid);
            st.executeUpdate();

            st = dbConn.prepareStatement("INSERT INTO Logs VALUES(now(),?,?,'file edit',?)");
            st.setString(1, ipAddress);
            st.setInt(2, this.fid);
            st.setString(3, this.name);
            st.executeUpdate();

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

    /**
     * Delete the file from the database and from disk
     * @param ds
     * @param pathFileStore
     * @return Were any errors encountered during the delete operation?
     */
    public boolean delete(DataSource ds, String pathFileStore, String ipAddress) {
        File realfile = new File(pathFileStore + "/" + Integer.toString(this.fid));
        realfile.delete();

        Connection dbConn = null;
        try {
            dbConn = ds.getConnection();
            PreparedStatement st = dbConn.prepareStatement("delete from FileItems where fid=?");
            st.setInt(1, this.fid);
            st.executeUpdate();

            st = dbConn.prepareStatement("INSERT INTO Logs VALUES(now(),?,?,'file delete',?)");
            st.setString(1, ipAddress);
            st.setInt(2, this.fid);
            st.setString(3, this.name);
            st.executeUpdate();

            st.close();
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
        return true;
    }

    public void logDownload(DataSource ds, String ipAddress) {
        logger.info("Logging download");
        Connection dbConn = null;
        try {
            dbConn = ds.getConnection();
            PreparedStatement st = dbConn.prepareStatement("UPDATE FileItems set downloads=downloads-1 where fid=? and downloads>0");
            st.setInt(1, this.fid);
            st.executeUpdate();

            st = dbConn.prepareStatement("INSERT INTO Logs VALUES(now(),?,?,'download',?)");
            st.setString(1, ipAddress);
            st.setInt(2, this.fid);
            st.setString(3, Long.toString(this.size));
            st.executeUpdate();

            st.close();
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

    /**
     * Verify if the provided password is the correct one
     * @param pwProvided plaintext password
     * @return Does the provided password check out?
     */
    public boolean authenticated(String pwProvided) {
        return Sha512Crypt.verifyPassword(pwProvided, this.pwHash);
    }

    public int getDaysUntilExpiration() {
        double millisLeft = dateExpiration.getTime() - System.currentTimeMillis();
        long daysLeft = Math.round(millisLeft / 1000 / 60 / 60 / 24);
        return (int) daysLeft;
    }

    /**
     * Human readable file size
     *
     * @param filesize For example 4020234934
     * @return Human readable file size, e.g. "4.02 MiB"
     */
    public static String humanReadable(long filesize) {
        DecimalFormat df = new DecimalFormat("0.#");
        int[] fs = getFileSize(filesize);
        return fs[1] + " " + unitsFileSize[fs[0]];
    }
    public static final String[] unitsFileSize = {
        "B",
        "KiB",
        "MiB",
        "GiB",
        "TiB",
        "PiB",
        "EiB",
        "ZiB",
        "YiB"
    };

    public static int[] getFileSize(Long size) {
        int unit = 0;
        while (!(size < 1024)) {
            unit++;
            size = size / 1024;
        }
        int[] result = {unit, size.intValue()};
        return result;
    }

    public ArrayList<FileLog> getLogs(DataSource ds) {
        ArrayList<FileLog> logs = new ArrayList<FileLog>();
        Connection dbConn = null;
        try {
            dbConn = ds.getConnection();
            PreparedStatement st = dbConn.prepareStatement("SELECT date, ipAddress FROM Logs WHERE `id`=? AND `action`='download' order by `date` DESC");
            st.setInt(1, this.fid);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                FileLog log = new FileLog(rs.getTimestamp(1), rs.getString(2));
                logs.add(log);
            }
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
        return logs;
    }
}
