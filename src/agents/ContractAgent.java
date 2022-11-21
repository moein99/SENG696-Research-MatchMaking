package src.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONObject;
import src.db.Bid;
import src.db.User;
import src.db.Utils;
import src.utils.Constants;

import java.sql.Connection;
import java.sql.SQLException;

public class ContractAgent extends BaseAgent {
    public Connection db;

    @Override
    protected void setup() {
        super.setup();
        register(Constants.contractServiceName);
        System.out.println("Starting Contract agent, name: " + getName() + " local name: " + getLocalName());
        try {
            db = Utils.get_db_connection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        addBehaviour(new BidCreationBehaviour(this, 100));
    }
}

class BidCreationBehaviour extends TickerBehaviour {
    private ContractAgent myAgent;

    public BidCreationBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ContractAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.bidCreationConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            JSONObject dbResponse = Bid.insert(myAgent.db, data);
            myAgent.sendMessage(
                    dbResponse.toString(),
                    Constants.bidCreationConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }
}

