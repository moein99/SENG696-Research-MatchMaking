package src.db;

import org.json.JSONObject;
import src.utils.Utils;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Bid {
    public int id;
    public int bidder_id;
    public int project_id;
    public int hourly_rate;
    public String description;
    public String status;

    public Bid(int id, int bidder_id, int project_id, int hourly_rate, String status, String description) {
        this.id = id;
        this.bidder_id = bidder_id;
        this.project_id = project_id;
        this.hourly_rate = hourly_rate;
        this.description = description;
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
                String description = rs.getString("description");
                String status = rs.getString("status");
                return new Bid(id, bidder_id, project_id, hourly_rate, description, status);
            }
        }
        return null;
    }

    public static Bid get_by_id(Connection db, int bidder_id, int project_id) {
        String query = "SELECT * FROM bid WHERE bidder_id=? AND project_id=?";

        Bid bid = null;
        int rows = 0;

        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            st.setInt(1, bidder_id);
            st.setInt(2, project_id);
            ResultSet rs = st.executeQuery();
            if (rs.last()) {
                rows = rs.getRow();
            }
            if (rows != 0) {
                rs.first();
                bid = sqlToModel(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());;
        }

        return bid;
    }

    public static JSONObject insert(Connection db, JSONObject data) {
        int projectId = data.getInt("project_id");
        int bidderId = data.getInt("bidder_id");
        int hourlyRate = data.getInt("amount");
        String description = data.getString("description");

        String query = "INSERT INTO bid (project_id, bidder_id, hourly_rate, description) values (?,?,?,?)";

        long bidId = -1;
        JSONObject obj = new JSONObject();

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setInt(1, projectId);
            st.setInt(2, bidderId);
            st.setInt(3, hourlyRate);
            st.setString(4, description);
            int rows = st.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        bidId = rs.getInt(1);
                        System.out.println("bid inserted, id: " + bidId);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        obj.put("id", bidId);
        return obj;
    }

    private static Bid sqlToModel(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int bidderId = rs.getInt("bidder_id");
        int projectId = rs.getInt("project_id");
        int hourlyRate = rs.getInt("hourly_rate");
        String description = rs.getString("description");
        String status = rs.getString("status");

        return new Bid(id, bidderId, projectId, hourlyRate, status, description);
    }
}
