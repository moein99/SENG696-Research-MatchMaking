package src.db;

import org.json.JSONArray;
import org.json.JSONObject;
import src.utils.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class Feedback {
    public int id;
    public int sender_id;
    public int receiver_id;
    public int project_id;
    public String comment;
    public int rate;

    public Feedback(int id, int sender_id, int receiver_id, int project_id, String comment, int rate) {
        this.id = id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.project_id = project_id;
        this.comment = comment;
        this.rate = rate;
    }

    public static Feedback get_by_id(Connection db, int identifier) throws SQLException {
        String query = "select * from feedback where id=" + identifier;
        try (Statement st = db.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("id");
                int sender_id = rs.getInt("sender_id");
                int receiver_id = rs.getInt("receiver_id");
                int project_id = rs.getInt("project_id");
                String comment = rs.getString("comment");
                int rate = rs.getInt("rate");
                return new Feedback(id, sender_id, receiver_id, project_id, comment, rate);
            }
        }
        return null;
    }

    public static ArrayList<Feedback> getUserFeedbacks(Connection db, int userId, boolean isSender) {
        String query = "SELECT * FROM feedback WHERE";
        if (isSender) {
            query += " sender_id=?";
        } else {
            query += " receiver_id=?";
        }

        ArrayList<Feedback> results = new ArrayList<>();

        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                results.add(sqlToModel(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return results;
    }

    private static Feedback sqlToModel(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int senderId = rs.getInt("sender_id");
        int receiverId = rs.getInt("receiver_id");
        int projectId = rs.getInt("project_id");
        String comment = rs.getString("comment");
        int rate = rs.getInt("rate");

        return new Feedback(id, senderId, receiverId, projectId, comment, rate);
    }

    public static void insert(Connection db, JSONObject data) {
        int senderId = data.getInt("sender_id");
        int receiverId = data.getInt("receiver_id");
        int projectId = data.getInt("project_id");
        int rate = data.getInt("rate");
        String comment = data.getString("comment");
        String query = "INSERT INTO feedback (sender_id, receiver_id, project_id, comment, rate) values (?,?,?,?,?)";

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setInt(1, senderId);
            st.setInt(2, receiverId);
            st.setInt(3, projectId);
            st.setString(4, comment);
            st.setInt(5, rate);
            st.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public JSONObject json() {
        JSONObject data = new JSONObject();
        data.put("id", id);
        data.put("sender_id", sender_id);
        data.put("receiver_id", receiver_id);
        data.put("project_id", project_id);
        data.put("comment", comment);
        data.put("rate", rate);

        return data;
    }
}
