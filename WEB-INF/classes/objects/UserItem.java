package objects;

import utils.CustomLogger;

import java.sql.*;

/**
 * Created by Zoran Pucar zoran@medorian.com.
 * User: zoran
 * Date: 2007-jan-06
 * Time: 10:13:04
 * This is copyrighted software. If you got hold
 * of this software by unauthorized means, please
 * contact us at email above.
 */
public class UserItem {
    private String username;
    private String password;
    private String email;
    private int uid = -1;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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


    public boolean save(Connection conn){
        PreparedStatement st = null;
        try {
            if ( this.uid==-1 ) {
                st=conn.prepareStatement("insert into UserItems values(NULL,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            } else {
                st=conn.prepareStatement("update UserItems set username=?,password=?,email=?");
            }
            st.setString(1,this.username);
            st.setString(2,this.password);
            st.setString(3,this.email);
            st.executeUpdate();
            if (this.uid == -1){
                ResultSet rs = st.getGeneratedKeys();
                while ( rs.next()){
                    this.uid = rs.getInt(1);
                }
            }
            st.close();
            return true;
        } catch (SQLException e) {
            CustomLogger.logme(this.getClass().getName(), e.toString(),true);
            return false;
        }
    }

    public void delete(Connection conn){
        if ( this.uid != -1 ){
            try {
                PreparedStatement st = conn.prepareStatement("delete from UserItems where uid=?");
                st.setInt(1,this.uid);
                st.executeUpdate();
                st.close();
            } catch (SQLException e) {
                CustomLogger.logme(this.getClass().getName(), e.toString(),true);
            }
        }
    }

}
