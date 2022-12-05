package src.db;

import org.json.JSONObject;
import src.utils.Utils;

import java.security.PublicKey;
import java.sql.*;
import java.util.ArrayList;

public class Bid {
    public int id;
    public int bidder_id;
    public int project_id;
    public int hourly_rate;
    public String description;
    public String status;

    public static final String SENT_TO_PROVIDER = "SP";
    public static final String ACCEPTED = "A";
    public static final String REJECTED = "R";

    public Bid(int id, int bidder_id, int project_id, int hourly_rate, String status, String description) {
        this.id = id;
        this.bidder_id = bidder_id;
        this.project_id = project_id;
        this.hourly_rate = hourly_rate;
        this.description = description;
        this.status = status;
    }

    public static Bid getById(Connection db, int id) {
        String query = "SELECT * FROM bid WHERE id=?";

        Bid bid = null;
        int rows = 0;

        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            st.setInt(1, id);
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

    public static Bid getById(Connection db, int bidder_id, int project_id) {
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

    public static ArrayList<Bid> getByProjectId(Connection db, int project_id) {
        String query = "SELECT * FROM bid WHERE project_id=?";
        ArrayList<Bid> results = new ArrayList<>();

        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            st.setInt(1, project_id);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                results.add(sqlToModel(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());;
        }

        return results;
    }

    public static ArrayList<Bid> getByProjectIds(Connection db, ArrayList<Integer> projectIds, String status) {
        String query = "SELECT * FROM bid WHERE status=? AND project_id IN (" + Utils.concatIntsWithCommas(projectIds) + ")";

        ArrayList<Bid> results = new ArrayList<>();

        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            st.setString(1, status);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                results.add(sqlToModel(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return results;
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

    public static void update_status(Connection db, ArrayList<Integer> ids, String newStatus) {
        String query = "UPDATE bid SET status=? WHERE id IN (" + Utils.concatIntsWithCommas(ids) + ")";

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setString(1, newStatus);
            st.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
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

    public static ArrayList<Bid> getClientBids(Connection db, int userId) {
        String query = "SELECT * FROM bid WHERE status IN ('SP', 'A') AND bidder_id=?";

        ArrayList<Bid> results = new ArrayList<>();

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

    public String getContractStatus(Connection db) {
        Contract contract = Contract.getWithBidId(db, id);
        if (contract == null) {
            return Contract.NOT_CREATED;
        }
        return contract.status();
    }
}
