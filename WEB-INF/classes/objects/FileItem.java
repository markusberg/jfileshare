package objects;

import utils.CustomLogger;

import java.io.File;
import java.util.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.SQLException;

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
    private Float size;
    private String md5sum;


    private boolean permanent;
    private int downloads;
    private String password;
    private Date ddate;
    private Date expiration;


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

    public Float getSize() {
        return size;
    }

    public void setSize(Float size) {
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

    public boolean save(Connection conn){
        PreparedStatement st = null;
        if ( this.fid == -1 ){
            try {
                st = conn.prepareStatement("insert into FileItems values(NULL,?,?,?,?,?,?,?,?)");
                st.setString(1,this.name);
                st.setString(2,this.type);
                st.setFloat(3,this.size);
                st.setString(4,this.md5sum);
                st.setBoolean(5,this.permanent);
                st.setInt(6,this.downloads);
                st.setTimestamp(7,new Timestamp(this.ddate.getTime()));
                if ( this.expiration == null ){
                    st.setNull(8,java.sql.Types.TIMESTAMP);
                } else {
                    st.setTimestamp(8,new Timestamp(this.expiration.getTime()));
                }
                st.executeUpdate();
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
                st.executeUpdate();
                st.close();
                return true;
            } catch (SQLException e) {
                CustomLogger.logme(this.getClass().getName(),e.toString(),true);
                return false;
            }
        }
    }
}
