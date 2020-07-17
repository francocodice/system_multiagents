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
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPANames;

import java.io.*;
import java.util.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class ContractNetInitiatorAgent extends Agent {
    public int nResponders;
    public List<Article> invetory = new LinkedList<>();
    public List<AID> buyers = new LinkedList<>();

    protected void setup() {
        try {
            fillInvetory();
        } catch (IOException e) {
            e.printStackTrace();
        }
        findBuyers();
        addBehaviour(new SellItem(this, 10000));
    }

    @Override
    protected void takeDown() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        Object[] args = getArguments();
        for (int i = 0; i < getArguments().length; ++i) {
            msg.addReceiver(new AID((String) args[i], AID.ISLOCALNAME));
        }
        msg.setContent("sale-ended");
        this.send(msg);
    }

    public class SellItem extends TickerBehaviour {
        public SellItem(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            if (invetory.size() == 0) this.myAgent.doDelete();
            else {
                System.out.println("Trying to sell {"
                        + invetory.get(0).getName() + " , "
                        + invetory.get(0).getPrice() + "} to one of " + nResponders + " responders.");
                try {
                    ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < buyers.size(); ++i) {
                        msg.addReceiver(buyers.get(i));
                    }
                    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                    msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
                    msg.setContentObject(invetory.get(0));
                    addBehaviour(new PerformRequest((ContractNetInitiatorAgent) myAgent, msg));
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        }
    }

    private void findBuyers() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            nResponders = args.length;
            for (int i = 0; i < args.length; ++i) {
                buyers.add(new AID((String) args[i], AID.ISLOCALNAME));
            }
        } else {
            System.out.println("No responder specified.");
        }
    }

    private void fillInvetory() throws IOException {
        StringTokenizer st ;
        BufferedReader TSVFile = new BufferedReader(new FileReader("resources/article.tsv"));
        String dataRow = TSVFile.readLine();
        int index = 0;
        while (dataRow != null){
            st = new StringTokenizer(dataRow,"\t");
            List<String>dataArray = new ArrayList<String>() ;
            while(st.hasMoreElements()){
                dataArray.add(st.nextElement().toString());
            }
            if(index > 0){
                List<String> domain = Arrays.asList(dataArray.get(2).split(","));
                Article item = new Article(dataArray.get(0), Double.parseDouble(dataArray.get(1)), domain);
                invetory.add(item);
            }
            dataRow = TSVFile.readLine();
            index++;
        }
        TSVFile.close();
    }

    public void deleteArticle(String name) {
        for (int index = 0; index < invetory.size(); index++) {
            if (invetory.get(index).getName().equals(name)) {
                invetory.remove(index);
            }
        }
    }

    public void increaseDiscount(String name){
        for(Article a : invetory){
            if(a.getName().equals(name)){
                a.increaseDiscount();
            }
        }
    }
}



