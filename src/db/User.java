package src.db;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.security.MessageDigest;

import static src.utils.Utils.convertStringToDate;

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

    public static User get_with_id(Connection db, int id) {
        String query = "SELECT * FROM user WHERE id=?";
        System.out.println(id);

        User user = null;
        int rows = 0;

        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.last()) {
                rows = rs.getRow();
            }
            if (rows != 0) {
                rs.first();
                user = sqlToModel(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());;
        }
        return user;
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
                        System.out.println("user inserted, id: " + user_id);
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

    public static JSONObject signupProvider(Connection db, JSONObject data) {
        long userId = -1;
        JSONObject obj = new JSONObject();
        String username = data.getString("username");
        String password = data.getString("password");
        String nameField = data.getString("name");
        String website = data.getString("website");
        String logoAddress = data.getString("logoAddress");
        String resumeAddress = data.getString("resumeAddress");
        String hourlyCompensation = data.getString("hourlyCompensation");
        ArrayList<String> keywords = convertJsonArrayToArrayList(data.getJSONArray("keywords"));
        String query = "INSERT INTO user (username, encrypted_password, user_type, name, website, logo_address, resume_address, hourly_compensation, is_verified) VALUES (?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setString(1, username);
            st.setString(2, getHash(password));
            st.setString(3, PROVIDER_TYPE);
            st.setString(4, nameField);
            st.setString(5, website);
            st.setString(6, logoAddress);
            st.setString(7, resumeAddress);
            st.setString(8, hourlyCompensation);
            st.setBoolean(9, false);
            int rows = st.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                        System.out.println("user inserted, id: " + userId);
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
        obj.put("id", userId);
        setProviderKeywords(db, keywords, userId);

        return obj;
    }

    private static void setProviderKeywords(Connection db, ArrayList<String> keywords, long userId) {
        for (String keywordText : keywords) {
            Keyword keyword = Keyword.get_by_text(db, keywordText);
            if (keyword == null) {
                Keyword.insert(db, keywordText);
                keyword = Keyword.get_by_text(db, keywordText);
            }
            UserKeyword.insert(db, String.valueOf(userId), String.valueOf(keyword.id));
        }
    }

    public static JSONObject login(Connection db, JSONObject data) {
        JSONObject userFields = null;

        int rows = 0;
        ResultSet rs;

        String username = data.getString("username");
        String password = data.getString("password");
        String query = "SELECT * FROM user WHERE username=? AND encrypted_password=?";
        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            st.setString(1, username);
            st.setString(2, getHash(password));
            rs = st.executeQuery();
            if (rs.last()) {
                rows = rs.getRow();
            }
            if (rows != 0) {
                rs.first();
                userFields = sqlToJSON(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return userFields;
    }

    private static JSONObject sqlToJSON(ResultSet rs) throws SQLException {
        JSONObject fields = new JSONObject();
        fields.put("id", rs.getInt("id"));
        fields.put("username", rs.getString("username"));
        fields.put("encrypted_password", rs.getString("encrypted_password"));
        fields.put("user_type", rs.getString("user_type"));
        fields.put("name", rs.getString("name"));
        fields.put("website", rs.getString("website"));
        fields.put("logo_address", rs.getString("logo_address"));
        fields.put("resume_address", rs.getString("resume_address"));
        fields.put("hourly_compensation", rs.getInt("hourly_compensation"));
        fields.put("is_verified", rs.getBoolean("is_verified"));
        fields.put("subscription_ends", rs.getString("subscription_ends"));
        fields.put("balance", rs.getInt("balance"));
        return fields;
    }

    private static User sqlToModel(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String encryptedPassword = rs.getString("encrypted_password");
        String userType = rs.getString("user_type");
        String name = rs.getString("name");
        String website = rs.getString("website");
        String logoAddress = rs.getString("logo_address");
        String resumeAddress = rs.getString("resume_address");
        int hourlyCompensation = rs.getInt("hourly_compensation");
        boolean isVerified = rs.getBoolean("is_verified");
        Date subscriptionEnds = convertStringToDate(rs.getString("subscription_ends"));
        int balance = rs.getInt("balance");

        return new User(id, username, encryptedPassword, userType, name, website, logoAddress, resumeAddress, hourlyCompensation, isVerified, subscriptionEnds, balance);
    }

    public static User JSONtoModel(JSONObject obj) {
        int id = obj.getInt("id");
        String username = obj.getString("username");
        String encryptedPassword = obj.getString("encrypted_password");
        String userType = obj.getString("user_type");
        String name = obj.has("name") ? obj.getString("name") : null;
        String website = obj.has("website") ? obj.getString("name") : null;
        String logoAddress = obj.has("logo_address") ? obj.getString("name") : null;
        String resumeAddress = obj.has("resume_address") ? obj.getString("name") : null;
        int hourlyCompensation = obj.getInt("hourly_compensation");
        boolean isVerified = obj.getBoolean("is_verified");
        Date subscriptionEnds = convertStringToDate(obj.has("subscription_ends") ? obj.getString("name") : null);
        int balance = obj.getInt("balance");

        return new User(id, username, encryptedPassword, userType, name, website, logoAddress, resumeAddress, hourlyCompensation, isVerified, subscriptionEnds, balance);
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

    private static ArrayList<String> convertJsonArrayToArrayList(JSONArray array) {
        ArrayList<String> data = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            data.add(array.getString(i));
        }
        return data;
    }

    public static void addBalance(Connection db, int assigneeId, int amount) {
        String query = "UPDATE user SET balance=balance + ? WHERE id=?";

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setInt(1, amount);
            st.setInt(2, assigneeId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
