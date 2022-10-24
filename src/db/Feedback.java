package src.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
}
