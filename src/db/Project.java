package src.db;

import org.json.JSONArray;
import org.json.JSONObject;
import src.utils.Utils;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static src.utils.Utils.convertStringToDate;

public class Project {
    public int id;
    public int owner_id;
    public String title;
    public String description;
    public int assigneeId;
    public int progress;
    public Date deadline;
    public String status;
    public int hoursWorked;

    public final static String CREATED = "C";
    public final static String ASSIGNED = "A";

    public Project(
            int id,
            int owner_id,
            String title,
            String description,
            int assigneeId,
            int progress,
            Date deadline,
            String status,
            int hoursWorked
    ) {
        this.id = id;
        this.owner_id = owner_id;
        this.title = title;
        this.description = description;
        this.assigneeId = assigneeId;
        this.progress = progress;
        this.deadline = deadline;
        this.status = status;
        this.hoursWorked = hoursWorked;
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
        String query = "SELECT * FROM project WHERE status=?";
        JSONArray results = new JSONArray();

        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            st.setString(1, Project.CREATED);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                results.put(sqlToJson(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return results;
    }

    public static ArrayList<Project> getUserProjects(Connection db, int userId, String status) {
        String query = "SELECT * FROM project WHERE (owner_id=? OR assignee_id=?) AND status=?";
        ArrayList<Project> results = new ArrayList<>();

        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            st.setInt(1, userId);
            st.setInt(2, userId);
            st.setString(3, status);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                results.add(sqlToModel(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return results;
    }

    public static Project get_with_id(Connection db, int id) {
        String query = "SELECT * FROM project WHERE id=?";

        Project project = null;
        int rows = 0;

        try (PreparedStatement st = db.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.last()) {
                rows = rs.getRow();
            }
            if (rows != 0) {
                rs.first();
                project = sqlToModel(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());;
        }

        return project;
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
        fields.put("hours_worked", rs.getInt("hours_worked"));
        return fields;
    }

    private static Project sqlToModel(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int ownerId = rs.getInt("owner_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        int assigneeId = rs.getInt("assignee_id");
        int progress = rs.getInt("progress");
        Date deadline = Utils.convertStringToDate(rs.getString("deadline"));
        String status = rs.getString("status");
        int hoursWorked = rs.getInt("hours_worked");

        return new Project(id, ownerId, title, description, assigneeId, progress, deadline, status, hoursWorked);
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
        int hoursWorked = data.getInt("hours_worked");

        return new Project(id, owner_id, title, description, assignee_id, progress, deadline, status, hoursWorked);
    }

    public static void assignUser(Connection db, int projectId, int assigneeId) {
        String query = "UPDATE project SET assignee_id=?, status=?  WHERE id=?";

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setInt(1, assigneeId);
            st.setString(2, Project.ASSIGNED);
            st.setInt(3, projectId);
            st.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void extend(Connection db, int projectId, int days) {
        Project project = Project.get_with_id(db, projectId);
        Calendar cal = Calendar.getInstance();
        cal.setTime(project.deadline);
        cal.add(Calendar.DAY_OF_MONTH, days);
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
        String query = "UPDATE project SET deadline=? WHERE id=?";

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setString(1, timeStamp);
            st.setInt(2, projectId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateProgress(Connection db, int projectId, int progress) {
        String query = "UPDATE project SET progress=? WHERE id=?";

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setInt(1, progress);
            st.setInt(2, projectId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateHours(Connection db, int projectId, int hours) {
        String query = "UPDATE project SET hours_worked=? WHERE id=?";

        try (PreparedStatement st = db.prepareStatement(query, new String[] { "id" })) {
            st.setInt(1, hours);
            st.setInt(2, projectId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject json() {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("owner_id", owner_id);
        obj.put("title", title);
        obj.put("description", description);
        obj.put("assignee_id", assigneeId);
        obj.put("progress", progress);
        obj.put("deadline", Utils.convertDateToString(deadline));
        obj.put("status", status);
        obj.put("hours_worked", hoursWorked);
        return obj;
    }
}
