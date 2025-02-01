package vendeur;

import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import misc.FishMarketPerformatif;


public class Vendeur extends GuiAgent {
    private static final Logger logger = Logger.getLogger(Vendeur.class.getName());
    private VendeurGUI gui;

    public AID getMarket() {
        return market;
    }

    private AID market = new AID("market", AID.ISLOCALNAME);
    private AID preneur = null;
    private Map<String, AID> agentsPreneurs = new HashMap<String, AID>();


    // Initialisation du nom, du prix initial et du pas de variation du prix
    private String name;
    private int price;
    private int pas;
    private int temps;
    private long start;


    /**
     * setup : création de l'automate FSM
     */
    @Override
    protected void setup() {
        logger.info("Agent Vendeur " + getName() + " is ready.");

        Object[] args = getArguments();
        // Vérifie s'il y a les arguments nécessaires
        if (args != null && args.length > 0) {

            gui = new VendeurGUI(this);
            gui.showGui();


            // FSM Behaviour
            FSMBehaviour fsm = new FSMBehaviour(this) {
                public int onEnd() {
                    logger.info(getAID().getName() + "-------------------------> Behaviour completed.");
                    logger.info("Fin de l'enchère.");
                    logger.info("Agent Vendeur " + getAID().getName() + " terminating.");
                    return super.onEnd();
                }
            };

            // Définition des états
            fsm.registerFirstState(new AttentePremiereOffre(), "1");
            fsm.registerState(new AttenteSecondeOffre(), "2");
            fsm.registerState(new AttenteAutresOffres(), "3");
            fsm.registerState(new Attribution(), "4");
            fsm.registerState(new AttentePaiement(), "5");
            fsm.registerState(new Livraison(), "6");

            // Définition des transactions
            fsm.registerDefaultTransition("1", "1");
            fsm.registerTransition("1", "1", FishMarketPerformatif.TO_ANNOUNCE);
            fsm.registerTransition("1", "2", FishMarketPerformatif.TO_BID);
            fsm.registerTransition("2", "3", FishMarketPerformatif.TO_BID);
            fsm.registerTransition("2", "4", FishMarketPerformatif.REP_BID_OK);
            fsm.registerTransition("3", "3", FishMarketPerformatif.TO_BID);
            fsm.registerTransition("3", "1", FishMarketPerformatif.REP_BID_NOK);
            fsm.registerTransition("4", "5", FishMarketPerformatif.TO_ATTRIBUTE);
            fsm.registerTransition("5", "6", FishMarketPerformatif.TO_PAY);

            addBehaviour(fsm);

        } else {
            // Termine l'agent
            logger.info("Pas de nom de poisson spécifié.");
            doDelete();
        }
        logger.info("Fin de setup du vendeur.");
    }

    /**
     * @param ev The GUI event to handle.
     */
    @Override
    protected void onGuiEvent(GuiEvent ev) {
        int eventType = ev.getType();

        if (eventType == 1) { // Vérification du type d'event
            name = (String) ev.getParameter(0);
            price = (Integer) ev.getParameter(1);
            pas = (Integer) ev.getParameter(2);
            temps = (Integer) ev.getParameter(3);

            logger.info("Enchère : " + name + " à " + price + "€ avec une variation de " + pas + "€.");

            // Description du service
            ServiceDescription sd = new ServiceDescription();
            sd.setName(name);
            sd.setType("Fishmarket");
            // Agents that want to use this service need to "know" the fish-auction-ontology
            sd.addOntologies("fish-auction-ontology");
            // Agents that want to use this service need to "speak" the FIPA-SL language
            sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);

            // Register the fishmarket service in the yellow pages
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            dfd.addServices(sd);
            try {
                DFService.register(this, new AID("market", AID.ISLOCALNAME), dfd);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }

    }


    // Définiton de tous les états possibles de l'automate

