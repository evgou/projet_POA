package preneur;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
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
import marche.Prix;
import jade.domain.DFService;
import java.util.Vector;
import java.util.logging.Logger;


public class Preneur extends GuiAgent {

    private String myName;
    private float _budget;
    private float fondCourant;
    private boolean modeAuto = false;
    private static final Logger logger = Logger.getLogger(Preneur.class.getName());

    public float get_budget() {
        return _budget;
    }

    public float getFondCourant() {
        return fondCourant;
    }

    public boolean isModeAuto() {
        return modeAuto;
    }

    protected void setup() {
        System.out.println("Agent " + getAID().getLocalName() + " started !");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            myName = (String) args[0];
            PreneurGUI gui = new PreneurGUI(this);
            gui.showGui();

            String serviceType = "Fishmarket";
            logger.info("Agent " + this.getName() + " trying to subscribe for services of type " + serviceType);

            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(serviceType);
            dfd.addServices(sd);

            // Message d'abonnement

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
                            gui.addOffer(vendeur.getLocalName(), "test", 40);
                            logger.info("Le preneur a reçu une annonce");
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                }
            });

            addBehaviour(new CyclicBehaviour(this) {
                public void action() {
                    ACLMessage msg = receive(); // Reçoit n'importe quel message

                    if (msg != null) {
                        int performative = msg.getPerformative();

                        if (performative == ACLMessage.INFORM) {
                            System.out.println("Contenu du message : " + msg.getContent());
                            gui.addOffer(msg.getSender().getLocalName(), "poisson", Integer.parseInt(msg.getContent()));
                        }
                    } else {
                        block();  // Bloque le comportement en attendant un message
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
        automatePreneur.registerState(new toBidBehaviour(), "To Bid");
        automatePreneur.registerState(new repBidBehaviour(), "Rep Bid");
        automatePreneur.registerState(new attributeBehaviour(), "Attribute");
        automatePreneur.registerState(new toPayBehaviour(), "To Pay");
        automatePreneur.registerState(new toGiveBehaviour(), "To Give");

        //On définit les transitions de l'agent Preneur
        automatePreneur.registerTransition("Départ", "To Bid", FishMarketPerformatif.TO_ANNOUNCE);
        automatePreneur.registerTransition("To Bid", "To Bid", FishMarketPerformatif.TO_ANNOUNCE);
        automatePreneur.registerTransition("To Bid", "Rep Bid", FishMarketPerformatif.TO_BID);
        automatePreneur.registerTransition("Rep Bid", "Départ", FishMarketPerformatif.REP_BID_NOK);
        automatePreneur.registerTransition("Rep Bid", "Attribute", FishMarketPerformatif.REP_BID_OK);
        automatePreneur.registerTransition("Attribute", "To Pay", FishMarketPerformatif.TO_ATTRIBUTE);
        automatePreneur.registerTransition("To Pay", "To Give", FishMarketPerformatif.TO_GIVE);

    }

    private class departBehaviour extends OneShotBehaviour{
        public void action(){
            logger.info("action du depart behaviour");
            ACLMessage msg = myAgent.receive();
            if(msg != null && msg.getPerformative() == FishMarketPerformatif.TO_ANNOUNCE){
                logger.info(getAID().getName() + " a reçu une offre");
                try {
                    Prix prix = (Prix) msg.getContentObject();
                    if(isModeAuto() && prix.getPrice() < fondCourant){
                        msg.createReply(FishMarketPerformatif.TO_BID);
                    }
                } catch (UnreadableException e) {
                    logger.severe("Erreur du message de " + getAID().getName() + " : " + e.getMessage());
                    throw new RuntimeException(e);
                }

            }
        }
        public int onEnd(){
            return FishMarketPerformatif.TO_ANNOUNCE;
        }
    }

    private class toBidBehaviour extends OneShotBehaviour{
        public void action(){
            logger.info("action du to bid behaviour");
        }
        public int onEnd(){
            return 2;
        }
    }

    private class repBidBehaviour extends OneShotBehaviour{
        public void action(){
            logger.info("action du rep bid behaviour");
        }
        public int onEnd(){
            return 3;
        }
    }

    private class attributeBehaviour extends OneShotBehaviour{
        public void action(){
            logger.info("action du attribute behaviour");
        }
        public int onEnd(){
            return 4;
        }
    }

    private class toPayBehaviour extends OneShotBehaviour{
        public void action(){
            logger.info("action du to pay behaviour");
        }
        public int onEnd(){
            return 5;
        }
    }

    private class toGiveBehaviour extends OneShotBehaviour{
        public void action(){
            logger.info("action du to give behaviour");
        }
        public int onEnd(){
            return 6;
        }
    }

    @Override
    protected void onGuiEvent(GuiEvent ev) {
        logger.info("Commande reçue depuis l'IHM : " + ev.getAllParameter());
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