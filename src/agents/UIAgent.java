package src.agents;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import src.UI.Launch;
import src.db.Project;
import src.db.User;
import src.db.Utils;
import src.utils.Constants;
import org.json.JSONObject;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;


public class UIAgent extends BaseAgent {
    public Connection db;
    @Override
    protected void setup() {
        super.setup();
        register(Constants.UIServiceName);
        try {
            db = Utils.get_db_connection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Starting UI agent, name: " + getName() + " local name: " + getLocalName());
        new Launch(this);
    }

    private ACLMessage call(
            JSONObject data,
            String conversationID,
            String serviceName,
            MessageTemplate receivingTemplate
    ) {
        StringWriter out = new StringWriter();
        data.write(out);
        this.sendMessage(
                out.toString(),
                conversationID,
                ACLMessage.REQUEST,
                searchForService(serviceName)
        );

        return blockingReceive(receivingTemplate);
    }

    private MessageTemplate getInformTemplate(String conversationId) {
        return MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(conversationId)
        );
    }

    public JSONObject call_for_client_signup(
            String username,
            String password
    ) {
        JSONObject data = new JSONObject();
        data.put("username", username);
        data.put("type", "C");
        data.put("password", password);

        ACLMessage response = call(data, Constants.signupConversationID, Constants.profileServiceName, getInformTemplate(Constants.signupConversationID));

        return new JSONObject(response.getContent());
    }

    public JSONObject call_for_provider_signup(
            String username,
            String password,
            String name,
            String website,
            String logoAddress,
            String resumeAddress,
            String hourlyCompensation,
            String rawKeywords
    ) {
        JSONObject data = new JSONObject();
        ArrayList<String> keywords = extractKeywords(rawKeywords);
        data.put("username", username);
        data.put("password", password);
        data.put("type", "P");
        data.put("name", name);
        data.put("website", website);
        data.put("logoAddress", logoAddress);
        data.put("resumeAddress", resumeAddress);
        data.put("hourlyCompensation", hourlyCompensation);
        data.put("keywords", keywords);

        ACLMessage response = call(data, Constants.signupConversationID, Constants.profileServiceName, getInformTemplate(Constants.signupConversationID));

        return new JSONObject(response.getContent());
    }

    public User call_for_login(String username, String password) {
        JSONObject data = new JSONObject();
        data.put("username", username);
        data.put("password", password);

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
                ),
                MessageTemplate.MatchConversationId(Constants.loginConversationID)
        );

        ACLMessage response = call(data, Constants.loginConversationID, Constants.profileServiceName, template);

        if (response.getPerformative() == ACLMessage.REFUSE) {
            return null;
        } else if (response.getPerformative() == ACLMessage.INFORM) {
            JSONObject userJson = new JSONObject(response.getContent());
            return User.JSONtoModel(userJson);
        }

        return null;
    }

    public JSONObject call_for_project_creation(int ownerId, String title, String description, int duration) {
        JSONObject data = new JSONObject();
        data.put("owner_id", ownerId);
        data.put("title", title);
        data.put("description", description);
        data.put("duration", duration);

        ACLMessage response = call(data, Constants.projectCreationConversationID, Constants.projectServiceName, getInformTemplate(Constants.projectCreationConversationID));
        return new JSONObject(response.getContent());
    }

    public JSONObject call_for_bid_creation(int projectId, int bidderId, String description, int amount) {
        JSONObject data = new JSONObject();
        data.put("project_id", projectId);
        data.put("bidder_id", bidderId);
        data.put("description", description);
        data.put("amount", amount);

        ACLMessage response = call(data, Constants.bidCreationConversationID, Constants.contractServiceName, getInformTemplate(Constants.bidCreationConversationID));
        return new JSONObject(response.getContent());
    }

    public ArrayList<Project> getProjects() {
        ACLMessage response = call(new JSONObject(), Constants.retrieveProjectsConversationID, Constants.projectServiceName, getInformTemplate(Constants.retrieveProjectsConversationID));
        JSONArray projectsJson = new JSONArray(response.getContent());
        ArrayList<Project> projects = new ArrayList<>();
        for (int i = 0; i < projectsJson.length(); i++) {
            projects.add(Project.JSONToModel(projectsJson.getJSONObject(i)));
        }
        return projects;
    }

    private ArrayList<String> extractKeywords(String rawKeywords) {
        return new ArrayList<>(Arrays.asList(rawKeywords.split(",")));
    }
}