    /**
     * <b>AttentePremiereOffre</b> envoie un message <i>TO_ANNOUNCE</i> au marché et attend une réponse.
     * <ul>
     *     <li>Si le vendeur reçoit un message SUSCRIBE, il ajoute le preneur à sa liste de preneurs d'abonnés.</li>
     *     <li>Si le vendeur ne reçoit pas de message, il renvoit une offre plus faible.</li>
     * </ul>
     */
    private class AttentePremiereOffre extends OneShotBehaviour {
        private int returnPerformatif;

        @Override
        public void action() {
            //sendOffre();

            if (temps > 0) {
                //logger.info("Temps d'attente enregistré = " + temps);


                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
                ACLMessage msgReceived = myAgent.receive(mt);
                if (msgReceived != null) {
                    String preneur = msgReceived.getContent();
                    agentsPreneurs.add(msgReceived.getSender());
                    logger.info("Preneur " + msgReceived.getSender().getLocalName() + " ajouté.");
                }





                if (System.currentTimeMillis() - start > temps * 1000L) {
                    /*
                    MessageTemplate mt = MessageTemplate.MatchPerformative(FishMarketPerformatif.TO_BID); //PROPOSE
                    ACLMessage msgReceived = myAgent.receive(mt);
                    if (msgReceived != null) {
                        agentsPreneur.clear();
                        agentsPreneur.add(msgReceived.getSender());
                        logger.info("Ajout de " + msgReceived.getSender().getLocalName() + " à la liste des agents preneurs.");
                        returnPerformatif = FishMarketPerformatif.TO_BID;
                    } else {

                     */
                        price = price - pas;
                        if (price >= 0) {
                            sendOffre();
                            returnPerformatif = FishMarketPerformatif.TO_ANNOUNCE;
                        } else {
                            // TODO : voir comportement
                            //logger.info("Le prix est négatif.");
                        }

                    //}
                }
            }
        }

        @Override
        public int onEnd() {
            //logger.info("Vendeur end : " + returnPerformatif);
            return returnPerformatif;
        }
    }

    /**
     * Envoie une offre au marché via un ACLMessage
     */
    public void sendOffre() {
        ACLMessage msg = new ACLMessage(FishMarketPerformatif.TO_ANNOUNCE); //CFP
        msg.setContent(String.valueOf(price));
        msg.addReceiver(market);
        send(msg);
        logger.info("Message " + msg.getContent() + " envoyé");
        start = System.currentTimeMillis(); // Enregistrement du début de l'envoie de l'offre
        logger.info("start " + start);
    }


    /**
     * <b>AttenteSecondeOffre</b> attend une seconde offre d'un autre preneur.
     * <ul>
     *     <li>Si un autre preneur envoie un message SUBSCRIBE, il l'ajoute à sa liste de preneurs abonnées
     *     et va attendre d'autres offres dans la classe <b>AttenteAutresOffres</b></li>
     *     <li>S'il ne reçoit pas de seconde offre, l'enchère est attribuée au preneur abonné.</li>
     * </ul>
     */
    private class AttenteSecondeOffre extends OneShotBehaviour {
        private int returnPerformatif;

        @Override
        public void action() {
            logger.info("Arrivé dans la classe AttenteSecondeOffre.");

            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < temps * 1000L) {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
                ACLMessage msgReceived = myAgent.receive(mt);
                if (msgReceived != null) {
                    agentsPreneur.add(msgReceived.getSender());
                    logger.info("Preneur " + msgReceived.getSender().getLocalName() + " ajouté.");
                } else {
                    block();
                }
            }
            MessageTemplate mt = MessageTemplate.MatchPerformative(FishMarketPerformatif.TO_BID); //PROPOSE
            ACLMessage msgReceived = myAgent.receive(mt);
            if (msgReceived != null) {
                agentsPreneur.add(msgReceived.getSender());
                logger.info("Ajout de " + msgReceived.getSender().getLocalName() + " à la liste des agents preneurs.");
                returnPerformatif = FishMarketPerformatif.TO_BID;
            } else {
                returnPerformatif = FishMarketPerformatif.REP_BID_OK;
            }
        }

