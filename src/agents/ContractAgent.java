package src.agents;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONObject;
import src.db.*;
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
        addBehaviour(new BidCreationBehaviour(this));
        addBehaviour(new RetrieveActiveBidsBehaviour(this, 100));
        addBehaviour(new BidAcceptBehaviour(this, 100));
        addBehaviour(new ContractUpdateBehaviour(this, 100));
    }
}

class BidCreationBehaviour extends SimpleBehaviour {
    private ContractAgent myAgent;

    public BidCreationBehaviour(Agent a) {
        super(a);
        myAgent = (ContractAgent) a;
    }

    @Override
    public void action() {
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

    @Override
    public boolean done() {
        return false;
    }
}

class RetrieveActiveBidsBehaviour extends TickerBehaviour {
    private ContractAgent myAgent;

    public RetrieveActiveBidsBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ContractAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.retrieveActiveBidsConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            User user = User.get_with_id(myAgent.db, data.getInt("user_id"));
            JSONObject merged;
            if (user.user_type.equals(User.PROVIDER_TYPE)) {
                merged = getProviderBids(data);
            } else {
                merged = getClientBids(data);
            }

            myAgent.sendMessage(
                    merged.toString(),
                    Constants.retrieveActiveBidsConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }

    private JSONObject getClientBids(JSONObject data) {
        int userId = data.getInt("user_id");
        ArrayList<Bid> bids = Bid.getClientBids(myAgent.db, userId);
        JSONObject result = new JSONObject();

        for (Bid bid: bids) {
            HashMap<String, String> map = new HashMap<>();
            String contractStatus = bid.getContractStatus(myAgent.db);
            if (contractStatus.equals(Contract.REJECTED) || contractStatus.equals(Contract.ACCEPTED_BY_BOTH)) {
                continue;
            }
            map.put("project_title", Project.get_with_id(myAgent.db, bid.project_id).title);
            map.put("bid_amount", String.valueOf(bid.hourly_rate));
            map.put("bid_description", bid.description);
            map.put("bidder_username", User.get_with_id(myAgent.db, bid.bidder_id).username);
            map.put("bidder_id", String.valueOf(bid.bidder_id));
            map.put("contract_status", bid.getContractStatus(myAgent.db));
            result.put(String.valueOf(bid.id), map);
        }

        return result;
    }

    private JSONObject getProviderBids(JSONObject data) {
        int userId = data.getInt("user_id");
        ArrayList<Project> userProjects = Project.getUserProjects(myAgent.db, userId, Project.CREATED);
        ArrayList<Integer> projectIds = extractIds(userProjects);
        JSONObject sentToProviderBids = getSentToProviderBids(projectIds, userProjects);
        JSONObject acceptedBids = getAcceptedBids(projectIds, userProjects);
        JSONObject merged = new JSONObject();
        if (sentToProviderBids.length() != 0) {
            merged = new JSONObject(sentToProviderBids, JSONObject.getNames(sentToProviderBids));
        }
        if (acceptedBids.length() != 0) {
            for (String key : JSONObject.getNames(acceptedBids)) {
                merged.put(key, acceptedBids.get(key));
            }
        }
        return merged;
    }

    private JSONObject getSentToProviderBids(ArrayList<Integer> projectIds, ArrayList<Project> userProjects) {
        JSONObject result = new JSONObject();
        if (projectIds.size() != 0) {
            ArrayList<Bid> bids = Bid.getByProjectIds(myAgent.db, projectIds, Bid.SENT_TO_PROVIDER);
            if (bids.size() != 0) {
                result = getBidsWithProjectName(userProjects, bids);
            }
        }
        return result;
    }

    private JSONObject getAcceptedBids(ArrayList<Integer> projectIds, ArrayList<Project> userProjects) {
        JSONObject result = new JSONObject();
        if (projectIds.size() != 0) {
            ArrayList<Bid> bids = Bid.getByProjectIds(myAgent.db, projectIds, Bid.ACCEPTED);
            if (bids.size() != 0) {
                ArrayList<Bid> toBeRemoved = new ArrayList<>();
                for (Bid bid : bids) {
                    String contractStatus = bid.getContractStatus(myAgent.db);
                    boolean first = contractStatus.equals(Contract.ACCEPTED_BY_BOTH);
                    boolean second = contractStatus.equals(Contract.REJECTED);
                    if (first || second) {
                        toBeRemoved.add(bid);
                    }
                }
                bids.removeAll(toBeRemoved);
                if (bids.size() != 0) {
                    return getBidsWithProjectName(userProjects, bids);
                }
            }
        }
        return result;
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
                data.put("bidder_id", String.valueOf(bid.bidder_id));
                data.put("contract_status", bid.getContractStatus(myAgent.db));
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
            Contract.insert(myAgent.db, bidId);

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
        if (bidIds.size() != 0) {
            Bid.update_status(myAgent.db, bidIds, Bid.REJECTED);
        }
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

class ContractUpdateBehaviour extends TickerBehaviour {
    private ContractAgent myAgent;

    public ContractUpdateBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ContractAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.contractUpdateConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            int bidId = data.getInt("bid_id");
            int userId = data.getInt("user_id");
            boolean status = data.getBoolean("status");

            Contract contract = Contract.getWithBidId(myAgent.db, bidId);
            boolean isProvider = userId == contract.provider_id;
            Contract.updateStatus(myAgent.db, contract.id, isProvider, status);

            contract = Contract.getWithBidId(myAgent.db, bidId);
            if (contract.accepted_by_provider.equals(Contract.ACCEPTED) && contract.accepted_by_client.equals(Contract.ACCEPTED)) {
                handleProjectInitialization(Bid.getById(myAgent.db, bidId));
            }

            myAgent.sendMessage(
                    "",
                    Constants.contractUpdateConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
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
}
