package src.db;

import org.json.JSONObject;

import java.sql.*;
import java.util.Date;

public class UserKeyword {
    public int user_id;
    public int keyword_id;

    public UserKeyword(int user_id, int keyword_id) {
        this.user_id = user_id;
        this.keyword_id = keyword_id;
    }

    public static JSONObject insert(Connection db, String userId, String keywordId) {
        String query = "INSERT INTO userKeyword (user_id, keyword_id) VALUES (?,?)";

        long userIdFromDB = -1;
        long keywordIdFromDB = -1;
        JSONObject obj = new JSONObject();

        try (PreparedStatement st = db.prepareStatement(query)) {
            st.setString(1, userId);
            st.setString(2, keywordId);
            st.executeUpdate();
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate")) {
                obj.put("message", "The UserKeyword already exists");
            }
        }

        obj.put("user_id", userIdFromDB);
        obj.put("keyword_id", keywordIdFromDB);
        return obj;
    }
}
