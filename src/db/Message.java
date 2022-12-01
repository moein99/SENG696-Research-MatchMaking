package src.db;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Message {
    public int id;
    public int sender_id;
    public int receiver_id;
    public int project_id;
    public String text;

    public Message(int id, int sender_id, int receiver_id, int project_id, String text) {
        this.id = id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.project_id = project_id;
        this.text = text;
    }

    public static boolean insert(Connection db, JSONObject data) {
        String query = "INSERT INTO message (sender_id, receiver_id, project_id, text) values (?,?,?,?)";

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setInt(1, data.getInt("sender_id"));
            st.setInt(2, data.getInt("receiver_id"));
            st.setInt(3, data.getInt("project_id"));
            st.setString(4, data.getString("text"));
            st.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }

        return true;
    }

    public static Message get_by_id(Connection db, int identifier) throws SQLException {
        String query = "select * from message where id=" + identifier;
        try (Statement st = db.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("id");
                int sender_id = rs.getInt("sender_id");
                int receiver_id = rs.getInt("receiver_id");
                int project_id = rs.getInt("project_id");
                String text = rs.getString("text");
                return new Message(id, sender_id, receiver_id, project_id, text);
            }
        }
        return null;
    }

    public static JSONArray get_by_projectId(Connection db, int projectId) {
        String query = "SELECT * FROM message WHERE project_id=?";

        JSONArray messages = new JSONArray();

        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            st.setInt(1, projectId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                messages.put(sqlToJson(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return messages;
    }

    private static JSONObject sqlToJson(ResultSet rs) throws SQLException {
        JSONObject fields = new JSONObject();
        fields.put("id", rs.getInt("id"));
        fields.put("sender_id", rs.getInt("sender_id"));
        fields.put("receiver_id", rs.getInt("receiver_id"));
        fields.put("project_id", rs.getInt("project_id"));
        fields.put("text", rs.getString("text"));
        return fields;
    }

}
