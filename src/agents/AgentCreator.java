package src.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.HashMap;
import java.util.Map;

public class AgentCreator extends Agent {
    public HashMap<String, String> agentToSrc;
    @Override
    protected void setup() {
        super.setup();
        agentToSrc = new HashMap<>();
        agentToSrc.put("Profile_Agent", "src.agents.ProfileAgent");
        agentToSrc.put("Project_Agent", "src.agents.ProjectAgent");
        agentToSrc.put("Contract_Agent", "src.agents.ContractAgent");
        agentToSrc.put("Payment_Agent", "src.agents.PaymentAgent");
        agentToSrc.put("UI_Agent", "src.agents.UIAgent");

        addBehaviour(new CreateAgentsBehaviour(this));
    }
}

class CreateAgentsBehaviour extends OneShotBehaviour {
    AgentCreator myAgent;
    public CreateAgentsBehaviour(AgentCreator myAgent) {
        super();
        this.myAgent = myAgent;
    }
    @Override
    public void action() {
        for (Map.Entry<String, String> set : myAgent.agentToSrc.entrySet()) {
            createAgent(set.getKey(), set.getValue());
        }
    }

    private void createAgent(String name, String className) {
        AID agentID = new AID(name, AID.ISLOCALNAME);
        AgentContainer controller = myAgent.getContainerController();
        try {
            AgentController agent = controller.createNewAgent(name, className, null);
            agent.start();
            System.out.println("+++ Created: " + agentID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
