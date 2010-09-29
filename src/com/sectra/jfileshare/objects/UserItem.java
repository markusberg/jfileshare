package com.sectra.jfileshare.objects;

import com.sectra.jfileshare.utils.Jcrypt;
import com.sectra.jfileshare.utils.Sha512Crypt;

import java.io.File;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.Map;
import java.util.TreeMap;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;
import javax.sql.DataSource;

public class UserItem {

    private String username;
    private String pwHash;
    private String email;
    private int uid = -1;
    private int usertype = TYPE_EXTERNAL;
    private Timestamp dateCreation;
    private Timestamp dateLastLogin;
    private Timestamp dateExpiration = null;
    private Integer creatoruid = null;
    private double sumFilesize;
    private int sumFiles = 0;
    private int sumChildren = 0;
    public static final int TYPE_ADMIN = 1;
    public static final int TYPE_INTERNAL = 2;
    public static final int TYPE_EXTERNAL = 3;
    private Logger logger = Logger.getLogger(UserItem.class.getName());
    /**
     * Integer to string mapping of expiration dates
     */
    public static Map<Integer, String> dayMap = new TreeMap<Integer, String>() {

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

    public UserItem(Connection conn, int uid) {
        try {
            PreparedStatement st = conn.prepareStatement("select * from UserItems left outer join viewUserFiles using (uid) left outer join viewUserChildren using (uid) where uid=?");
            st.setInt(1, uid);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                setUid(rs.getInt("UserItems.uid"));
                setUsername(rs.getString("UserItems.username"));
                setPwHash(rs.getString("UserItems.password"));
                setEmail(rs.getString("UserItems.email"));
                setUserType(rs.getInt("UserItems.usertype"));
                setDateCreation(rs.getTimestamp("UserItems.dateCreation"));
                setDateLastLogin(rs.getTimestamp("UserItems.dateLastLogin"));
                setDateExpiration(rs.getTimestamp("UserItems.dateExpiration"));
                setSumFiles(rs.getInt("sumFiles"));
                setSumFilesize(rs.getDouble("sumFilesize"));
                setSumChildren(rs.getInt("sumChildren"));
            }
        } catch (SQLException e) {
            logger.severe(e.toString());
        }
    }

