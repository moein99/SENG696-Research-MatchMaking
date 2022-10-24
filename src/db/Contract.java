package src.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Contract {
    public int id;
    public int provider_id;
    public int client_id;
    public String description;
    public boolean accepted_by_provider;
    public boolean accepted_by_client;

    public Contract(int id, int provider_id, int client_id, String description, boolean accepted_by_provider, boolean accepted_by_client) {
        this.id = id;
        this.provider_id = provider_id;
        this.client_id = client_id;
        this.description = description;
        this.accepted_by_provider = accepted_by_provider;
        this.accepted_by_client = accepted_by_client;
    }

    public static Contract get_by_id(Connection db, int identifier) throws SQLException {
        String query = "select * from contract where id=" + identifier;
        try (Statement st = db.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("id");
                int provider_id = rs.getInt("provider_id");
                int client_id = rs.getInt("client_id");
                String description = rs.getString("description");
                boolean accepted_by_provider = rs.getBoolean("accepted_by_provider");
                boolean accepted_by_client = rs.getBoolean("accepted_by_client");
                return new Contract(id, provider_id, client_id, description, accepted_by_provider, accepted_by_client);
            }
        }
        return null;
    }
}
