package src.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import src.db.Feedback;
import src.db.User;
import src.db.Utils;
import src.utils.Constants;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class ProfileAgent extends BaseAgent {
    public Connection db;

    @Override
    protected void setup() {
        super.setup();
        register(Constants.profileServiceName);
        System.out.println("Starting Profile agent, name: " + getName() + " local name: " + getLocalName());
        try {
            db = Utils.get_db_connection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        addBehaviour(new SignupBehaviour(this, 100));
        addBehaviour(new LoginBehaviour(this, 100));
        addBehaviour(new FeedbackCreationBehaviour(this, 100));
        addBehaviour(new RetrieveFeedbacksBehaviour(this, 100));
    }
}

class SignupBehaviour extends TickerBehaviour {
    private ProfileAgent myAgent;

    public SignupBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ProfileAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.MatchConversationId(Constants.signupConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            JSONObject dbResponse = new JSONObject();
            if (data.getString("type").equals("C")) {
                dbResponse = User.signupClient(myAgent.db, data);
            } else if (data.getString("type").equals("P")) {
                dbResponse = User.signupProvider(myAgent.db, data);
            }
            myAgent.sendMessage(
                    dbResponse.toString(),
                    Constants.signupConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }
}

class LoginBehaviour extends TickerBehaviour {
    private ProfileAgent myAgent;

    public LoginBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ProfileAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.loginConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            JSONObject response = User.login(myAgent.db, data);
            String content = "";
            int type = ACLMessage.REFUSE;

            if (response != null) {
                content = response.toString();
                type = ACLMessage.INFORM;
            }
            myAgent.sendMessage(
                    content,
                    Constants.loginConversationID,
                    type,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }
}

class FeedbackCreationBehaviour extends TickerBehaviour {
    private ProfileAgent myAgent;

    public FeedbackCreationBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ProfileAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.feedbackCreationConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            Feedback.insert(myAgent.db, data);
            myAgent.sendMessage(
                    "",
                    Constants.feedbackCreationConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }
}

class RetrieveFeedbacksBehaviour extends TickerBehaviour {
    private ProfileAgent myAgent;

    public RetrieveFeedbacksBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ProfileAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.retrieveFeedbacksConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            int userId = data.getInt("user_id");
            ArrayList<Feedback> feedbacks = Feedback.getUserFeedbacks(myAgent.db, userId, false);
            JSONArray results = new JSONArray();
            for (Feedback feedback : feedbacks) {
                results.put(feedback.json());
            }
            myAgent.sendMessage(
                    results.toString(),
                    Constants.retrieveFeedbacksConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }
}