    public UserItem(Connection dbConn, String username) {
        try {
            PreparedStatement st = dbConn.prepareStatement("select * from UserItems where username=?");
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                setUid(rs.getInt("UserItems.uid"));
                setUsername(rs.getString("UserItems.username"));
                setPwHash(rs.getString("UserItems.password"));
                setEmail(rs.getString("UserItems.email"));
                setUserType(rs.getInt("UserItems.usertype"));
                setDateCreation(rs.getTimestamp("UserItems.dateCreation"));
                setDateLastLogin(rs.getTimestamp("UserItems.dateLastLogin"));
                setDateExpiration(rs.getTimestamp("UserItems.dateExpiration"));
            }
        } catch (SQLException e) {
            logger.severe(e.toString());
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPwHash(String password) {
        this.pwHash = password;
    }

    public void setPwPlainText(String pwPlainText) {
        this.pwHash = Sha512Crypt.Sha512_crypt(pwPlainText, null, 0);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUid() {
        return uid;
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

    public int getCreatorUid() {
        return this.creatoruid == null ? -1 : this.creatoruid;
    }

    public void setCreatorUid(int creatoruid) {
        this.creatoruid = creatoruid;
    }

    public double getSumFilesize() {
        return this.sumFilesize;
    }

    public int getSumFiles() {
        return this.sumFiles;
    }

    public int getSumChildren() {
        return this.sumChildren;
    }

    public void setSumFilesize(double sumFilesize) {
        this.sumFilesize = sumFilesize;
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

    public boolean save(DataSource datasource) {
        Connection dbConn = null;
        try {
            dbConn = datasource.getConnection();
            this.save(dbConn);
        } catch (SQLException e) {
            logger.severe("Unable to connect to database: " + e.toString());
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

    public boolean save(Connection conn) {
        PreparedStatement st = null;
        try {
            if (this.uid == -1) {
                st = conn.prepareStatement("insert into UserItems values(NULL,?,?,?,?,now(),NULL,?,?)", Statement.RETURN_GENERATED_KEYS);
                st.setInt(1, this.usertype);
                st.setString(2, this.username);
                st.setString(3, this.pwHash);
                st.setString(4, this.email);
                st.setTimestamp(5, this.dateExpiration);
                if (this.creatoruid == null) {
                    st.setNull(6, java.sql.Types.NULL);
                } else {
                    st.setInt(6, this.creatoruid);
                }
            } else {
                st = conn.prepareStatement("update UserItems set usertype=?,username=?,password=?,email=?,dateExpiration=? where uid=?");
                st.setInt(1, this.usertype);
                st.setString(2, this.username);
                st.setString(3, this.pwHash);
                st.setString(4, this.email);
                st.setTimestamp(5, this.dateExpiration);
                st.setInt(6, this.uid);
            }

            st.executeUpdate();
            if (this.uid == -1) {
                ResultSet rs = st.getGeneratedKeys();
                while (rs.next()) {
                    this.uid = rs.getInt(1);
                }
            }
            st.close();
            return true;
        } catch (SQLException e) {
            logger.severe(e.toString());
            return false;
        }
    }

    /**
     * Delete the user and all files belonging to user
     * @param dbConn Database to delete user from
     * @param pathFileStore Physical on-disk path to the filestore
     */
    public void delete(Connection dbConn, String pathFileStore) {
        if (this.uid != -1) {
            ArrayList<FileItem> aFiles = this.getFiles(dbConn);
            if (aFiles.size() > 0) {
                // Delete all files belonging to this user
                logger.info("Deleting " + aFiles.size() + " file(s) belonging to " + this.getUserInfo());
                // Iterator<FileItem> it = aFiles.iterator();
                for (FileItem oFile : aFiles) {
                    // Delete the files on disk
                    // CASCADE DELETE will take care of the database
                    File fileondisk = new File(pathFileStore + "/" + oFile.getFid());
                    fileondisk.delete();
                }
            } else {
                logger.info("No files owned by user " + this.getUserInfo());
            }

            try {
                // CASCADE SET NULL will automatically orphan any children
                PreparedStatement st = dbConn.prepareStatement("delete from UserItems where uid=?");
                st.setInt(1, this.uid);
                st.executeUpdate();
                st.close();
                logger.info("User " + this.getUserInfo() + " has now been deleted");
            } catch (SQLException e) {
                logger.warning(e.toString());
            }
        }
    }

    /**
     * Number of days until the user is deleted
     * @param iDays
     */
    public void setDaysUntilExpiration(int iDays) {
        long millis = (long) iDays * 1000 * 60 * 60 * 24;
        setDateExpiration(new Timestamp(System.currentTimeMillis() + millis));
    }

    public int getDaysUntilExpiration() {
        if (dateExpiration == null) {
            return -1;
        }

        double millisLeft = dateExpiration.getTime() - System.currentTimeMillis();
        long daysLeft = Math.round(millisLeft / 1000 / 60 / 60 / 24);
        return (int) daysLeft;
    }

    public boolean saveLastLogin(Connection dbConn) {
        try {
            PreparedStatement psLastLogin = dbConn.prepareStatement("UPDATE UserItems set dateLastLogin=now() where uid=" + this.uid);
            psLastLogin.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.warning("Error saving last login: " + e.toString());
            return false;
        }
    }

    /**
     * Verify if the provided password is correct
     * @param pwProvided plaintext password
     * @return Does the provided password check out?
     */
    public boolean authenticated(String pwProvided) {
        if (this.pwHash.length() < 20) {
            String pwCrypt = Jcrypt.crypt(pwHash, pwProvided);
            if (pwCrypt.equals(pwHash)) {
                return true;
            }
        } else {
            if (Sha512Crypt.verifyPassword(pwProvided, this.pwHash)) {
                return true;
            }
        }
        logger.info("Incorrect user password for user " + this.getUserInfo());
        return false;
    }

    public ArrayList<UserItem> getChildren(Connection conn) {
        ArrayList<UserItem> aChildren = new ArrayList<UserItem>();
        try {
            PreparedStatement st = conn.prepareStatement("select * from UserItems LEFT OUTER JOIN viewUserFiles USING (uid) LEFT OUTER JOIN viewUserChildren USING (uid) where creator=? ORDER BY sumFilesize DESC");
            st.setInt(1, this.getUid());
            st.execute();
            ResultSet rs = st.getResultSet();

            while (rs.next()) {
                UserItem user = new UserItem();

                user.setUid(rs.getInt("UserItems.uid"));

                user.setUsername(rs.getString("UserItems.username"));
                user.setPwHash(rs.getString("UserItems.password"));
                user.setEmail(rs.getString("UserItems.email"));
                user.setUserType(rs.getInt("UserItems.usertype"));
                user.setDateCreation(rs.getTimestamp("UserItems.dateCreation"));
                user.setDateLastLogin(rs.getTimestamp("UserItems.dateLastLogin"));
                user.setDateExpiration(rs.getTimestamp("UserItems.dateExpiration"));
                user.setSumFilesize(rs.getDouble("sumFilesize"));
                user.setSumFiles(rs.getInt("sumFiles"));
                user.setSumChildren(rs.getInt("sumChildren"));
                aChildren.add(user);
            }
        } catch (SQLException e) {
            logger.severe("Exception: " + e.toString());
        }
        logger.info("Found " + aChildren.size() + " children to user " + this.getUserInfo());
        return aChildren;
    }

    public ArrayList<FileItem> getFiles(Connection conn) {
        ArrayList<FileItem> aFiles = new ArrayList<FileItem>();
        try {
            PreparedStatement st = conn.prepareStatement("select FileItems.* from FileItems where FileItems.owner=? order by FileItems.size DESC;");
            st.setInt(1, this.getUid());
            st.execute();
            ResultSet rs = st.getResultSet();

            while (rs.next()) {
                FileItem file = new FileItem();

                file.setFid(rs.getInt("FileItems.fid"));
                file.setName(rs.getString("FileItems.name"));
                file.setType(rs.getString("FileItems.type"));
                file.setSize(rs.getDouble("FileItems.size"));
                file.setMd5sum(rs.getString("FileItems.md5sum"));
                file.setEnabled(rs.getBoolean("FileItems.enabled"));
                file.setDownloads(rs.getInt("FileItems.downloads"));
                if (rs.wasNull()) {
                    file.setDownloads(-1);
                }
                file.setPwHash(rs.getString("FileItems.password"));
                if (rs.wasNull()) {
                    file.setPwHash(null);
                }
                file.setDateCreation(rs.getTimestamp("FileItems.dateCreation"));
                file.setDateExpiration(rs.getTimestamp("FileItems.dateExpiration"));
                file.setOwnerUid(rs.getInt("FileItems.owner"));
                aFiles.add(file);
            }
        } catch (SQLException e) {
            logger.warning("Exception: " + e.toString());
        }
        logger.info("Found " + aFiles.size() + " files owned by user " + this.getUserInfo());
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
        if (errors.size() == 0) {
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
            this.setEmail(emailAddress);
        } catch (AddressException e) {
            errors.add("That does not look like a valid email address");
        }
        return errors;
    }

    /**
     * Validate that the provided username is available
     * @param username
     * @return ArrayList of errors encountered
     */
    public ArrayList<String> validateUserName(String username) {
        ArrayList<String> errors = new ArrayList<String>();
        if (username.equals("")) {
            errors.add("Username is empty");
        } else {
            if (this.getUid() != -1) {
                errors.add("Username is already taken");
            }
        }
        return errors;
    }
}

