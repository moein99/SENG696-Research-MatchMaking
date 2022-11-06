package src.agents;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import src.UI.Launch;
import src.db.User;
import src.utils.Constants;
import org.json.JSONObject;

import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;


public class UIAgent extends BaseAgent {
    @Override
    protected void setup() {
        super.setup();
        register(Constants.UIServiceName);
        System.out.println("Starting UI agent, name: " + getName() + " local name: " + getLocalName());
        new Launch(this);
    }

    public JSONObject call_for_client_signup(String username, String password) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("type", "C");
        jsonObject.put("password", password);
        StringWriter out = new StringWriter();
        jsonObject.write(out);

        this.sendMessage(
            out.toString(),
            Constants.signupConversationID,
            ACLMessage.REQUEST,
            searchForService(Constants.profileServiceName)
        );

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(Constants.signupConversationID)
        );

        ACLMessage message = blockingReceive(template);
        if (message != null) {
            jsonObject = new JSONObject(message.getContent());
            jsonObject.put("status", true);
            return jsonObject;
        } else {
            jsonObject = new JSONObject();
            jsonObject.put("status", false);
            jsonObject.put("message", Constants.noResponseMessage);
            return jsonObject;
        }

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

        StringWriter out = new StringWriter();
        data.write(out);

        this.sendMessage(
                out.toString(),
                Constants.signupConversationID,
                ACLMessage.REQUEST,
                searchForService(Constants.profileServiceName)
        );

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(Constants.signupConversationID)
        );

        ACLMessage message = blockingReceive(template);
        JSONObject response;
        if (message != null) {
            response = new JSONObject(message.getContent());
            response.put("status", true);
            return response;
        } else {
            response = new JSONObject();
            response.put("status", false);
            response.put("message", Constants.noResponseMessage);
            return response;
        }
    }

    public User call_for_login(String username, String password) throws ParseException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("password", password);
        StringWriter out = new StringWriter();
        jsonObject.write(out);

        this.sendMessage(
                out.toString(),
                Constants.loginConversationID,
                ACLMessage.REQUEST,
                searchForService(Constants.profileServiceName)
        );

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
                ),
                MessageTemplate.MatchConversationId(Constants.loginConversationID)
        );

        ACLMessage message = blockingReceive(template);
        if (message != null) {
            if (message.getPerformative() == ACLMessage.REFUSE) {
                return null;
            } else if (message.getPerformative() == ACLMessage.INFORM) {
                JSONObject response = new JSONObject(message.getContent());
                return User.JSONtoModel(response);
            }
        }

        return null;
    }

    private ArrayList<String> extractKeywords(String rawKeywords) {
        return new ArrayList<>(Arrays.asList(rawKeywords.split(",")));
    }
}
