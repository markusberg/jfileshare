package views;

import objects.UserItem;
import objects.FileItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import utils.CustomLogger;

/**
 * SECTRA.
 * User: zoran
 * Date: Jan 11, 2007
 * Time: 10:27:26 AM
 */
public class UserItemView {
    private Connection conn = null;
    private int id = -1;
    private String username = null;
    private String email = null;
    private int creator = -1;
    public static int SEARCH_EMAIL = 1;
    public static int SEARCH_BY_CREATOR = 1;
    private UserItem user = null;
    private Map<Integer,UserItem> children = new TreeMap<Integer,UserItem>();
    private Map<Integer,UserItem> users = new TreeMap<Integer,UserItem>();
    private Map<Integer, FileItem> files = new TreeMap<Integer,FileItem>();

    private static final int RESULT_TYPE_USER = 1;
    private static final int RESULT_TYPE_FILES = 2;
    private static final int RESULT_TYPE_CHILDREN = 3;

    public UserItemView(){

    }

    public UserItemView(Connection conn){
        this.conn = conn;
    }

    public UserItemView(Connection conn, int id) {
        this.conn = conn;
        this.id = id;
        searchUser();
        CustomLogger.logme(this.getClass().getName(),"User set. Getting children...");
        //UserItemView uview = new UserItemView(conn,this.id,UserItemView.SEARCH_BY_CREATOR);
        //this.user = uview.getUserItem();
        //CustomLogger.logme(this.getClass().getName(),"In conn,uid found " + uview.getUsers().size());
        if ( this.user != null ){
            //this.user.setChildren(uview.getUsers());
        } else {
            CustomLogger.logme(this.getClass().getName(),"For some reason this.user is null");
        }
    }

    public UserItemView(Connection conn, int creator, int SEARCH_BY_CREATOR) {
        CustomLogger.logme(this.getClass().getName(),"Searching for children of user " + creator);
        this.conn = conn;
        this.creator = creator;
        this.users = searchChildren();
        String plural = this.users.size()==1?"":"ren";
        CustomLogger.logme(this.getClass().getName(),"Found " + this.users.size() + " child" + plural );
    }


    public UserItemView(Connection conn, String username) {
        this.conn = conn;
        this.username = username;
        searchUser();
        if ( this.user != null ){
            CustomLogger.logme(this.getClass().getName(),"User set. Getting children...");
            UserItemView uview = new UserItemView(conn,this.user.getUid(),UserItemView.SEARCH_BY_CREATOR);
            CustomLogger.logme(this.getClass().getName(),"In conn,username found " + uview.getUsers().size());
            this.user.setChildren(uview.getUsers());
        }
    }


    public UserItemView(Connection conn, String email, int SEARCH_EMAIL) {
        this.conn = conn;
        this.email = email;
        searchUser();
        UserItemView uview = new UserItemView(conn,this.user.getUid(),UserItemView.SEARCH_BY_CREATOR);
        this.user.setChildren(uview.getUsers());
    }


