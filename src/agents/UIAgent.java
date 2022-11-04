package src.agents;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import src.UI.Launch;
import src.utils.Constants;
import org.json.JSONObject;

import java.io.StringWriter;


public class UIAgent extends BaseAgent {
    @Override
    protected void setup() {
        super.setup();
        register(Constants.UIServiceName);
        System.out.println("Starting UI agent, name: " + getName() + " local name: " + getLocalName());
        new Launch(this);
    }

    public JSONObject call_for_signup(String username, String password) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
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
}
