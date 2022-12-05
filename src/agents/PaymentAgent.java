package src.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONObject;
import src.db.Project;
import src.db.Transaction;
import src.db.User;
import src.db.Utils;
import src.utils.Constants;

import java.sql.Connection;
import java.sql.SQLException;

public class PaymentAgent extends BaseAgent {
    public Connection db;

    @Override
    protected void setup() {
        super.setup();
        register(Constants.paymentServiceName);
        System.out.println("Starting Profile agent, name: " + getName() + " local name: " + getLocalName());
        try {
            db = Utils.get_db_connection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        addBehaviour(new MakeProjectPaymentBehaviour(this));
        addBehaviour(new PurchaseSubscriptionBehaviour(this));
    }
}

class MakeProjectPaymentBehaviour extends CyclicBehaviour {
    private PaymentAgent myAgent;
    final static double SYSTEM_RATE = 0.7;

    public MakeProjectPaymentBehaviour(Agent a) {
        super(a);
        myAgent = (PaymentAgent) a;
    }

    @Override
    public void action() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.makeProjectPaymentConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            int projectId = data.getInt("project_id");
            Project project = Project.get_with_id(myAgent.db, projectId);
            User projectOwner = User.get_with_id(myAgent.db, project.ownerId);
            int amount = project.hoursWorked * projectOwner.hourly_compensation;
            String transactionDescription = "received " + amount + "$ for '" + project.title + "' project.";

            Transaction.insert(myAgent.db, project.ownerId, project.assigneeId, transactionDescription, amount);
            User.addBalance(myAgent.db, project.assigneeId, (int)(amount * SYSTEM_RATE));
            User.addBalance(myAgent.db, project.ownerId, -amount);

            myAgent.sendMessage(
                    "",
                    Constants.makeProjectPaymentConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.projectServiceName)
            );
        }
    }
}

class PurchaseSubscriptionBehaviour extends CyclicBehaviour {
    private PaymentAgent myAgent;

    public PurchaseSubscriptionBehaviour(Agent a) {
        super(a);
        myAgent = (PaymentAgent) a;
    }

    @Override
    public void action() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.purchaseSubscriptionConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            int userId = data.getInt("user_id");
            int premiumPrice = data.getInt("premium_price");
            User.activatePremium(myAgent.db, userId, premiumPrice);

            myAgent.sendMessage(
                    "",
                    Constants.purchaseSubscriptionConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }
}
