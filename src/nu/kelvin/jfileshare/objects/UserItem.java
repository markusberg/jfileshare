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
package nu.kelvin.jfileshare.objects;

import nu.kelvin.jfileshare.utils.Sha512Crypt;

import java.io.Serializable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.Map;
import java.util.TreeMap;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;
import javax.sql.DataSource;

public class UserItem implements Serializable {
    static final long serialVersionUID = 1L;

    private String username;
    private String pwHash;
    private String email;
    private Integer uid;
    private int usertype = TYPE_EXTERNAL;
    private Timestamp dateCreation;
    private Timestamp dateLastLogin;
    private Timestamp dateExpiration;
    private Timestamp datePasswordChange;
    private Integer uidCreator;
    private long sumFileSize;
    private int sumFiles;
    private int sumChildren;
    public static final int TYPE_ADMIN = 1;
    public static final int TYPE_INTERNAL = 2;
    public static final int TYPE_EXTERNAL = 3;
    private static final Logger logger = Logger.getLogger(UserItem.class.getName());
    /**
     * Integer to string mapping of expiration dates
     */
    public static Map<Integer, String> DAY_MAP = new TreeMap<Integer, String>() {

        {
            put(new Integer(15), "15 days");
            put(new Integer(30), "30 days");
            put(new Integer(60), "2 months");
            put(new Integer(182), "6 months");
            put(new Integer(365), "1 year");
        }
    };

    public UserItem() {
    }

