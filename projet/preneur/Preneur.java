package preneur;

import jade.core.AID;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionInitiator;
import misc.FishMarketPerformatif;
import jade.domain.DFService;
import marche.Prix;

import java.util.logging.Logger;

public class Preneur extends GuiAgent {

    private String myName;
    private float _budget;
    private float fondCourant;
    private static final Logger logger = Logger.getLogger(Preneur.class.getName());

    protected void setup() {
        System.out.println("Agent " + getAID().getLocalName() + " started !");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            myName = (String) args[0];
            //PreneurAgentGUI gui = new PreneurAgentGUI(this);
            //gui.showGui();

            // Dans la méthode setup() de PreneurAgent
            String serviceType = "Fishmarket";
            logger.info("Agent " + this.getName() + " trying to subscribe for services of type " + serviceType);

            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(serviceType);
            dfd.addServices(sd);

            // Création du message d'abonnement au DF
            ACLMessage subscriptionMsg = DFService.createSubscriptionMessage(this, new AID ("market", AID.ISLOCALNAME), dfd, null);
            subscriptionMsg.addReceiver(new AID ("market", AID.ISLOCALNAME));
            send(subscriptionMsg);


            addBehaviour(new SubscriptionInitiator(this, subscriptionMsg) {
                @Override
                protected void handleInform(ACLMessage inform) {
                    try {
                        DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
                        for (DFAgentDescription dfd : results) {
                            AID vendeur = dfd.getName();
                            // TODO : Trouver comment ajouter les offres à l'IHM
                            //gui.addOffer(vendeur.getLocalName(), "test", 40);
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                }
            });

            this.automate();
        }
        else {
            // Make the agent terminate
            System.out.println("No name specified");
            doDelete();
        }
    }

    protected void takeDown() {
        System.out.println("Agent "+getAID().getName()+" terminating.");
    }

    protected void automate(){
        FSMBehaviour automatePreneur = new FSMBehaviour(){
            @Override
            public int onEnd() {
                logger.info(getAID().getName() + "-------------------------> Behaviour completed.");
                logger.info("Fin de l'enchère.");
                logger.info("Agent Vendeur " + getAID().getName() + " terminating.");
                return super.onEnd();
            }
        };

        //On définit les états de l'agent Preneur
        automatePreneur.registerFirstState(new departBehaviour(), "Départ");
        //automatePreneur.registerState(new toBidBehaviour(), "To Bid");
        //automatePreneur.registerState(new repBidBehaviour(), "Rep Bid");
        //automatePreneur.registerState(new attributeBehaviour(), "Attribute");
        //automatePreneur.registerState(new toPayBehaviour(), "To Pay");
        //automatePreneur.registerState(new toGiveBehaviour(), "To Give");

        //On définit les transitions de l'agent Preneur
        automatePreneur.registerTransition("Départ", "To Bid", 1);
        automatePreneur.registerTransition("To Bid", "To Bid", 2);

    }

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {

    }

    private class departBehaviour extends OneShotBehaviour{
        public void action(){
            logger.info("action du depart behaviour");
            ACLMessage msg = myAgent.receive();
            if(msg != null && msg.getPerformative() == FishMarketPerformatif.TO_ANNOUNCE){
                logger.info(getAID().getName() + " a reçu une offre");
                try {
                    Prix prix = (Prix) msg.getContentObject();
                    logger.info("Prix reçu : " + prix);
                } catch (UnreadableException e) {
                    logger.severe("Erreur du message de " + getAID().getName() + " : " + e.getMessage());
                    throw new RuntimeException(e);
                }

            }
        }
        public int onEnd(){
            return 1;
        }
    }
    
}
