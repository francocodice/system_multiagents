import jade.core.Agent;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.RefuseException;

import java.util.HashSet;
import java.util.Set;

public class PerformResponder extends ContractNetResponder {
    public PerformResponder(Agent a, MessageTemplate mt) {
        super(a, mt);
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException {
        Article itemOffered = null;
        try {
            itemOffered = (Article) cfp.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        if (articleIsRelevant(itemOffered)) {
            if(((ContractNetResponderAgent) myAgent).availabilityMoney > 0.0){
                double proposal = evaluteArticle(itemOffered);
                if(proposal > ((ContractNetResponderAgent) myAgent).availabilityMoney){
                    proposal = ((ContractNetResponderAgent) myAgent).availabilityMoney;
                }
                System.out.println("Agent " + myAgent.getLocalName() + " Proposing :" + proposal);
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(String.valueOf(proposal));
                return propose;
            } else{
                ACLMessage refuse = cfp.createReply();
                refuse.setPerformative(ACLMessage.REFUSE);
                return refuse;
            }

        } else if (itemOffered.isDiscount()) {
            double proposal = ((itemOffered.getPrice()/2) + (((ContractNetResponderAgent) myAgent).availabilityMoney * 0.10));
            System.out.println("Agent " + myAgent.getLocalName() + " Proposing :" + proposal);
            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            propose.setContent(String.valueOf(proposal));
            return propose;
        } else {
            System.out.println("Agent " + myAgent.getLocalName() + ": Refuse");
            throw new RefuseException("evaluation-failed");
        }
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
        System.out.println("Agent " + myAgent.getLocalName() + ": Proposal accepted");
        Article contentObject;
        try {
            contentObject = (Article) cfp.getContentObject();
            double proposal = Double.parseDouble(propose.getContent());
            ((ContractNetResponderAgent) myAgent).buyItem(contentObject,proposal);
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        //System.out.println("Agent " + myAgent.getLocalName() + " has items " + ((ContractNetResponderAgent)myAgent).currentItems);
        ACLMessage inform = accept.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        return inform;
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        System.out.println("Agent " + myAgent.getLocalName() + ": Proposal rejected");
        //System.out.println("Agent " + myAgent.getLocalName() + " has items " + ((ContractNetResponderAgent)myAgent).currentItems);
    }

    private double evaluteArticle(Article itemOffered) {
        int score = 0;
        for (Interest i : ((ContractNetResponderAgent) myAgent).Interests) {
            if (itemOffered.getDomain().contains(i.getValue())) {
                score += i.getRank();
            }
        }
        return itemOffered.getPrice() + (score * 0.01) * itemOffered.getPrice();
    }

    private boolean articleIsRelevant(Article itemOffered) {
        Set<String> res = new HashSet<String>();
        for (Interest i : ((ContractNetResponderAgent) myAgent).Interests) {
            res.add(i.getValue());
        }
        for (String interest : itemOffered.getDomain()) {
            if ((res.contains(interest.toLowerCase()))) {
                return true;
            }
        }
        return false;
    }



}
