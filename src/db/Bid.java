package src.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Bid {
    public int id;
    public int bidder_id;
    public int project_id;
    public int hourly_rate;
    public String status;

    public Bid(int id, int bidder_id, int project_id, int hourly_rate, String status) {
        this.id = id;
        this.bidder_id = bidder_id;
        this.project_id = project_id;
        this.hourly_rate = hourly_rate;
        this.status = status;
    }

    public static Bid get_by_id(Connection db, int identifier) throws SQLException {
        String query = "select * from bid where id=" + identifier;
        try (Statement st = db.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("id");
                int bidder_id = rs.getInt("bidder_id");
                int project_id = rs.getInt("project_id");
                int hourly_rate = rs.getInt("hourly_rate");
                String status = rs.getString("status");
                return new Bid(id, bidder_id, project_id, hourly_rate, status);
            }
        }
        return null;
    }
}