    private void searchUser(){
        PreparedStatement st = null;
        ResultSet rs = null;
        if ( this.id != -1 ){
            try {

                st = this.conn.prepareStatement("select * from UserItems where UserItems.uid=?");
                st.setInt(1,this.id);
                rs = st.executeQuery();
                if (rs == null ){
                    CustomLogger.logme(this.getClass().getName(),"RS seams to be NULL... WTF WTF");
                } else {
                    populateFromSQL(rs, RESULT_TYPE_USER );
                }

                st = this.conn.prepareStatement("select * from FileItems where FileItems.owner=?");
                st.setInt(1,this.id);
                rs = st.executeQuery();
                if ( rs == null ){
                    CustomLogger.logme(this.getClass().getName(),"RS seams to be null when getting fileitems... WTF WTF");
                } else {
                    populateFromSQL(rs, RESULT_TYPE_FILES);
                }

                st = this.conn.prepareStatement("select * from UserItems where creator=?");
                st.setInt(1,this.id);
                rs = st.executeQuery();
                if ( rs == null ){
                    CustomLogger.logme(this.getClass().getName(),"RS null when getting children... WTF WTF");
                } else {
                    populateFromSQL(rs, RESULT_TYPE_CHILDREN);
                }


            } catch ( SQLException e){
                CustomLogger.logme(this.getClass().getName(),e.toString(),true);
            }


        } else if ( this.username != null ){
            try {
                CustomLogger.logme(this.getClass().getName(),"THIS USERNAME Should be " + this.username);
                st = this.conn.prepareStatement("select * from UserItems where UserItems.username=?");
                st.setString(1,this.username);
                rs = st.executeQuery();

                if (rs == null ){
                    CustomLogger.logme(this.getClass().getName(),"RS seams to be NULL... WTF WTF");
                } else {
                    populateFromSQL(rs, RESULT_TYPE_USER );
                }

                if ( this.user != null ){
                    CustomLogger.logme(this.getClass().getName(),"Searching for files uploaded by " + this.user.getUsername());
                    st = this.conn.prepareStatement("select * from FileItems where FileItems.owner=?");
                    st.setInt(1,this.user.getUid());
                    rs = st.executeQuery();
                    if ( rs == null ){
                        CustomLogger.logme(this.getClass().getName(),"RS seams to be null when getting fileitems... WTF WTF");
                    } else {
                        populateFromSQL(rs, RESULT_TYPE_FILES);
                    }

                    CustomLogger.logme(this.getClass().getName(),"Searching for users created by " + this.user.getUsername());
                    st = this.conn.prepareStatement("select * from UserItems where creator=?");
                    st.setInt(1,this.user.getUid());
                    rs = st.executeQuery();
                    if ( rs == null ){
                        CustomLogger.logme(this.getClass().getName(),"RS null when getting children... WTF WTF");
                    } else {
                        populateFromSQL(rs, RESULT_TYPE_CHILDREN);
                    }
                }
            } catch ( SQLException e){
                CustomLogger.logme(this.getClass().getName(),e.toString(),true);
            }
        } else if ( this.email != null ){
            try {
                CustomLogger.logme(this.getClass().getName(),"THIS EMAIL Should be " + this.email);
                st = this.conn.prepareStatement("select * from UserItems where UserItems.email=?");
                st.setString(1,this.email);
                rs = st.executeQuery();
                if (rs == null ){
                    CustomLogger.logme(this.getClass().getName(),"RS seams to be NULL... WTF WTF");
                } else {
                    populateFromSQL(rs, RESULT_TYPE_USER );
                }

                st = this.conn.prepareStatement("select * from FileItems where FileItems.owner=?");
                st.setInt(1,this.user.getUid());
                rs = st.executeQuery();
                if ( rs == null ){
                    CustomLogger.logme(this.getClass().getName(),"RS seams to be null when getting fileitems... WTF WTF");
                } else {
                    populateFromSQL(rs, RESULT_TYPE_FILES);
                }

                st = this.conn.prepareStatement("select * from UserItems where creator=?");
                st.setInt(1,this.user.getUid());
                rs = st.executeQuery();
                if ( rs == null ){
                    CustomLogger.logme(this.getClass().getName(),"RS null when getting children... WTF WTF");
                } else {
                    populateFromSQL(rs, RESULT_TYPE_CHILDREN);
                }
            } catch ( SQLException e){
                CustomLogger.logme(this.getClass().getName(),e.toString(),true);
            }
        }
        try {
            if ( rs != null ) rs.close();

            if ( st != null ) st.close();
        } catch (SQLException e) {
            CustomLogger.logme(this.getClass().getName(), e.toString(), true);
        }
    }


