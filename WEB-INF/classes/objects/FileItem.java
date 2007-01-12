package objects;

import utils.CustomLogger;

import java.io.File;
import java.util.Date;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import config.Config;

/**
 * SECTRA.
 * User: zoran
 * Date: Jan 8, 2007
 * Time: 9:59:20 AM
 */
public class FileItem {
    private int fid = -1;
    private File file;
    private String name;
    private String type;
    private Double size = 0d;
    private String md5sum;


    private boolean permanent = true;
    private int downloads = -1;
    private String password;
    private Date ddate;
    private Date expiration;

    private UserItem owner;


    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getDdate() {
        return ddate;
    }

    public void setDdate(Date ddate) {
        this.ddate = ddate;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }


    public UserItem getOwner() {
        return owner;
    }

    public void setOwner(UserItem owner) {
        this.owner = owner;
    }


    

    public boolean save(Connection conn){
        PreparedStatement st = null;
        if ( this.fid == -1 ){
            try {
                st = conn.prepareStatement("insert into FileItems values(NULL,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                st.setString(1,this.name);
                st.setString(2,this.type);
                st.setDouble(3,this.size);
                st.setString(4,this.md5sum);
                st.setBoolean(5,this.permanent);
                if ( this.downloads == -1 ){
                    st.setNull(6,java.sql.Types.INTEGER);
                } else {
                    st.setInt(6,this.downloads);
                }
                if ( this.password != null){
                    st.setString(7,this.password);
                } else {
                    st.setNull(7,java.sql.Types.VARCHAR);
                }
                st.setTimestamp(8,new Timestamp(this.ddate.getTime()));
                if ( this.expiration == null ){
                    st.setNull(9,java.sql.Types.TIMESTAMP);
                } else {
                    st.setTimestamp(9,new Timestamp(this.expiration.getTime()));
                }
                st.setInt(10,this.owner.getUid());
                st.executeUpdate();
                ResultSet rs = st.getGeneratedKeys();
                while (rs.next()){
                    this.fid = rs.getInt(1);
                }
                st.close();
                return true;
            } catch (SQLException e) {
                CustomLogger.logme(this.getClass().getName(),e.toString(),true);
                return false;
            }

        } else {
            try {
                st = conn.prepareStatement("update FileItems set permanent=?,downloads=?,expiration=? where fid=?");
                st.setBoolean(1,this.permanent);
                st.setInt(2,this.downloads);
                if ( expiration == null ){
                    st.setNull(3,java.sql.Types.TIMESTAMP);
                } else {
                    st.setTimestamp(3,new Timestamp(this.expiration.getTime()));
                }
                st.setInt(4,this.fid);
                st.executeUpdate();
                st.close();
                return true;
            } catch (SQLException e) {
                CustomLogger.logme(this.getClass().getName(),e.toString(),true);
                return false;
            }
        }
    }

    public boolean search(Connection conn, String md5hash){
        try {
            PreparedStatement st = conn.prepareStatement("select * from FileItems,UserItems where FileItems.owner=UserItems.uid and md5sum=?");
            st.setString(1,md5hash);
            ResultSet rs = st.executeQuery();
            while ( rs.next() ){
                this.fid = rs.getInt("fid");
                this.name = rs.getString("name");
                this.type = rs.getString("type");
                this.size = rs.getDouble("size");
                this.md5sum = rs.getString("md5sum");
                this.permanent = rs.getBoolean("permanent");
                if ( rs.wasNull() ) this.permanent = false;
                rs.getInt("downloads");
                if ( ! rs.wasNull()) this.downloads = rs.getInt("downloads");
                if ( rs.getString("FileItems.password") != null ) this.password = rs.getString("FileItems.password");
                this.ddate = rs.getTimestamp("ddate");
                if ( rs.getTimestamp("expiration") != null ) this.expiration = rs.getTimestamp("expiration");
                UserItem owner = new UserItem();
                owner.setUid(rs.getInt("uid"));
                owner.setUsername(rs.getString("username"));
                owner.setPassword(rs.getString("UserItems.password"));
                owner.setEmail(rs.getString("email"));
                this.owner = owner;
                this.file = new File(Config.getFilestore() + "/" + this.fid );
                return true;
            }
        } catch (SQLException e) {
            CustomLogger.logme(this.getClass().getName(), e.toString(), true);
        }

        return false;
    }

    public boolean delete(Connection conn){
        //First load the file in order to remove the actual file from the disk;

        FileItem me = new FileItem();
        me.search(conn,this.md5sum);
        File file = me.getFile();
        file.delete();


        try {
            PreparedStatement st = conn.prepareStatement("delete from FileItems where fid=?");
            st.setInt(1,this.fid);
            st.executeUpdate();
            st.close();

        } catch (SQLException e){
            CustomLogger.logme(this.getClass().getName(),e.toString(),true);
            return false;
        }
        return true;
    }
}
