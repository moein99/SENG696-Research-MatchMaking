package src.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class Project {
    int id;
    int owner_id;
    String description;
    int assignee_id;
    int progress;
    Date deadline;
    String status;

    public Project(
            int id,
            int owner_id,
            String description,
            int assignee_id,
            int progress,
            Date deadline,
            String status
    ) {
        this.id = id;
        this.owner_id = owner_id;
        this.description = description;
        this.assignee_id = assignee_id;
        this.progress = progress;
        this.deadline = deadline;
        this.status = status;
    }

    public static Project get_by_id(Connection db, int identifier) throws SQLException {
        String query = "select * from project where id=" + identifier;
        try (Statement st = db.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("id");
                int owner_id = rs.getInt("owner_id");
                String description = rs.getString("description");
                int assignee_id = rs.getInt("assignee_id");
                int progress = rs.getInt("progress");
                Date deadline = rs.getTimestamp("deadline");
                String status = rs.getString("status");
                return new Project(id, owner_id, description, assignee_id, progress, deadline, status);
            }
        }
        return null;
    }
}