    private Map<Integer,UserItem> searchChildren(){
        if ( this.conn == null || this.creator == -1 ) return null;
        Map<Integer,UserItem> users = new TreeMap<Integer,UserItem>();
        try {
            PreparedStatement st = this.conn.prepareStatement("SELECT * FROM UserItems WHERE creator=?");
            st.setInt(1,this.creator);
            ResultSet rs = st.executeQuery();
            while ( rs.next() ){
                UserItem user = new UserItem();
                user.setUid(rs.getInt("UserItems.uid"));
                user.setUsername(rs.getString("UserItems.username"));
                user.setPassword(rs.getString("UserItems.password"));
                user.setEmail(rs.getString("UserItems.email"));
                user.setUserType(rs.getInt("UserItems.usertype"));
                user.setCreated(rs.getTimestamp("UserItems.created"));
                user.setLastlogin(rs.getTimestamp("UserItems.lastlogin"));
                user.setExpires(rs.getBoolean("UserItems.expires"));
                if ( user.expires() ){
                    user.setExpiry(rs.getInt("UserItems.daystoexpire"));
                }
                users.put(user.getUid(),user);

            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            CustomLogger.logme(this.getClass().getName(),e.toString(),true);
        }
        return users;

    }


    private void populateFromSQL(ResultSet rs, int RESULT_TYPE){

        if ( RESULT_TYPE == RESULT_TYPE_USER){
            try {
                while ( rs.next() ){

                    UserItem user = new UserItem();
                    user.setUid(rs.getInt("UserItems.uid"));
                    user.setUsername(rs.getString("UserItems.username"));
                    user.setPassword(rs.getString("UserItems.password"));
                    user.setEmail(rs.getString("UserItems.email"));
                    user.setUserType(rs.getInt("UserItems.usertype"));
                    user.setCreated(rs.getTimestamp("UserItems.created"));
                    user.setLastlogin(rs.getTimestamp("UserItems.lastlogin"));
                    user.setExpires(rs.getBoolean("UserItems.expires"));
                    if ( user.expires() ){
                        user.setExpiry(rs.getInt("UserItems.daystoexpire"));
                    }
                    this.user = user;

                }
            } catch (SQLException e) {
                CustomLogger.logme(this.getClass().getName(),e.toString(),true);
            }
        } else if ( RESULT_TYPE == RESULT_TYPE_FILES ){
            try {
                while ( rs.next() ){
                    FileItem file = new FileItem();
                    file.setFid(rs.getInt("FileItems.fid"));
                    file.setName(rs.getString("FileItems.name"));
                    file.setType(rs.getString("FileItems.type"));
                    file.setSize(rs.getDouble("FileItems.size"));
                    file.setMd5sum(rs.getString("FileItems.md5sum"));
                    file.setPermanent(rs.getBoolean("FileItems.permanent"));
                    file.setEnabled(rs.getBoolean("FileItems.enabled"));
                    file.setDownloads(rs.getInt("FileItems.downloads"));
                    if ( rs.wasNull()){
                        file.setDownloads(-1);
                    }
                    file.setPassword(rs.getString("FileItems.password"));
                    if ( rs.wasNull()) file.setPassword(null);
                    file.setDdate(rs.getTimestamp("FileItems.ddate"));
                    file.setExpiration(rs.getTimestamp("FileItems.expiration"));
                    file.setOwner(this.user);
                    this.files.put(file.getFid(),file);
                    this.user.addFile(file);
                }
            } catch (SQLException e) {
                CustomLogger.logme(this.getClass().getName(), e.toString(), true);
            }

        } else if ( RESULT_TYPE == RESULT_TYPE_CHILDREN ){
            try {
                while ( rs.next() ){
                    UserItem user = new UserItem();
                    user.setUid(rs.getInt("UserItems.uid"));
                    user.setUsername(rs.getString("UserItems.username"));
                    user.setPassword(rs.getString("UserItems.password"));
                    user.setEmail(rs.getString("UserItems.email"));
                    user.setUserType(rs.getInt("UserItems.usertype"));
                    user.setCreated(rs.getTimestamp("UserItems.created"));
                    user.setLastlogin(rs.getTimestamp("UserItems.lastlogin"));
                    user.setExpires(rs.getBoolean("UserItems.expires"));
                    if ( user.expires() ){
                        user.setExpiry(rs.getInt("UserItems.daystoexpire"));
                    }
                    this.children.put(user.getUid(),user);
                    this.user.addChild(user);

                }
            } catch (SQLException e) {
                CustomLogger.logme(this.getClass().getName(), e.toString(), true);
            }
        }
    }

    public UserItem getUserItem(){
        return this.user;
    }

    public Map<Integer,UserItem> getChildren(){
        return this.children;
    }

    public Map<Integer,UserItem> getUsers(){
        return this.users;
    }

    public Map<Integer,FileItem> getFiles(){
        return this.files;
    }

    public void remove(int fid){
        this.files.remove(fid);
    }


    public Set<UserItem> getExpiredUsers(Connection conn){
        Set<Integer> uids_expiring = new HashSet<Integer>();
        Set<UserItem> expired_users = new HashSet<UserItem>();
        try {
            PreparedStatement st = conn.prepareStatement("SELECT uid FROM UserItems WHERE expires=?");
            st.setBoolean(1,true);
            ResultSet rs = st.executeQuery();
            while ( rs.next() ){
                uids_expiring.add(rs.getInt(1));

            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            CustomLogger.logme(this.getClass().getName(), e.toString(), true);
        }
        CustomLogger.logme(this.getClass().getName(),"CHECKING " + uids_expiring.size() + " UZRZ");
        for ( Integer uid: uids_expiring ){
            UserItemView uview = new UserItemView(conn,uid);
            UserItem user = uview.getUserItem();
            if ( user.isExpired() ){
                CustomLogger.logme(this.getClass().getName(),"User " + user.getUid() + " is expired");
                expired_users.add(user);
            } else {
                CustomLogger.logme(this.getClass().getName(),"User " + user.getUid() + " is not expired");
            }
        }

        return expired_users;


    }

}
