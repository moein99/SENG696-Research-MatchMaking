package src.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import src.db.Project;
import src.db.Utils;
import src.utils.Constants;

import java.sql.Connection;
import java.sql.SQLException;

public class ProjectAgent extends BaseAgent {
    public Connection db;

    @Override
    protected void setup() {
        super.setup();
        register(Constants.projectServiceName);
        System.out.println("Starting project agent, name: " + getName() + " local name: " + getLocalName());
        try {
            db = Utils.get_db_connection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        addBehaviour(new ProjectCreationBehaviour(this, 100));
        addBehaviour(new RetrieveProjectsBehaviour(this, 100));
    }
}

class ProjectCreationBehaviour extends TickerBehaviour {
    private ProjectAgent myAgent;

    public ProjectCreationBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ProjectAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.projectCreationConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            JSONObject dbResponse = Project.insert(myAgent.db, data);
            myAgent.sendMessage(
                    dbResponse.toString(),
                    Constants.projectCreationConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }
}

class RetrieveProjectsBehaviour extends TickerBehaviour {
    private ProjectAgent myAgent;

    public RetrieveProjectsBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ProjectAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.retrieveProjectsConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONArray dbResponse = Project.get_available(myAgent.db);
            myAgent.sendMessage(
                    dbResponse.toString(),
                    Constants.retrieveProjectsConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }
}

