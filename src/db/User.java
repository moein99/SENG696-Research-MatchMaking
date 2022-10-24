package src.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

public class User {
    int id;
    String username;
    String encrypted_password;
    String user_type;
    String name;
    String website;
    String logo_address;
    String resume_address;
    int hourly_compensation;
    boolean is_verified;
    Date subscription_ends;

    public User(
            int id,
            String username,
            String encrypted_password,
            String user_type,
            String name,
            String website,
            String logo_address,
            String resume_address,
            int hourly_compensation,
            boolean is_verified,
            Date subscription_ends
    ) {
        this.id = id;
        this.username = username;
        this.encrypted_password = encrypted_password;
        this.user_type = user_type;
        this.name = name;
        this.website = website;
        this.logo_address = logo_address;
        this.resume_address = resume_address;
        this.hourly_compensation = hourly_compensation;
        this.is_verified = is_verified;
        this.subscription_ends = subscription_ends;
    }

    public static ArrayList<User> all(Connection db) throws SQLException {
        ArrayList<User> items = new ArrayList<>();
        String query = "select * from user";
        try (Statement st = db.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String encrypted_password = rs.getString("encrypted_password");
                String user_type = rs.getString("user_type");
                String name = rs.getString("name");
                String website = rs.getString("website");
                String logo_address = rs.getString("logo_address");
                String resume_address = rs.getString("resume_address");
                int hourly_compensation = rs.getInt("hourly_compensation");
                boolean is_verified = rs.getBoolean("is_verified");
                Date subscription_ends = rs.getTimestamp("subscription_ends");
                items.add(new User(id, username, encrypted_password, user_type, name, website, logo_address, resume_address, hourly_compensation, is_verified, subscription_ends));
            }
        }
        return items;
    }
}
