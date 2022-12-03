package src.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class AgentCreator extends Agent {
    @Override
    protected void setup() {
        super.setup();
        createAgent("Profile_Agent", "src.agents.ProfileAgent");
        createAgent("Project_Agent", "src.agents.ProjectAgent");
        createAgent("Contract_Agent", "src.agents.ContractAgent");
        createAgent("Payment_Agent", "src.agents.PaymentAgent");
        createAgent("UI_Agent", "src.agents.UIAgent");
    }

    private void createAgent(String name, String className) {
        AID agentID = new AID(name, AID.ISLOCALNAME);
        AgentContainer controller = getContainerController();
        try {
            AgentController agent = controller.createNewAgent(name, className, null);
            agent.start();
            System.out.println("+++ Created: " + agentID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
