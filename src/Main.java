package src;

import org.json.JSONObject;
import src.UI.Launch;
import src.UI.Login;
import src.UI.StartGUI;
import src.db.*;

import java.security.Key;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws SQLException {
        Connection db = Utils.get_db_connection();
        Keyword.get_by_text(db, "Django");
//        ArrayList<User> users = User.all(db);
//        for (User user: users) {
//            System.out.println(user.id);
//        }
//        Project p = Project.get_by_id(db, 4);
//        Bid b = Bid.get_by_id(db, 1);
//        Contract c = Contract.get_by_id(db, 1);
//        Transaction t = Transaction.get_by_id(db, 1);
//        Message m = Message.get_by_id(db, 1);
//        Feedback f = Feedback.get_by_id(db, 1);
//        Keyword k = Keyword.get_by_id(db, 1);
//        UserKeyword uk = UserKeyword.get_by_id(db, 2, 1);
//        System.out.println();
//        new Launch();
//        new Login();
    }
}
