package src.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
}
