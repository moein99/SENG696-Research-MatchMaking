package src.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class UserKeyword {
    public int user_id;
    public int keyword_id;

    public UserKeyword(int user_id, int keyword_id) {
        this.user_id = user_id;
        this.keyword_id = keyword_id;
    }

    public static UserKeyword get_by_id(Connection db, int usr_id, int kw_id) throws SQLException {
        String query = "select * from userKeyword where user_id=" + usr_id + " AND keyword_id=" + kw_id;
        try (Statement st = db.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int user_id = rs.getInt("user_id");
                int keyword_id = rs.getInt("keyword_id");
                return new UserKeyword(user_id, keyword_id);
            }
        }
        return null;
    }
}