        @Override
        public int onEnd() {
            return returnPerformatif;
        }
    }

    /**
     * <b>AttenteAutresOffres</b> attend d'autres offre de potentiels preneurs.
     * <ul>
     *     <li>S'il en recoit d'autres, il reste dans cette classe.</li>
     *     <li>S'il n'en recoit pas d'autres durant le laps de temps indiqué,
     *     il retourne dans la classe <b>AttentePremiereOffre</b> pour renvoyer une offre avec un prix plus élevé.</li>
     * </ul>
     */
    private class AttenteAutresOffres extends OneShotBehaviour {
        private int returnPerformatif;

        @Override
        public void action() {
            logger.info("Arrivé dans la classe AttenteAutresOffres");

            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < temps * 1000L) {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
                ACLMessage msgReceived = myAgent.receive(mt);
                if (msgReceived != null) {
                    agentsPreneur.add(msgReceived.getSender());
                    logger.info("Preneur " + msgReceived.getSender().getLocalName() + " ajouté.");
                } else {
                    block();
                }
            }

            MessageTemplate mt = MessageTemplate.MatchPerformative(FishMarketPerformatif.TO_BID); // PROPOSE
            ACLMessage msgReceived = myAgent.receive(mt);
            if (msgReceived != null) {
                agentsPreneur.add(msgReceived.getSender());
                logger.info("Ajout de " + msgReceived.getSender().getLocalName() + " à la liste des agents preneurs.");
                returnPerformatif = FishMarketPerformatif.TO_BID;
            } else {
                price = price + pas;
                returnPerformatif = FishMarketPerformatif.REP_BID_NOK;
            }
        }

        @Override
        public int onEnd() {
            return returnPerformatif;
        }
    }


    /**
     * <b>Attribution</b> envoie un message TO_ATTRIBUTE au seul preneur qui a répondu à l'offre.
     */
    private class Attribution extends OneShotBehaviour {
        @Override
        public void action() {
            logger.info("Arrivé dans la classe Attribution.");
            ACLMessage msg = new ACLMessage(FishMarketPerformatif.TO_ATTRIBUTE); //ACCEPT_PROPOSAL
            // TODO : modifier new AID
            msg.addReceiver(agentsPreneur.get(0));
            send(msg);
        }

        @Override
        public int onEnd() {
            return FishMarketPerformatif.TO_ATTRIBUTE;
        }
    }

    /**
     * <b>AttentePaiement</b> vérifie que le preneur confirme bien le message ACCEPT_PROPOSAL que le vendeur lui a envoyé
     */
    private class AttentePaiement extends OneShotBehaviour {
        @Override
        public void action() {
            logger.info("Arrivé dans la classe ReceivedConfirm.");
            MessageTemplate mt = MessageTemplate.MatchPerformative(FishMarketPerformatif.TO_PAY); //CONFIRM
            ACLMessage msg = myAgent.receive(mt);
            if (msg == null) {
                block();
            }
        }

        @Override
        public int onEnd() {
            return FishMarketPerformatif.TO_PAY;
        }
    }

    /**
     * <b>SendAgree</b> envoie un message AGREE au marché avec comme contenu le poisson et supprime l'agent
     */
    private class Livraison extends OneShotBehaviour {
        @Override
        public void action() {
            logger.info("Arrivé dans la classe SendAgree.");
            // Envoie le message AGREE (TO_GIVE) au marché pour l'informer de la fin de l'enchère
            ACLMessage msg = new ACLMessage(FishMarketPerformatif.TO_GIVE); //AGREE
            msg.setContent(name);
            msg.addReceiver(market);
            send(msg);
            doDelete();
        }

        @Override
        public int onEnd() {
            return FishMarketPerformatif.TO_GIVE;
        }
    }


    protected void takeDown() {
        // Demande au DF de supprimer les services qui ont été inscrits par l'agent
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Ferme le GUI
        gui.dispose();
        logger.info("Agent Vendeur " + getName() + " done.");
    }

}