    public void fetch(DataSource ds, int uid)
            throws NoSuchUserException, SQLException {
        Connection dbConn = null;
        try {
            dbConn = ds.getConnection();
            PreparedStatement st = dbConn.prepareStatement("select * from UserItems left outer join viewUserFiles using (uid) left outer join viewUserChildren using (uid) where uid=?");
            st.setInt(1, uid);
            ResultSet rs = st.executeQuery();
            if (rs.first()) {
                setUid(rs.getInt("UserItems.uid"));
                setUsername(rs.getString("UserItems.username"));
                setPwHash(rs.getString("UserItems.pwHash"));
                setDatePasswordChange(rs.getTimestamp("UserItems.datePasswordChange"));
                setEmail(rs.getString("UserItems.email"));
                setUserType(rs.getInt("UserItems.usertype"));
                setDateCreation(rs.getTimestamp("UserItems.dateCreation"));
                setDateLastLogin(rs.getTimestamp("UserItems.dateLastLogin"));
                setDateExpiration(rs.getTimestamp("UserItems.dateExpiration"));
                setUidCreator(rs.getInt("UserItems.uidCreator"));
                setSumFiles(rs.getInt("sumFiles"));
                setSumFileSize(rs.getLong("sumFileSize"));
                setSumChildren(rs.getInt("sumChildren"));
            } else {
                throw new NoSuchUserException("User not found");
            }
            st.close();
        } catch (SQLException e) {
            logger.severe(e.toString());
            throw new SQLException("Unable to connect to database. Please contact the system administrator.");
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }

    public void fetch(DataSource ds, String username)
            throws NoSuchUserException, SQLException {
        Connection dbConn = null;
        try {
            dbConn = ds.getConnection();
            PreparedStatement st = dbConn.prepareStatement("select * from UserItems where username=?");
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.first()) {
                setUid(rs.getInt("UserItems.uid"));
                setUsername(rs.getString("UserItems.username"));
                setPwHash(rs.getString("UserItems.pwHash"));
                setDatePasswordChange(rs.getTimestamp("UserItems.datePasswordChange"));
                setEmail(rs.getString("UserItems.email"));
                setUserType(rs.getInt("UserItems.usertype"));
                setDateCreation(rs.getTimestamp("UserItems.dateCreation"));
                setDateLastLogin(rs.getTimestamp("UserItems.dateLastLogin"));
                setDateExpiration(rs.getTimestamp("UserItems.dateExpiration"));
                setUidCreator(rs.getInt("UserItems.uidCreator"));
            } else {
                throw new NoSuchUserException("User not found");
            }
            st.close();
        } catch (SQLException e) {
            logger.severe(e.toString());
            throw new SQLException("Unable to connect to database. Please contact the system administrator.");
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPwHash(String pwHash) {
        this.pwHash = pwHash;
    }

    /***
     * Set the hashed password based on provided plaintext password
     * 
     * This method is only ever called when the password is set or changed
     * therefore, datePasswordChange is also updated when this method is called
     * 
     * @param pwPlainText
     */
    public void setPwPlainText(String pwPlainText) {
        this.pwHash = Sha512Crypt.Sha512_crypt(pwPlainText, null, 0);
        this.setDatePasswordChange(new Timestamp(System.currentTimeMillis()));
    }

    public Timestamp getDatePasswordChange() {
        return this.datePasswordChange;
    }

    public void setDatePasswordChange(Timestamp datePasswordChange) {
        this.datePasswordChange = datePasswordChange;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getUid() {
        return this.uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getUserType() {
        return this.usertype;
    }

    public void setUserType(int usertype) {
        this.usertype = usertype;
    }

    /**
     * Convenience function for retrieving username and uid
     *
     * This is used for logging purposes. It keeps the logger-lines short
     *
     * @return String consisting of "username (uid)"
     */
    public String getUserInfo() {
        return this.username + " (" + this.uid + ")";
    }

    public Timestamp getDateCreation() {
        return this.dateCreation;
    }

    public void setDateCreation(Timestamp creation) {
        this.dateCreation = creation;
    }

    public Timestamp getDateLastLogin() {
        return this.dateLastLogin;
    }

    public void setDateLastLogin(Timestamp lastlogin) {
        this.dateLastLogin = lastlogin;
    }

    public Timestamp getDateExpiration() {
        return this.dateExpiration;
    }

    public void setDateExpiration(Timestamp expiration) {
        this.dateExpiration = expiration;
    }

    public Integer getUidCreator() {
        return this.uidCreator;
    }

    public void setUidCreator(Integer uidCreator) {
        this.uidCreator = uidCreator;
    }

    public long getSumFileSize() {
        return this.sumFileSize;
    }

    public int getSumFiles() {
        return this.sumFiles;
    }

    public int getSumChildren() {
        return this.sumChildren;
    }

    public void setSumFileSize(long sumFileSize) {
        this.sumFileSize = sumFileSize;
    }

    public void setSumFiles(int sumFiles) {
        this.sumFiles = sumFiles;
    }

    public void setSumChildren(int sumChildren) {
        this.sumChildren = sumChildren;
    }

    public boolean isAdmin() {
        if (this.usertype == TYPE_ADMIN) {
            return true;
        }
        return false;
    }

    public boolean isExternal() {
        if (this.usertype == TYPE_EXTERNAL) {
            return true;
        }
        return false;
    }

    public boolean create(DataSource ds, String ipAddress) {
        Connection dbConn = null;
        try {
            dbConn = ds.getConnection();
            PreparedStatement st;

            st = dbConn.prepareStatement("insert into UserItems values(NULL,?,?,?,now(),?,now(),NULL,?,?)", Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, this.usertype);
            st.setString(2, this.username);
            st.setString(3, this.pwHash);
            st.setString(4, this.email);
            st.setTimestamp(5, this.dateExpiration);
            if (this.uidCreator == null) {
                st.setNull(6, java.sql.Types.NULL);
            } else {
                st.setInt(6, this.uidCreator);
            }

            st.executeUpdate();

            ResultSet rs = st.getGeneratedKeys();
            while (rs.next()) {
                this.uid = rs.getInt(1);
            }

            st = dbConn.prepareStatement("INSERT INTO Logs VALUES(now(),?,?,'create user',?)");
            st.setString(1, ipAddress);
            st.setInt(2, this.uid);
            st.setString(3, this.username + " (" + this.uid.toString() + ")");
            st.executeUpdate();
            st.close();

            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to connect to database: {0}", e.toString());
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
        try {
            dbConn = ds.getConnection();
            PreparedStatement st;
            st = dbConn.prepareStatement("update UserItems set usertype=?,username=?,pwHash=?,datePasswordChange=?,email=?,dateExpiration=? where uid=?");
            st.setInt(1, this.usertype);
            st.setString(2, this.username);
            st.setString(3, this.pwHash);
            st.setTimestamp(4, this.datePasswordChange);
            st.setString(5, this.email);
            st.setTimestamp(6, this.dateExpiration);
            st.setInt(7, this.uid);
            st.executeUpdate();

            st = dbConn.prepareStatement("INSERT INTO Logs VALUES(now(),?,?,'user edit',?)");
            st.setString(1, ipAddress);
            st.setInt(2, this.uid);
            st.setString(3, this.username);
            st.executeUpdate();

            st.close();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to connect to database: {0}", e.toString());
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
     * Delete the user and all files belonging to user
     * @param ds DataSource to use
     * @param pathFileStore Physical on-disk path to the filestore
     */
    public void delete(DataSource ds, String pathFileStore, String ipAddress) {
        Connection dbConn = null;
        ArrayList<FileItem> files = this.getFiles(ds);
        if (!files.isEmpty()) {
            logger.log(Level.INFO, "Deleting {0} file(s) belonging to {1}", new Object[]{files.size(), this.getUserInfo()});
            for (FileItem file : files) {
                file.delete(ds, pathFileStore, ipAddress);
            }
        } else {
            logger.log(Level.INFO, "No files owned by user {0}", this.getUserInfo());
        }

        try {
            dbConn = ds.getConnection();
            // CASCADE SET NULL will automatically orphan any children
            PreparedStatement st = dbConn.prepareStatement("delete from UserItems where uid=?");
            st.setInt(1, this.uid);
            st.executeUpdate();

            st = dbConn.prepareStatement("INSERT INTO Logs VALUES(now(),?,?,'user delete',?)");
            st.setString(1, ipAddress);
            st.setInt(2, this.uid);
            st.setString(3, this.username);
            st.executeUpdate();

            st.close();

            logger.log(Level.INFO, "User {0} has now been deleted", this.getUserInfo());
        } catch (SQLException e) {
            logger.warning(e.toString());
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
     * Number of days until the user is deleted
     * @param days
     */
    public void setDaysUntilExpiration(int days) {
        long millis = (long) days * 1000 * 60 * 60 * 24;
        setDateExpiration(new Timestamp(System.currentTimeMillis() + millis));
    }

    public int getDaysUntilExpiration() {
        if (dateExpiration == null) {
            return 0;
        }
        double millisLeft = dateExpiration.getTime() - System.currentTimeMillis();
        double daysLeft = millisLeft / 1000 / 60 / 60 / 24;
        return (int) Math.round(daysLeft);
    }

    public boolean saveLastLogin(DataSource ds, String ipAddress) {
        Connection dbConn = null;
        try {
            dbConn = ds.getConnection();
            PreparedStatement ps = dbConn.prepareStatement("UPDATE UserItems set dateLastLogin=now() where uid=?");
            ps.setInt(1, this.uid);
            ps.executeUpdate();

            ps = dbConn.prepareStatement("INSERT INTO Logs VALUES(now(),?,?,'login',?)");
            ps.setString(1, ipAddress);
            ps.setInt(2, this.uid);
            ps.setString(3, this.username);
            ps.executeUpdate();

            ps.close();
            return true;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error saving last login: {0}", e.toString());
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
     * Verify if the provided password is correct
     * @param pwProvided plaintext password
     * @return Does the provided password check out?
     */
    public boolean authenticated(String pwProvided) {
        if (Sha512Crypt.verifyPassword(pwProvided, this.pwHash)) {
            return true;
        }
        logger.log(Level.INFO, "Incorrect user password for user {0}", this.getUserInfo());
        return false;
    }

    public ArrayList<UserItem> getChildren(DataSource ds) {
        ArrayList<UserItem> aChildren = new ArrayList<UserItem>();
        Connection dbConn = null;
        try {
            dbConn = ds.getConnection();
            PreparedStatement st = dbConn.prepareStatement("select * from UserItems LEFT OUTER JOIN viewUserFiles USING (uid) LEFT OUTER JOIN viewUserChildren USING (uid) where uidCreator=? ORDER BY sumFileSize DESC");
            st.setInt(1, this.getUid());
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
                user.setSumFileSize(rs.getLong("sumFilesize"));
                user.setSumFiles(rs.getInt("sumFiles"));
                user.setSumChildren(rs.getInt("sumChildren"));
                aChildren.add(user);
            }
            st.close();
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
        logger.log(Level.INFO, "Found {0} children to user {1}", new Object[]{aChildren.size(), this.getUserInfo()});
        return aChildren;
    }

    public ArrayList<FileItem> getFiles(DataSource ds) {
        ArrayList<FileItem> aFiles = new ArrayList<FileItem>();
        Connection dbConn = null;
        try {
            dbConn = ds.getConnection();
            PreparedStatement st = dbConn.prepareStatement("select FileItems.* from FileItems where FileItems.uid=? order by FileItems.name ASC;");
            st.setInt(1, this.getUid());
            st.execute();
            ResultSet rs = st.getResultSet();

            while (rs.next()) {
                FileItem file = new FileItem();

                file.setFid(rs.getInt("FileItems.fid"));
                file.setName(rs.getString("FileItems.name"));
                file.setType(rs.getString("FileItems.type"));
                file.setSize(rs.getLong("FileItems.size"));
                file.setMd5sum(rs.getString("FileItems.md5sum"));
                file.setEnabled(rs.getBoolean("FileItems.enabled"));
                file.setDownloads(rs.getInt("FileItems.downloads"));
                if (rs.wasNull()) {
                    file.setDownloads(null);
                }
                file.setPwHash(rs.getString("FileItems.pwHash"));
                if (rs.wasNull()) {
                    file.setPwHash(null);
                }
                file.setDateCreation(rs.getTimestamp("FileItems.dateCreation"));
                file.setDateExpiration(rs.getTimestamp("FileItems.dateExpiration"));
                if (rs.wasNull()) {
                    file.setDateExpiration(null);
                }
                file.setOwnerUid(rs.getInt("FileItems.uid"));
                aFiles.add(file);
            }
            st.close();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Exception: {0}", e.toString());
        } finally {
            if (dbConn != null) {
                try {
                    dbConn.close();
                } catch (SQLException e) {
                }
            }
        }

        logger.log(Level.INFO, "Found {0} files owned by user {1}", new Object[]{aFiles.size(), this.getUserInfo()});
        return aFiles;
    }

    /**
     * Validate that the password lives up to complexity requirements
     * @param pw1
     * @param pw2
     * @return ArrayList of errors encountered
     */
    public ArrayList<String> validatePassword(String pw1, String pw2) {
        ArrayList<String> errors = new ArrayList<String>();
        if (!pw1.equals(pw2)) {
            errors.add("Passwords don't match");
        } else {
            if (pw1.length() < 6) {
                errors.add("Password must contain at least six (6) characters");
            }
            if (!Pattern.matches(".*[a-zA-Z].*", pw1)) {
                errors.add("Password must contain at least one alphabetic character (a-z)");
            }
            if (!Pattern.matches(".*[^a-zA-Z].*", pw1)) {
                errors.add("Password must contain at least one non-alphabetic character (numeric, symbol, space)");
            }
        }
        if (errors.isEmpty()) {
            this.setPwPlainText(pw1);
        }
        return errors;
    }

    /**
     * Validate that the provided email address looks correct
     * @param emailAddress
     * @return ArrayList of errors encountered
     */
    public ArrayList<String> validateEmailAddress(String emailAddress) {
        ArrayList<String> errors = new ArrayList<String>();
        try {
            InternetAddress etemp = new InternetAddress(emailAddress);
            etemp.validate();
        } catch (AddressException e) {
            errors.add("That does not look like a valid email address");
        }
        this.setEmail(emailAddress);
        return errors;
    }

    /**
     * Does the user enjoy edit rights to the specified file
     * @param file
     * @return True if the user has access to the provided file
     */
    public boolean hasEditAccessTo(FileItem file) {
        if (this.isAdmin()) {
            logger.log(Level.INFO, "Administrator access to edit file {0}", file.getFid());
            return true;
        } else if (file.getOwnerUid().equals(this.getUid())) {
            logger.log(Level.INFO, "Owner access to edit file {0}", file.getFid());
            return true;
        }
        logger.log(Level.INFO, "User {0} does not have edit access to file {1}", new Object[]{this.getUserInfo(), file.getFid()});
        return false;
    }

    /**
     * Does the user enjoy edit rights to the specified user
     * @param user
     * @return True if the user has access to the provided user
     */
    public boolean hasEditAccessTo(UserItem user) {
        if (this.isAdmin()) {
            logger.log(Level.INFO, "Administrator access to edit user {0}", user.getUserInfo());
            return true;
        } else if (this.isParentTo(user)) {
            logger.log(Level.INFO, "Creator access to edit user {0}", user.getUserInfo());
            return true;
        } else if (user.getUid().equals(this.getUid())) {
            logger.log(Level.INFO, "Edit access to self granted to {0}", user.getUserInfo());
            return true;
        }
        logger.log(Level.INFO, "User {0} does not have edit access to user {1}", new Object[]{this.getUserInfo(), user.getUserInfo()});
        return false;
    }

    public boolean isParentTo(UserItem user) {
        return (user.getUidCreator().equals(this.getUid()));
    }

    public boolean passwordIsOlderThan(int days) {
        long expiration = (long) days * 1000 * 60 * 60 * 24;
        return (this.datePasswordChange.getTime() + expiration) < System.currentTimeMillis();
    }
}
