package src.agents;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import src.UI.Home;
import src.db.Project;
import src.db.User;
import src.db.Utils;
import src.utils.Constants;
import org.json.JSONObject;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
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
//        new Launch(this);
        new Home(this, User.get_with_id(db, 42));
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

    public JSONObject callForClientSignup(
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

    public JSONObject callForProviderSignup(
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

    public User callForLogin(String username, String password) {
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

    public JSONObject callForProjectCreation(int ownerId, String title, String description, int duration) {
        JSONObject data = new JSONObject();
        data.put("owner_id", ownerId);
        data.put("title", title);
        data.put("description", description);
        data.put("duration", duration);

        ACLMessage response = call(data, Constants.projectCreationConversationID, Constants.projectServiceName, getInformTemplate(Constants.projectCreationConversationID));
        return new JSONObject(response.getContent());
    }

    public JSONObject callForBidCreation(int projectId, int bidderId, String description, int amount) {
        JSONObject data = new JSONObject();
        data.put("project_id", projectId);
        data.put("bidder_id", bidderId);
        data.put("description", description);
        data.put("amount", amount);

        ACLMessage response = call(data, Constants.bidCreationConversationID, Constants.contractServiceName, getInformTemplate(Constants.bidCreationConversationID));
        return new JSONObject(response.getContent());
    }

    public void callForBidAccept(int bidId) {
        JSONObject data = new JSONObject();
        data.put("bid_id", bidId);

        call(data, Constants.bidAcceptConversationID, Constants.contractServiceName, getInformTemplate(Constants.bidAcceptConversationID));
    }

    public ArrayList<Project> retrieveProjects(int min, int max, int projectsDone, String keywords) {
        JSONObject data = new JSONObject();
        data.put("min", min);
        data.put("max", max);
        data.put("projects_done", projectsDone);
        data.put("keywords", keywords);
        ACLMessage response = call(data, Constants.retrieveProjectsConversationID, Constants.projectServiceName, getInformTemplate(Constants.retrieveProjectsConversationID));
        JSONArray projectsJson = new JSONArray(response.getContent());
        ArrayList<Project> projects = new ArrayList<>();
        for (int i = 0; i < projectsJson.length(); i++) {
            projects.add(Project.JSONToModel(projectsJson.getJSONObject(i)));
        }
        return projects;
    }

    public JSONObject getProviderOpenBids(int userId) {
        JSONObject data = new JSONObject();
        data.put("user_id", userId);
        ACLMessage response = call(data, Constants.retrieveOpenBidsConversationID, Constants.contractServiceName, getInformTemplate(Constants.retrieveOpenBidsConversationID));

        return new JSONObject(response.getContent());
    }

    private ArrayList<String> extractKeywords(String rawKeywords) {
        return new ArrayList<>(Arrays.asList(rawKeywords.split(",")));
    }

    public JSONArray getUserProjectsList(User user) {
        JSONObject data = new JSONObject();
        data.put("user_id", user.id);
        ACLMessage response = call(data, Constants.retrieveProjectsListConversationID, Constants.projectServiceName, getInformTemplate(Constants.retrieveProjectsListConversationID));
        return new JSONArray(response.getContent());
    }

    public void callForMessage(String message, int senderId, int receiverId, int projectId) {
        JSONObject data = new JSONObject();
        data.put("text", message);
        data.put("sender_id", String.valueOf(senderId));
        data.put("receiver_id", String.valueOf(receiverId));
        data.put("project_id", String.valueOf(projectId));
        call(data, Constants.messageCreationConversationID, Constants.projectServiceName, getInformTemplate(Constants.messageCreationConversationID));
    }

    public JSONArray retrieveChat(int projectId) {
        JSONObject data = new JSONObject();
        data.put("project_id", projectId);
        ACLMessage response = call(data, Constants.retrieveMessagesConversationID, Constants.projectServiceName, getInformTemplate(Constants.retrieveMessagesConversationID));
        return new JSONArray(response.getContent());
    }

    public void callForProjectExtension(int extendAmount, int id) {
        JSONObject data = new JSONObject();
        data.put("amount", String.valueOf(extendAmount));
        data.put("project_id", String.valueOf(id));
        call(data, Constants.extendProjectConversationID, Constants.projectServiceName, getInformTemplate(Constants.extendProjectConversationID));
    }

    public void callForProgressUpdate(int progressAmount, int id) {
        JSONObject data = new JSONObject();
        data.put("amount", String.valueOf(progressAmount));
        data.put("project_id", String.valueOf(id));
        call(data, Constants.updateProgressConversationID, Constants.projectServiceName, getInformTemplate(Constants.updateProgressConversationID));
    }

    public void callForHoursUpdate(int hoursAmount, int id, int hoursWorked) {
        JSONObject data = new JSONObject();
        data.put("amount", String.valueOf(hoursAmount));
        data.put("project_id", String.valueOf(id));
        data.put("amount_so_far", hoursWorked);
        call(data, Constants.hoursUpdateConversationID, Constants.projectServiceName, getInformTemplate(Constants.hoursUpdateConversationID));
    }

    public void callForProjectEnding(int id) {
        JSONObject data = new JSONObject();
        data.put("project_id", id);
        call(data, Constants.endProjectConversationID, Constants.projectServiceName, getInformTemplate(Constants.endProjectConversationID));
    }

    public void callForFeedbackCreation(JSONObject project, String comment, int rate, User user) {
        JSONObject data = new JSONObject();
        int senderId, receiverId;
        senderId = user.id;
        if (senderId == project.getInt("owner_id")) {
            receiverId = project.getInt("assignee_id");
        } else {
            receiverId = project.getInt("owner_id");
        }
        data.put("project_id", project.getInt("id"));
        data.put("sender_id", senderId);
        data.put("receiver_id", receiverId);
        data.put("comment", comment);
        data.put("rate", rate);

        call(data, Constants.feedbackCreationConversationID, Constants.profileServiceName, getInformTemplate(Constants.feedbackCreationConversationID));
    }

    public JSONArray retrieveUserFeedbacks(int userId) {
        JSONObject data = new JSONObject();
        data.put("user_id", userId);
        ACLMessage response = call(data, Constants.retrieveFeedbacksConversationID, Constants.profileServiceName, getInformTemplate(Constants.retrieveFeedbacksConversationID));
        return new JSONArray(response.getContent());
    }
}
