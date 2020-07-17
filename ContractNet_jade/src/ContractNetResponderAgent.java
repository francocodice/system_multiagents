/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * <p>
 * GNU Lesser General Public License
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;


public class ContractNetResponderAgent extends Agent {
    public List<Article> currentItems = new LinkedList();
    public double availabilityMoney = 0.0;
    public List<Interest> Interests = new LinkedList<>();


    protected void setup() {
        buildAgents();
        System.out.println("Agent Name : " + getLocalName()
                + " Money Avaiable : " + availabilityMoney
                + " Interessi : " + printInterest() + " waiting for CFP...");
        MessageTemplate templateResponder = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));

        addBehaviour(new PerformResponder(this, templateResponder));
        addBehaviour(new PerformResponderSaleEnded(this, 3000));
    }

    private class PerformResponderSaleEnded extends TickerBehaviour {
        public PerformResponderSaleEnded(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            MessageTemplate templateSaleEnded = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(templateSaleEnded);
            if (msg != null) {
				if(msg.getContent().equals("sale-ended")){
                    System.out.println("For Agent : " + myAgent.getLocalName() + " Money Avaiable: " + availabilityMoney);
                    myAgent.doDelete();
                    System.out.println("For Agent : " + myAgent.getLocalName() + " Invetory size: " + currentItems.size());
                }
            } else {
                block();
            }
        }
    }

    protected void buyItem(Article a, double proposal){
        System.out.println("Agent" + getLocalName() + "decrease money of " + proposal);
        currentItems.add(a);
        availabilityMoney = availabilityMoney - proposal;
    }

    private void buildAgents() {
        Set<String> interestes = getInterests();
        availabilityMoney = getRandomVar(400,2000);
        for(int index = 0; index < 5; index++){
            String val = getRandomElement(interestes);
            interestes.remove(val);
            Interests.add(new Interest(val,getRandomVar(1,10)));
        }
    }


    public Set<String> getInterests(){
        Set<String> interestes = new HashSet<>();
        try {
            File myObj = new File("resources/interets.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                interestes.addAll(Arrays.asList(data.split(",")));
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return interestes;
    }

    private static String getRandomElement(Set<String> set){
        Random random = new Random();
        int randomNumber = random.nextInt(set.size());
        Iterator<String> iterator = set.iterator();
        int currentIndex = 0;
        String randomElement = null;
        while(iterator.hasNext()){
            randomElement = iterator.next();
            if(currentIndex == randomNumber)
                return randomElement;
            currentIndex++;
        }
        return randomElement;
    }

    public int getRandomVar(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    private String printInterest() {
        String res = "";
        for(Interest i : Interests){
            res += i.getValue() + ",";
        }
        return res;
    }



}


