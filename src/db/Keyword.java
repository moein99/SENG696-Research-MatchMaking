package src.db;

import org.json.JSONObject;

import java.security.Key;
import java.sql.*;

public class Keyword {
    public int id;
    public String text;

    public Keyword(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public static JSONObject insert(Connection db, String text) {
        String query = "INSERT INTO keyword (text) values (?)";

        long keyword_id = -1;
        JSONObject obj = new JSONObject();

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setString(1, text);
            int rows = st.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        keyword_id = rs.getInt(1);
                        System.out.println("keyword inserted, id: " + keyword_id);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate")) {
                obj.put("message", "The keyword already exists");
            }
        }

        obj.put("id", keyword_id);
        return obj;
    }

    public static Keyword get_by_text(Connection db, String text) {
        String query = "SELECT * FROM keyword WHERE text=?";

        Keyword keyword = null;
        int rows = 0;

        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            st.setString(1, text);
            ResultSet rs = st.executeQuery();
            if (rs.last()) {
                rows = rs.getRow();
            }
            if (rows != 0) {
                rs.first();
                keyword = sqlToModel(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());;
        }
        return keyword;
    }

    private static Keyword sqlToModel(ResultSet rs) throws SQLException {
        return new Keyword(rs.getInt("id"), rs.getString("text"));
    }
}
