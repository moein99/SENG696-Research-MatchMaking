package src;

import src.db.User;
import src.db.Utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws SQLException {
        Connection db = Utils.get_db_connection();
        ArrayList<User> users = User.all(db);
        for (User user: users) {
            System.out.println(user);
        }
    }
}
