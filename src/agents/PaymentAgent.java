package src.agents;

import jade.core.Agent;
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
        addBehaviour(new MakeProjectPaymentBehaviour(this, 100));
    }
}

class MakeProjectPaymentBehaviour extends TickerBehaviour {
    private PaymentAgent myAgent;

    public MakeProjectPaymentBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (PaymentAgent) a;
    }

    @Override
    protected void onTick() {
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
            User.addBalance(myAgent.db, project.assigneeId, amount);
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
