package views;

import objects.FileItem;

import java.util.Set;
import java.util.HashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: zoran
 * Date: Sep 22, 2008
 * Time: 9:43:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileItemView {
    public Set<FileItem> getExpiredFiles(Connection conn){
        Set<FileItem> retval = new HashSet<FileItem>();
        try {
            PreparedStatement st = conn.prepareStatement("select * from FileItems where ( expiration<now() or expiration is null ) and permanent=?");
            st.setBoolean(1,false);
            ResultSet rs = st.executeQuery();
            while ( rs.next() ){
                FileItem file = new FileItem();
                file.setFid(rs.getInt("fid"));
                file.setName(rs.getString("name"));
                file.setType(rs.getString("type"));
                file.setSize(rs.getDouble("size"));
                file.setMd5sum(rs.getString("md5sum"));
                file.setPermanent(rs.getBoolean("permanent"));
                file.setDownloads(rs.getInt("downloads"));
                file.setPassword(rs.getString("password"));
                file.setDdate(rs.getDate("ddate"));
                file.setExpiration(rs.getDate("expiration"));
                file.setEnabled(rs.getBoolean("enabled"));
                retval.add(file);

            }
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        return retval;
    }
}
