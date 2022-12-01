package src.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONObject;
import src.db.Bid;
import src.db.Project;
import src.db.User;
import src.db.Utils;
import src.utils.Constants;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

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
        addBehaviour(new RetrieveOpenBidsBehaviour(this, 100));
        addBehaviour(new BidAcceptBehaviour(this, 100));
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

class RetrieveOpenBidsBehaviour extends TickerBehaviour {
    private ContractAgent myAgent;

    public RetrieveOpenBidsBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ContractAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.retrieveOpenBidsConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            int userId = data.getInt("user_id");
            JSONObject result = new JSONObject();
            ArrayList<Project> userProjects = Project.getUserProjects(myAgent.db, userId, Project.CREATED);
            ArrayList<Integer> projectIds = extractIds(userProjects);
            if (projectIds.size() != 0) {
                ArrayList<Bid> bids = Bid.getByProjectIds(myAgent.db, projectIds, Bid.SENT_TO_PROVIDER);
                if (bids.size() != 0) {
                    result = getBidsWithProjectName(userProjects, bids);
                }
            }
            myAgent.sendMessage(
                    result.toString(),
                    Constants.retrieveOpenBidsConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }

    private JSONObject getBidsWithProjectName(ArrayList<Project> projects, ArrayList<Bid> bids) {
        JSONObject result = new JSONObject();

        for (Bid bid: bids) {
            for (Project project: projects) {
                if (project.id != bid.project_id) {
                    continue;
                }
                HashMap<String, String> data = new HashMap<>();
                data.put("project_title", project.title);
                data.put("bid_amount", String.valueOf(bid.hourly_rate));
                data.put("bid_description", bid.description);
                data.put("bidder_username", User.get_with_id(myAgent.db, bid.bidder_id).username);
                result.put(String.valueOf(bid.id), data);
                break;
            }
        }

        return result;
    }

    private ArrayList<Integer> extractIds(ArrayList<Project> projects) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (Project p :
                projects) {
            ids.add(p.id);
        }

        return ids;
    }
}

class BidAcceptBehaviour extends TickerBehaviour {
    private ContractAgent myAgent;

    public BidAcceptBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ContractAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.bidAcceptConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            int bidId = data.getInt("bid_id");
            Bid acceptedBid = Bid.getById(myAgent.db, bidId);

            handleBidsStatusUponAcceptance(acceptedBid);
            handleProjectInitialization(acceptedBid);

            myAgent.sendMessage(
                    "",
                    Constants.bidAcceptConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }

    private void handleBidsStatusUponAcceptance(Bid acceptedBid) {
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(acceptedBid.id);
        Bid.update_status(myAgent.db, ids, Bid.ACCEPTED);
        ArrayList<Bid> allProjectBids = Bid.getByProjectId(myAgent.db, acceptedBid.project_id);
        ArrayList<Integer> bidIds = extractIds(allProjectBids);
        bidIds.remove((Integer) acceptedBid.id);
        Bid.update_status(myAgent.db, bidIds, Bid.REJECTED);
    }

    private void handleProjectInitialization(Bid acceptedBid) {
        JSONObject content = new JSONObject();
        content.put("assignee_id", acceptedBid.bidder_id);
        content.put("project_id", acceptedBid.project_id);

        myAgent.sendMessage(
                content.toString(),
                Constants.projectInitializationConversationID,
                ACLMessage.REQUEST,
                myAgent.searchForService(Constants.projectServiceName)
        );

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(Constants.projectInitializationConversationID)
        );
        myAgent.blockingReceive(template);
    }

    private ArrayList<Integer> extractIds(ArrayList<Bid> projects) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (Bid b :
                projects) {
            ids.add(b.id);
        }

        return ids;
    }
}
