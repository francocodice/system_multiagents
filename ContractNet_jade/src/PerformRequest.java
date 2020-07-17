import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;

import java.util.Enumeration;
import java.util.Vector;

public class PerformRequest extends ContractNetInitiator {
    public Article currentArticle;
    public int currentRefuse = 0;
    public int currentRejectd = 0;

    public PerformRequest(ContractNetInitiatorAgent a, ACLMessage cfp) {
        super(a, cfp);
        try {
            this.currentArticle = (Article) cfp.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
    }

    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            System.out.println("Responder does not exist");
        } else {
            System.out.println("Agent " + failure.getSender().getName() + " failed");
        }
        // Immediate failure --> we will not receive a response from this agent
        ((ContractNetInitiatorAgent)myAgent).nResponders--;
    }

    protected void handleRefuse(ACLMessage refuse) {
        currentRefuse++;
    }

    protected void handleAllResponses(Vector responses, Vector acceptances) {
        if (responses.size() < ((ContractNetInitiatorAgent)myAgent).nResponders) {
            System.out.println("Timeout expired: missing " + (((ContractNetInitiatorAgent)myAgent).nResponders - responses.size()) + " responses");
        }
        // Evaluate proposals.
        double bestProposal = -1.0;
        AID bestProposer = null;
        ACLMessage accept = null;
        Enumeration e = responses.elements();
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                acceptances.addElement(reply);
                double proposal = Double.parseDouble(msg.getContent());
                if(proposal > currentArticle.getPrice()){
                    if (proposal > bestProposal) {
                        bestProposal = proposal;
                        bestProposer = msg.getSender();
                        accept = reply;
                    }
                }
            } else {
                currentRejectd++;
            }
        }
        // Accept the proposal of the best proposer
        if (accept != null) {
            System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        } else if (accept == null && currentRefuse == currentRejectd){
            ((ContractNetInitiatorAgent)myAgent).increaseDiscount(currentArticle.getName());
        }
    }

    protected void handleInform(ACLMessage inform) {
        System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
        ((ContractNetInitiatorAgent)myAgent).deleteArticle(currentArticle.getName());
        System.out.println("\nCurrent Invetory:");
        for(Article a : ((ContractNetInitiatorAgent) myAgent).invetory){
            System.out.println("Aritcle + " + a.getName() + "Price: " + a.getPrice());
        }
        System.out.println();
    }

}
