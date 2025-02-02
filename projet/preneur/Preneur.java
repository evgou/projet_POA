package preneur;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.*;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;
import misc.FishMarketPerformatif;
import jade.domain.DFService;
import marche.Prix;

import java.util.Vector;
import java.util.logging.Logger;

public class Preneur extends GuiAgent {

    private String myName;
    private float _budget;
    private float fondCourant;
    private boolean modeAuto = false;
    private AID vendeur;
    private static final Logger logger = Logger.getLogger(Preneur.class.getName());

    protected void setup() {
        System.out.println("Agent " + getAID().getLocalName() + " started !");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            myName = (String) args[0];
            PreneurGUI gui = new PreneurGUI(this);
            gui.showGui();

            // Dans la méthode setup() de PreneurAgent
            String serviceType = "Fishmarket";
            logger.info("Agent " + this.getName() + " trying to subscribe for services of type " + serviceType);

            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(serviceType);
            dfd.addServices(sd);

            // Message d'abonnement

            // Création du message d'abonnement au DF
            ACLMessage subscriptionMsg = DFService.createSubscriptionMessage(this, new AID("market", AID.ISLOCALNAME), dfd, null);
            subscriptionMsg.addReceiver(new AID("market", AID.ISLOCALNAME));
            send(subscriptionMsg);

            // FSM Behaviour
            FSMBehaviour fsm = new FSMBehaviour(this) {
                public int onEnd() {
                    logger.info(getAID().getName() + "-------------------------> Behaviour completed.");
                    logger.info("Agent Vendeur " + getAID().getName() + " terminating.");
                    return super.onEnd();
                }
            };

            // Définition des états
            fsm.registerFirstState(new AJoutenchere(), "0");
            fsm.registerState(new TraitementAnnonce(), "1");
            fsm.registerState(new AttenteReponseOffre(), "2");
            fsm.registerState(new AttenteAttribution(), "3");
            fsm.registerState(new Paiement(), "4");
            fsm.registerState(new AttenteLivraison(), "5");

            // Définition des transactions
            fsm.registerDefaultTransition("0", "1");
            fsm.registerTransition("1", "1", FishMarketPerformatif.TO_ANNOUNCE);
            fsm.registerTransition("1", "2", FishMarketPerformatif.TO_BID);
            fsm.registerTransition("2", "1", FishMarketPerformatif.REP_BID_NOK);
            fsm.registerTransition("2", "3", FishMarketPerformatif.REP_BID_OK);
            fsm.registerTransition("3", "4", FishMarketPerformatif.TO_ATTRIBUTE);
            fsm.registerTransition("4", "5", FishMarketPerformatif.TO_PAY);

            addBehaviour(fsm);

        } else {
            // Make the agent terminate
            System.out.println("No name specified");
            doDelete();
        }
    }

    protected void takeDown() {
        System.out.println("Agent " + getAID().getName() + " terminating.");
    }


    @Override
    protected void onGuiEvent(GuiEvent ev) {
        logger.info("Commande reçue depuis l'IHM : " + ev.getAllParameter());
    }


    private class AJoutenchere extends OneShotBehaviour {

        @Override
        public void action() {
            ACLMessage sub = new ACLMessage(ACLMessage.SUBSCRIBE);
            sub.addReceiver(new AID("market", AID.ISLOCALNAME));
            send(sub);
        }
    }

    private class TraitementAnnonce extends OneShotBehaviour {
        private int returnPerformatif;

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                switch (msg.getPerformative()) {

                    case FishMarketPerformatif.TO_ANNOUNCE:
                        logger.info(getAID().getName() + " a reçu une offre");
                        try {
                            Prix prix = (Prix) msg.getContentObject();
                            logger.info("Prix reçu : " + prix);
                            ACLMessage reply = new ACLMessage(FishMarketPerformatif.TO_BID); //PROPOSE
                            reply.addReceiver(msg.getSender());
                            send(reply);
                            logger.info("Envoie d'un TO-BID au vendeur : " + msg.getSender().getLocalName());
                        } catch (UnreadableException e) {
                            logger.severe("Erreur du message de " + getAID().getName() + " : " + e.getMessage());
                            throw new RuntimeException(e);
                        }
                        returnPerformatif = FishMarketPerformatif.TO_BID;
                        break;
                    default:
                        break;
                }
            } else {
                logger.info("Je n'ai rien");
                returnPerformatif = FishMarketPerformatif.TO_ANNOUNCE;
                block();
            }
        }

        @Override
        public int onEnd() {
            return returnPerformatif;
        }
    }


    private class AttenteReponseOffre extends OneShotBehaviour {
        private int returnPerformatif;

        @Override
        public void action() {
            logger.info("Arrivée dans la classe AttenteReponseOffre.");
            doWait(20000);
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                switch (msg.getPerformative()) {
                    case FishMarketPerformatif.REP_BID_OK:
                        logger.info(myAgent.getName() + " a reçu une réponse positive de " + msg.getSender());
                        returnPerformatif = FishMarketPerformatif.REP_BID_OK;
                        break;
                    case FishMarketPerformatif.REP_BID_NOK:
                        logger.info(myAgent.getName() + " a reçu une réponse négative de " + msg.getSender());
                        returnPerformatif = FishMarketPerformatif.REP_BID_NOK;
                        break;
                    default:
                        logger.info("Received performative " + msg.getPerformative());
                        break;
                }
            } else {
                logger.info("Je n'ai rien");
                block();
            }
        }

        @Override
        public int onEnd() {
            return returnPerformatif;
        }
    }

    private class AttenteAttribution extends OneShotBehaviour {

        public void action() {
            logger.info("Arrivée dans la classe AttenteAttribution.");
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                switch (msg.getPerformative()) {
                    case FishMarketPerformatif.TO_ATTRIBUTE:
                        logger.info("L'agent " + msg.getSender() + " a attribué l'offre à " + myAgent.getName());
                        vendeur = msg.getSender();
                        break;
                    default:
                        break;
                }
            } else {
                block();
            }
        }

        @Override
        public int onEnd() {
            return FishMarketPerformatif.TO_ATTRIBUTE;
        }
    }

    private class Paiement extends OneShotBehaviour {

        @Override
        public void action() {
            logger.info("Arrivée dans la classe Paiement, on veut envoyer un message au vendeur " + vendeur.getLocalName());
            ACLMessage reply = new ACLMessage(FishMarketPerformatif.TO_PAY); //CONFIRM
            reply.addReceiver(vendeur);
            send(reply);
        }

        @Override
        public int onEnd() {
            return FishMarketPerformatif.TO_PAY;
        }
    }

    private class AttenteLivraison extends OneShotBehaviour {

        @Override
        public void action() {
            logger.info("Arrivée dans la classe AttenteLivraison.");
            doWait(20000);
            MessageTemplate mt = MessageTemplate.MatchPerformative(FishMarketPerformatif.TO_GIVE); //AGREE
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                logger.info("L'agent vendeur " + msg.getSender().getLocalName() + " a livré " + msg.getContent() + " à " + myAgent.getName());
            } else {
                logger.warning(myAgent.getName() + " n'a pas reçu de messgae.");
                block();
            }

        }

        @Override
        public int onEnd() {
            logger.info("Fin de traitement, doDelete.");
            return 0;
        }
    }


    // Méthode pour afficher des messages sur l'IHM
    public void log(String message) {
        logger.info(message);
    }

    public void placeBid(String vendeur) {
        logger.info(this.myName + " est dans placeBid : " + vendeur);
    }

    public void onSelectionValidated(Vector<String> offres, boolean isAuto, float budget) {
        this._budget = budget;
        this.modeAuto = isAuto;
        logger.info(this.myName + " est dans onSelectionValidated : " + isAuto);
    }

}
