package src.db;

import src.utils.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Contract {
    public int id;
    public int bid_id;
    public int provider_id;
    public int client_id;
    public String description;
    public String accepted_by_provider;
    public String  accepted_by_client;
    public static final String WAITING_FOR_BOTH = "WFB";
    public static final String ACCEPTED_BY_BOTH = "ABB";
    public static final String WAITING_FOR_CLIENT = "WFC";
    public static final String WAITING_FOR_PROVIDER = "WFP";
    public static final String REJECTED = "R";
    public static final String ACCEPTED = "A";
    public static final String NOT_CREATED = "NC";

    public Contract(int id, int bid_id, int provider_id, int client_id, String description, String accepted_by_provider, String accepted_by_client) {
        this.id = id;
        this.bid_id = bid_id;
        this.provider_id = provider_id;
        this.client_id = client_id;
        this.description = description;
        this.accepted_by_provider = accepted_by_provider;
        this.accepted_by_client = accepted_by_client;
    }

    public static void insert(Connection db, int bidId) {
        Bid acceptedBid = Bid.getById(db, bidId);
        Project project = Project.get_with_id(db, acceptedBid.project_id);

        String query = "INSERT INTO contract (bid_id, provider_id, client_id, description) VALUES (?,?,?,?)";
        String description = "This is the contract for the project '" + project.title + "'. " +
                "Owner of the project is '" + User.get_with_id(db, project.ownerId).username + "' and " +
                "the project is assigned to '" + User.get_with_id(db, acceptedBid.bidder_id).username + "'. 30% of the client income " +
                "in this project will be for the system. The project will be initialized when both parties accept the contract.";

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setInt(1, acceptedBid.id);
            st.setInt(2, project.ownerId);
            st.setInt(3, acceptedBid.bidder_id);
            st.setString(4, description);
            st.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static Contract getWithBidId(Connection db, int bidId) {
        String query = "SELECT * FROM contract WHERE bid_id=?";

        Contract contract = null;
        int rows = 0;

        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            st.setInt(1, bidId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                rows = rs.getRow();
            }
            if (rows != 0) {
                rs.first();
                contract = sqlToModel(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());;
        }
        return contract;
    }

    private static Contract sqlToModel(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int bidId = rs.getInt("bid_id");
        int providerId = rs.getInt("provider_id");
        int clientId = rs.getInt("client_id");
        String description = rs.getString("description");
        String acceptedByProvider = rs.getString("accepted_by_provider");
        String acceptedByClient = rs.getString("accepted_by_client");

        return new Contract(id, bidId, providerId, clientId, description, acceptedByProvider, acceptedByClient);
    }

    public static void updateStatus(Connection db, int contractId, boolean isProvider, boolean status) {
        String userPart = "accepted_by_client=?";
        if (isProvider) {
            userPart = "accepted_by_provider=?";
        }
        String query = "UPDATE contract SET " + userPart + " WHERE id=?";

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            String answer = Contract.ACCEPTED;
            if (!status) {
                answer = Contract.REJECTED;
            }
            st.setString(1, answer);
            st.setInt(2, contractId);
            st.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public String status() {
        if (accepted_by_client.equals(Contract.REJECTED) || accepted_by_provider.equals(Contract.REJECTED)) {
            return Contract.REJECTED;
        }
        if (accepted_by_client.equals("") && accepted_by_provider.equals("")) {
            return Contract.WAITING_FOR_BOTH;
        } else if (accepted_by_client.equals(Contract.ACCEPTED) && accepted_by_provider.equals(Contract.ACCEPTED)) {
            return Contract.ACCEPTED_BY_BOTH;
        } else if (accepted_by_client.equals(Contract.ACCEPTED)) {
            return Contract.WAITING_FOR_PROVIDER;
        } else {
            return Contract.WAITING_FOR_CLIENT;
        }
    }
}
