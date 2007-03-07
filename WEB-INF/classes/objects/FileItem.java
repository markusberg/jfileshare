package objects;

import utils.CustomLogger;

import java.io.File;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import config.Config;

import javax.servlet.http.HttpServletRequest;

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

    private boolean enabled = true;

    public FileItem(){

    }

    public FileItem(int fid){
        this.fid = fid;
    }

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

    public boolean isEnabled(){
        return this.enabled;
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    

    public boolean save(Connection conn){
        PreparedStatement st = null;
        if ( this.fid == -1 ){
            try {
                st = conn.prepareStatement("insert into FileItems values(NULL,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
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
                st.setBoolean(11,this.enabled);
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
                st = conn.prepareStatement("update FileItems set permanent=?,downloads=?,expiration=?,enabled=?,password=? where fid=?");
                st.setBoolean(1,this.permanent);
                st.setInt(2,this.downloads);
                if ( expiration == null ){
                    st.setNull(3,java.sql.Types.TIMESTAMP);
                } else {
                    st.setTimestamp(3,new Timestamp(this.expiration.getTime()));
                }
                st.setBoolean(4,this.enabled);
                if ( this.password == null ){
                    st.setNull(5,java.sql.Types.VARCHAR);
                } else {
                    st.setString(5,this.password);
                }
                st.setInt(6,this.fid);
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
                this.enabled = rs.getBoolean("enabled");
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


    public boolean search(Connection conn, int fid){
        try {
            PreparedStatement st = conn.prepareStatement("select * from FileItems,UserItems where FileItems.owner=UserItems.uid and fid=?");
            st.setInt(1,fid);
            ResultSet rs = st.executeQuery();
            while ( rs.next() ){
                this.fid = rs.getInt("fid");
                this.name = rs.getString("name");
                this.type = rs.getString("type");
                this.size = rs.getDouble("size");
                this.md5sum = rs.getString("md5sum");
                this.permanent = rs.getBoolean("permanent");
                this.enabled = rs.getBoolean("enabled");
                if ( rs.wasNull() ) this.permanent = false;
                rs.getInt("downloads");
                if ( ! rs.wasNull()) this.downloads = rs.getInt("downloads");
                if ( rs.getString("FileItems.password") != null ) this.password = rs.getString("FileItems.password");
                this.ddate = rs.getTimestamp("ddate");
                if ( rs.getTimestamp("expiration") != null ) this.expiration = rs.getTimestamp("expiration");

                /// REMEMBER using view here needs making sure that view doesn't recurse back to FileItems when populating users.
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

    public boolean search(Connection conn, String md5hash, int fid){
        try {
            PreparedStatement st = conn.prepareStatement("select * from FileItems,UserItems where FileItems.owner=UserItems.uid and md5sum=? and fid=?");
            st.setString(1,md5hash);
            st.setInt(2,fid);
            ResultSet rs = st.executeQuery();
            while ( rs.next() ){
                this.fid = rs.getInt("fid");
                this.name = rs.getString("name");
                this.type = rs.getString("type");
                this.size = rs.getDouble("size");
                this.md5sum = rs.getString("md5sum");
                this.permanent = rs.getBoolean("permanent");
                this.enabled = rs.getBoolean("enabled");
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
        
        //this.getFile().delete();
        FileItem me = new FileItem();
        me.search(conn,this.fid);
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

    public int registerDownload(Connection conn, HttpServletRequest request){
        String remote_addr = request.getRemoteAddr();
        int dvalue = -2;
        try {
            Statement st = conn.createStatement();
            CustomLogger.logme(this.getClass().getName(),"Locking FileItems table for upgrade");
            //st.execute("LOCK TABLES FileItems");
            PreparedStatement st2 = conn.prepareStatement("SELECT downloads FROM FileItems WHERE fid=?");
            st2.setInt(1,this.fid);
            ResultSet rs = st2.executeQuery();
            int downloads = -1;
            while ( rs.next() ){
                downloads = rs.getInt("downloads");
                if ( rs.wasNull()) {
                    //If downloads is null, set it to -1 otherwise decrease one
                    downloads = -1;
                } else {
                    downloads = downloads - 1;
                }

            }

            
            st2 = conn.prepareStatement("UPDATE FileItems SET downloads=? where fid=?");
            if ( downloads == -1 ){
                st2.setNull(1, Types.INTEGER);
            } else {
                st2.setInt(1,downloads);
            }

            st2.setInt(2,this.fid);
            st2.executeUpdate();
            st2.close();
            rs.close();
            CustomLogger.logme(this.getClass().getName(),"Unlocking tables");
            st.execute("UNLOCK TABLES");
            st.close();
            dvalue = downloads;
            CustomLogger.logme(this.getClass().getName(),"Logging download");
            PreparedStatement st3 = conn.prepareStatement("INSERT INTO DownloadLogs VALUES(now(),?,?)");
            st3.setInt(1,this.fid);
            st3.setString(2,remote_addr);
            st3.executeUpdate();
            st3.close();

        } catch (SQLException e) {
            CustomLogger.logme(this.getClass().getName(), e.toString(), true);
        }

        return dvalue;
    }

    public Set<DownloadLog> getLogs(Connection conn){
        Set<DownloadLog> logs = new HashSet<DownloadLog>();

        try {
            PreparedStatement st = conn.prepareStatement("SELECT * FROM DownloadLogs WHERE fid=?");
            st.setInt(1,this.fid);
            ResultSet rs = st.executeQuery();
            while ( rs.next() ){
                DownloadLog log = new DownloadLog();
                log.setIp(rs.getString(3));
                log.setTime(rs.getTimestamp(1));
                logs.add(log);

            }
        } catch (SQLException e) {
            CustomLogger.logme(this.getClass().getName(), e.toString(), true);
        }

        return logs;
    }

    public class DownloadLog{
        private String ip;
        private Date time;

        public void setIp(String ip){
            this.ip = ip;
        }

        public String getIp(){
            return this.ip;
        }

        public void setTime(Date time){
            this.time = time;
        }

        public Date getTime(){
            return this.time;
        }


}
}
