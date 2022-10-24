package src.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class Keyword {
    public int id;
    public String text;

    public Keyword(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public static Keyword get_by_id(Connection db, int identifier) throws SQLException {
        String query = "select * from keyword where id=" + identifier;
        try (Statement st = db.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                return new Keyword(id, text);
            }
        }
        return null;
    }
}
