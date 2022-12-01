package src.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import src.db.Message;
import src.db.Project;
import src.db.Utils;
import src.utils.Constants;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

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
        addBehaviour(new ProjectInitializationBehaviour(this, 100));
        addBehaviour(new RetrieveActiveProjectsBehaviour(this, 100));
        addBehaviour(new MessageCreationBehaviour(this, 100));
        addBehaviour(new RetrieveMessagesBehaviour(this, 100));
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

class RetrieveActiveProjectsBehaviour extends TickerBehaviour {
    private ProjectAgent myAgent;

    public RetrieveActiveProjectsBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ProjectAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.retrieveActiveProjectsConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            int userId = data.getInt("user_id");
            ArrayList<Project> activeProjects = Project.getUserProjects(myAgent.db, userId, Project.ASSIGNED);
            JSONArray response = new JSONArray();
            for (Project project: activeProjects) {
                response.put(project.json());
            }
            myAgent.sendMessage(
                    response.toString(),
                    Constants.retrieveActiveProjectsConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }
}

class ProjectInitializationBehaviour extends TickerBehaviour {
    private ProjectAgent myAgent;

    public ProjectInitializationBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ProjectAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.projectInitializationConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject content = new JSONObject(message.getContent());
            int projectId = content.getInt("project_id");
            int assigneeId = content.getInt("assignee_id");
            Project.assignUser(myAgent.db, projectId, assigneeId);
            myAgent.sendMessage(
                    "",
                    Constants.projectInitializationConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.contractServiceName)
            );
        }
    }
}

class MessageCreationBehaviour extends TickerBehaviour {
    private ProjectAgent myAgent;

    public MessageCreationBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ProjectAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.messageCreationConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            Message.insert(myAgent.db, data);
            myAgent.sendMessage(
                    "",
                    Constants.messageCreationConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }
}

class RetrieveMessagesBehaviour extends TickerBehaviour {
    private ProjectAgent myAgent;

    public RetrieveMessagesBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ProjectAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.retrieveMessagesConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            int projectId = data.getInt("project_id");
            JSONArray messages = Message.get_by_projectId(myAgent.db, projectId);
            myAgent.sendMessage(
                    messages.toString(),
                    Constants.retrieveMessagesConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }
}


