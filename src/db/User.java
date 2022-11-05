package src.db;

import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.security.MessageDigest;

public class User {
    public int id;
    public String username;
    public String encrypted_password;
    public String user_type;
    public String name;
    public String website;
    public String logo_address;
    public String resume_address;
    public int hourly_compensation;
    public boolean is_verified;
    public Date subscription_ends;
    public int balance;

    public final static String CLIENT_TYPE = "C";
    public final static String PROVIDER_TYPE = "P";

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
        Date subscription_ends,
        int balance
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
        this.balance = balance;
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
                int balance = rs.getInt("balance");
                items.add(new User(id, username, encrypted_password, user_type, name, website, logo_address, resume_address, hourly_compensation, is_verified, subscription_ends, balance));
            }
        }
        return items;
    }

    public static JSONObject signupClient(Connection db, JSONObject data) {
        long user_id = -1;
        JSONObject obj = new JSONObject();
        String username = data.getString("username");
        String password = data.getString("password");
        String query = "INSERT INTO user (username, encrypted_password, user_type) VALUES (?,?,?)";

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setString(1, username);
            st.setString(2, getHash(password));
            st.setString(3, CLIENT_TYPE);
            int rows = st.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        user_id = rs.getInt(1);
                        System.out.println(user_id);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate")) {
                obj.put("message", "The username already exists");
            }
        }
        obj.put("id", user_id);

        return obj;
    }

    public static JSONObject isCredentialsValid(Connection db, JSONObject data) {
        JSONObject obj = new JSONObject();
        int rows = 0;
        obj.put("status", false);

        String username = data.getString("username");
        String password = data.getString("password");
        String query = "SELECT * FROM user WHERE username=? AND encrypted_password=?";
        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            st.setString(1, username);
            st.setString(2, getHash(password));
            ResultSet rs = st.executeQuery();
            if (rs.last()) {
                rows = rs.getRow();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        if (rows != 0) {
            obj.put("status", true);
            return obj;
        }
        return obj;
    }

    public static String getHash(String input) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        messageDigest.update(input.getBytes());
        return new String(messageDigest.digest());
    }
}
