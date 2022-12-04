package src.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import src.db.*;
import src.utils.Constants;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

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
        addBehaviour(new ExtendProjectBehaviour(this, 100));
        addBehaviour(new UpdateProgressBehaviour(this, 100));
        addBehaviour(new HoursUpdateBehaviour(this, 100));
        addBehaviour(new EndProjectBehaviour(this, 100));
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
            JSONObject data = new JSONObject(message.getContent());
            int min = data.getInt("min");
            int max = data.getInt("max");
            int projectsDone = data.getInt("projects_done");
            ArrayList<String> keywords = extractKeywords(data.getString("keywords"));
            ArrayList<Integer> filteredProviders = getFilteredProviders(min, max, projectsDone, keywords);
            JSONArray dbResponse = new JSONArray();
            if (filteredProviders.size() != 0) {
                dbResponse = Project.get_available(myAgent.db, filteredProviders);
            }
            myAgent.sendMessage(
                    dbResponse.toString(),
                    Constants.retrieveProjectsConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }

    private ArrayList<Integer> getFilteredProviders(int min, int max, int projectsDone, ArrayList<String> keywords) {
        ArrayList<Integer> projectsDoneIds = null;
        if (projectsDone > 0) {
            projectsDoneIds = new ArrayList<>();
            ArrayList<Project> finishedProjects = Project.getWithStatus(myAgent.db, Project.FINISHED);
            HashMap<Integer, Integer> ownerToFinishedProjects = new HashMap<>();
            for (Project project : finishedProjects) {
                if (ownerToFinishedProjects.containsKey(project.ownerId)) {
                    ownerToFinishedProjects.put(project.ownerId, ownerToFinishedProjects.get(project.ownerId) + 1);
                } else {
                    ownerToFinishedProjects.put(project.ownerId, 1);
                }
            }

            for (Map.Entry<Integer, Integer> set : ownerToFinishedProjects.entrySet()) {
                if (set.getValue() >= projectsDone) {
                    projectsDoneIds.add(set.getKey());
                }
            }
        }

        ArrayList<User> providersInRange = User.getByPriceRange(myAgent.db, min, max);
        ArrayList<Integer> inRangeIds = new ArrayList<>();
        for (User p : providersInRange) {
            inRangeIds.add(p.id);
        }

        ArrayList<Integer> providersWithKeywords = null;
        if (keywords.size() != 0) {
            providersWithKeywords = new ArrayList<>(User.getWithKeywords(myAgent.db, keywords));
        }

        if (projectsDoneIds == null) {
            projectsDoneIds = inRangeIds;
        }
        if (providersWithKeywords == null) {
            providersWithKeywords = inRangeIds;
        }

        HashSet<Integer> first = new HashSet<>(inRangeIds);
        HashSet<Integer> second = new HashSet<>(projectsDoneIds);
        HashSet<Integer> third = new HashSet<>(providersWithKeywords);
        first.retainAll(second);
        first.retainAll(third);

        return new ArrayList<>(first);
    }

    private ArrayList<String> extractKeywords(String keywordsRaw) {
        ArrayList<String> keywords = new ArrayList<>();
        if (!keywordsRaw.equals("")) {
            Collections.addAll(keywords, keywordsRaw.split(","));
        }
        return keywords;
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
                MessageTemplate.MatchConversationId(Constants.retrieveProjectsListConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            int userId = data.getInt("user_id");
            ArrayList<Project> userProjects = Project.getUserProjects(myAgent.db, userId, Project.ASSIGNED);
            ArrayList<Project> projectsWithoutFeedback = getProjectsWithoutFeedback(userId);
            JSONArray response = new JSONArray();
            for (Project project: userProjects) {
                JSONObject projectJSON = project.json();
                projectJSON.put("feedback_required", false);
                response.put(projectJSON);
            }
            for (Project project: projectsWithoutFeedback) {
                JSONObject projectJSON = project.json();
                projectJSON.put("feedback_required", true);
                response.put(projectJSON);
            }
            myAgent.sendMessage(
                    response.toString(),
                    Constants.retrieveProjectsListConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }

    private ArrayList<Project> getProjectsWithoutFeedback(int userId) {
        ArrayList<Project> finishedProjects = Project.getUserProjects(myAgent.db, userId, Project.FINISHED);
        ArrayList<Feedback> userFeedbacks = Feedback.getUserFeedbacks(myAgent.db, userId, true);
        ArrayList<Project> projectsWithoutFeedback = new ArrayList<>();
        for (Project project : finishedProjects) {
            boolean alreadyHasFeedback = false;
            for (Feedback feedback : userFeedbacks) {
                if (feedback.project_id == project.id && feedback.sender_id == userId) {
                    alreadyHasFeedback = true;
                    break;
                }
            }

            if (!alreadyHasFeedback) {
                projectsWithoutFeedback.add(project);
            }
        }
        return projectsWithoutFeedback;
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

class ExtendProjectBehaviour extends TickerBehaviour {
    private ProjectAgent myAgent;

    public ExtendProjectBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ProjectAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.extendProjectConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            int projectId = data.getInt("project_id");
            int days = data.getInt("amount");
            Project.extend(myAgent.db, projectId, days);
            myAgent.sendMessage(
                    "",
                    Constants.extendProjectConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }
}

class UpdateProgressBehaviour extends TickerBehaviour {
    private ProjectAgent myAgent;

    public UpdateProgressBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ProjectAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.updateProgressConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            int projectId = data.getInt("project_id");
            int progress = data.getInt("amount");
            Project.updateProgress(myAgent.db, projectId, progress);
            myAgent.sendMessage(
                    "",
                    Constants.updateProgressConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }
}

class HoursUpdateBehaviour extends TickerBehaviour {
    private ProjectAgent myAgent;

    public HoursUpdateBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ProjectAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.hoursUpdateConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            int projectId = data.getInt("project_id");
            int hours = data.getInt("amount");
            int hoursSoFar = data.getInt("amount_so_far");
            Project.updateHours(myAgent.db, projectId, hours + hoursSoFar);
            myAgent.sendMessage(
                    "",
                    Constants.hoursUpdateConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }
}

class EndProjectBehaviour extends TickerBehaviour {
    private ProjectAgent myAgent;

    public EndProjectBehaviour(Agent a, long period) {
        super(a, period);
        myAgent = (ProjectAgent) a;
    }

    @Override
    protected void onTick() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId(Constants.endProjectConversationID)
        );

        ACLMessage message = myAgent.receive(template);
        if (message != null) {
            JSONObject data = new JSONObject(message.getContent());
            int projectId = data.getInt("project_id");
            Project.updateStatus(myAgent.db, projectId, Project.FINISHED);
            callForPayments(projectId);
            myAgent.sendMessage(
                    "",
                    Constants.endProjectConversationID,
                    ACLMessage.INFORM,
                    myAgent.searchForService(Constants.UIServiceName)
            );
        }
    }

    private ACLMessage callForPayments(int projectId) {
        JSONObject data = new JSONObject();
        data.put("project_id", projectId);
        StringWriter out = new StringWriter();
        data.write(out);
        myAgent.sendMessage(
                out.toString(),
                Constants.makeProjectPaymentConversationID,
                ACLMessage.REQUEST,
                myAgent.searchForService(Constants.paymentServiceName)
        );

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchConversationId(Constants.makeProjectPaymentConversationID)
        );
        return myAgent.blockingReceive(template);
    }
}

