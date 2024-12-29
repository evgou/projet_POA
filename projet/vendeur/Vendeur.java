package vendeur;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.util.logging.Logger;


public class Vendeur extends Agent {
    private static final Logger logger = Logger.getLogger(AgentInitiateur.class.getName());

    /**
     * classe setup :
     */
    protected void setup() {
        logger.info("Agent Vendeur " + this.getName() + " is ready.");

        // FSM Behaviour
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                System.out.println(getAID().getName() + "-------------------------> Behaviour completed.");
                System.out.println("Agent Vendeur " + getAID().getName() + " terminating.");
                return super.onEnd();
            }
        };

        // TODO myGui =


        // Description du service
        ServiceDescription sd = new ServiceDescription();
        sd.setName(serviceName);
        sd.setType("Fishmarket");
        // Agents that want to use this service need to "know" the fish-auction-ontology
        sd.addOntologies("fish-auction-ontology");
        // Agents that want to use this service need to "speak" the FIPA-SL language
        sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
        // TODO terminer le code

        // Register the fishmarket service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);

        // Mise à jour de la liste des agents "seller"
        try {
            DSFAgentDescription[] result = DFService.search(myAgent, dfd);
            sellerAgents = new AID[result.length];
            for (int i = 0; i < result.length; i++) {
                sellerAgents[i] = result[i].getName();
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        DFService.register(this, new AID("market", AID.ISLOCALNAME), dfd);

        // Definition des etats
        fsm.registerFirstState (new OfferRequestsServer(), "CFP");
        // TODO modifier le nom des classes qu'on appelle (et les créer)
        fsm.registerState(new ReceivedPropose(), "Propose");
        fsm.registerState(new OneMoreTime(), "Refuse");
        fsm.registerState(new OneMoreTime(), "Inform");
        fsm.registerState(new OneMoreTime(), "Accept_proposal");
        fsm.registerState(new ReceivedConfirm(), "Confirm");
        fsm.registerLastState(new SendAgree(), "Agree");

        // Definition des transactions
        fsm.registerDefaultTransition("CFP", "CFP");
        fsm.registerTransition("CFP", "Propose", 1);
        fsm.registerTransition("Propose", "Inform", 2);
        fsm.registerTransition("Inform", "Accept_proposal", 3);
        fsm.registerTransition("Accept_proposal", "Confirm", 4);
        fsm.registerTransition("Confirm", "Agree", 5);

        addBehaviour(fsm);

    }


    // TODO : utilité ?
    private static ACLMessage sendRequest(String message) {
        ACLMessage request = new ACLMessage(ACLMessage.CFP);
        request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        request.addReceiver(new AID("preneur", AID.ISLOCALNAME));
        request.setContent(message);
        return request;
    }

    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // Traitement du message PROPOSE recu
                String price = msg.getContent();
                ACLMessage reply = msg.createReply();

                // TODO : qu'est ce que le catalogue ?
                Integer price = (Integer) catalogue.get(price);
                if (price != null) {
                    // Le poisson est disponible à la vente : envoie du message CFP avec le prix
                    reply.setPerformative(ACLMessage.CFP);
                    reply.setContent(String.valueOf(price.intValue()));
                }
                else {
                    // Le poisson n'est pas disponible à la vente : envoie du message REFUSE
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }

    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // ACCEPT_PROPOSAL Message received. Process it
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();

                Integer price = (Integer) catalogue.remove(title);
                if (price != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(title+" sold to agent "+msg.getSender().getName());
                }
                else {
                    // The requested book has been sold to another buyer in the meanwhile .
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }

    // TODO ReceiverBehaviour
    private class ReceivedPropose extends OneShotBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage msg = myAgent.receive(mt);
            msg.getContent();
            if (msg == null) {
                block();
            }
        }
        public int onEnd() {
            return 5;
        }
    }


    private class ReceivedConfirm extends OneShotBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
            ACLMessage msg = myAgent.receive(mt);
            if (msg == null) {
                block();
            }
        }
        public int onEnd() {
            return 5;
        }
    }

    private class SendAgree extends OneShotBehaviour {
        public void action() {
            // Envoie le message TO_GIVE (AGREE) au marché pour l'informer de la fin de l'enchère
            ACLMessage msg_give = new ACLMessage(ACLMessage.AGREE);
            // TODO : mettre le nom de l'enchère pour récupérer le nom du poisson
            msg_give.setContent("poisson");
            msg_give.addReceiver(new AID("market", AID.ISLOCALNAME));
            send(msg_give);
        }
    }


    protected void takeDown() {
        // Demande au DF de supprimer les services qui ont été inscrits par l'agent
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Ferme le GUI
        // TODO : création du "myGui" ?
        myGui.dispose();

        logger.info("Agent Vendeur " + this.getName() + " done.");
    }

}
