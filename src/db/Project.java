package src.db;

import org.json.Cookie;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static src.utils.Utils.convertStringToDate;

public class Project {
    public int id;
    public int owner_id;
    public String title;
    public String description;
    public int assignee_id;
    public int progress;
    public Date deadline;
    public String status;

    public Project(
            int id,
            int owner_id,
            String title,
            String description,
            int assignee_id,
            int progress,
            Date deadline,
            String status
    ) {
        this.id = id;
        this.owner_id = owner_id;
        this.title = title;
        this.description = description;
        this.assignee_id = assignee_id;
        this.progress = progress;
        this.deadline = deadline;
        this.status = status;
    }

    public static JSONObject insert(Connection db, JSONObject data) {
        int ownerId = data.getInt("owner_id");
        String title = data.getString("title");
        String description = data.getString("description");
        int durationDays = data.getInt("duration");
        String query = "INSERT INTO project (owner_id, title, description, deadline) values (?,?,?,?)";

        Calendar currentTime = Calendar.getInstance();
        currentTime.add(Calendar.DAY_OF_MONTH, durationDays);
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentTime.getTime());

        long projectId = -1;
        JSONObject obj = new JSONObject();

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setInt(1, ownerId);
            st.setString(2, title);
            st.setString(3, description);
            st.setString(4, timeStamp);
            int rows = st.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        projectId = rs.getInt(1);
                        System.out.println("project inserted, id: " + projectId);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        obj.put("id", projectId);
        return obj;
    }

    public static JSONArray get_available(Connection db) {
        String query = "SELECT * FROM project WHERE status='C'";
        JSONArray results = new JSONArray();

        int rows = 0;

        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                results.put(sqlToJson(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());;
        }
        return results;
    }

    private static JSONObject sqlToJson(ResultSet rs) throws SQLException {
        JSONObject fields = new JSONObject();
        fields.put("id", rs.getInt("id"));
        fields.put("owner_id", rs.getInt("owner_id"));
        fields.put("title", rs.getString("title"));
        fields.put("description", rs.getString("description"));
        fields.put("assignee_id", rs.getInt("assignee_id"));
        fields.put("progress", rs.getInt("progress"));
        fields.put("deadline", rs.getString("deadline"));
        fields.put("status", rs.getString("status"));
        return fields;
    }

    public static Project JSONToModel(JSONObject data) {
        int id = data.getInt("id");
        int owner_id = data.getInt("owner_id");
        String title = data.getString("title");
        String description = data.getString("description");
        int assignee_id = data.getInt("assignee_id");
        int progress = data.getInt("progress");
        Date deadline = convertStringToDate(data.getString("deadline"));
        String status = data.getString("status");

        return new Project(id, owner_id, title, description, assignee_id, progress, deadline, status);
    }
}
