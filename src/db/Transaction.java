package src.db;

import org.json.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Transaction {
    public int id;
    public int sender_id;
    public int receiver_id;
    public String description;
    public int amount;

    public Transaction(int id, int sender_id, int receiver_id, String description, int amount) {
        this.id = id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.description = description;
        this.amount = amount;
    }

    public static Transaction get_by_id(Connection db, int identifier) throws SQLException {
        String query = "select * from transaction where id=" + identifier;
        try (Statement st = db.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("id");
                int sender_id = rs.getInt("sender_id");
                int receiver_id = rs.getInt("receiver_id");
                String description = rs.getString("description");
                int amount = rs.getInt("amount");
                return new Transaction(id, sender_id, receiver_id, description, amount);
            }
        }
        return null;
    }

    public static void insert(Connection db, int ownerId, int assigneeId, String description, int amount) {
        String query = "INSERT INTO transaction (sender_id, receiver_id, description, amount) values (?,?,?,?)";

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setInt(1, ownerId);
            st.setInt(2, assigneeId);
            st.setString(3, description);
            st.setInt(4, amount);
            st.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
