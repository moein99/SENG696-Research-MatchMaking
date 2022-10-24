package src.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Utils {
    public static Connection get_db_connection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/matchmaking";
        String username = "root";
        String password = "12345";
        return DriverManager.getConnection(url, username, password);
    }
}
