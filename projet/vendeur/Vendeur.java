package vendeur;

import jade.core.Agent;
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
import java.util.List;
import java.util.logging.Logger;
import misc.FishMarketPerformatif;

/**
 * On appelle cet agent (vendeur) avec en argument son nom
 */
public class Vendeur extends GuiAgent {
    private static final Logger logger = Logger.getLogger(Vendeur.class.getName());
    private VendeurGUI gui;

    AID market = new AID("market", AID.ISLOCALNAME);
    private AID preneur = null;
    private List<AID> agentsPreneur;


    // Valeur du prix et du pas par défaut pour le moment
    // TODO : modifier et le mettre dans l'interface
    private int price = 1000;
    private final int pas = 50;


    /**
     * setup : création de l'automate FSM
     */
    @Override
    protected void setup() {
        logger.info("Agent Vendeur " + getName() + " is ready.");

        // Envoie d'un message au marché pour se faire connaitre et s'enregistrer dans la liste des vendeurs
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(market);
        msg.setContent("0");
        send(msg);


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
            fsm.registerState(new AttentePremiereOffre(), "1");
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
            fsm.registerTransition("3", "0", FishMarketPerformatif.REP_BID_NOK);
            fsm.registerTransition("4", "5", FishMarketPerformatif.TO_ATTRIBUTE);
            fsm.registerTransition("5", "6", FishMarketPerformatif.TO_PAY);

            addBehaviour(fsm);


            // Description du service
            String poisson = args[1].toString();
            ServiceDescription sd = new ServiceDescription();
            sd.setName(poisson);
            sd.setType("fipa-df");
            // TODO : à vérifier : fishamrket ou fipa-df
            // Agents that want to use this service need to "know" the fish-auction-ontology
            sd.addOntologies("fish-auction-ontology");
            // Agents that want to use this service need to "speak" the FIPA-SL language
            sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
            // TODO terminer le code

            // Register the fishmarket service in the yellow pages
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            dfd.addServices(sd);
            try {
                DFService.register(this, market, dfd);
            }
            catch (FIPAException fe) {
                fe.printStackTrace();
            }

            // Gestion de l'abonnement des preneurs
            // TODO : on recoit un message du marché pour connaitre les preneurs abonnés



        }
        else {
            // Termine l'agent
            System.out.println("Pas de nom de poisson spécifié.");
            doDelete();
        }
    }

    /**
     * @param ev The GUI event to handle.
     */
    @Override
    protected void onGuiEvent(GuiEvent ev) {
        logger.info("Commande reçue depuis l'IHM : " + ev.getAllParameter());
    }


    // Définiton de tous les états possibles de l'automate

    /**
     * <b>AttentePremiereOffre</b> envoie un message <i>TO_ANNONCE</i> au marché et attend une réponse.
     * <ul>
     *     <li>Si le vendeur reçoit un message SUSCRIBE, il ajoute le preneur à sa liste de preneurs d'abonnés.</li>
     *     <li>Si le vendeur ne reçoit pas de message, il renvoit une offre plus faible.</li>
     * </ul>
     */
    private class AttentePremiereOffre extends OneShotBehaviour {
        @Override
        public void action() {
            logger.info("Arrivé dans la classe AttentePremiereOffre.");
            ACLMessage msg = new ACLMessage(FishMarketPerformatif.TO_ANNOUNCE); //CFP
            msg.setContent(String.valueOf(price));
            msg.addReceiver(market);
            send(msg);

            // TODO : vérifier comment on reçoit les messages SUBSCRIBE
            MessageTemplate mt = MessageTemplate.MatchPerformative(FishMarketPerformatif.TO_BID); //PROPOSE
            ACLMessage msgReceived = myAgent.receive(mt);
            if (msgReceived != null) {
                agentsPreneur.clear();
                agentsPreneur.add(msgReceived.getSender());
                logger.info("Ajout de " + msgReceived.getSender().getLocalName() + " à la liste des agents preneurs.");
              //  return FishMarketPerformatif.TO_BID;
            } else {
                price = price - pas;
              //  return FishMarketPerformatif.TO_ANNOUNCE;
            }
        }
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
        @Override
        public void action() {
            // TODO : mettre un compteur
            logger.info("Arrivé dans la classe AttenteSecondeOffre.");
            MessageTemplate mt = MessageTemplate.MatchPerformative(FishMarketPerformatif.TO_BID); //PROPOSE
            ACLMessage msgReceived = myAgent.receive(mt);
            if (msgReceived != null) {
                agentsPreneur.add(msgReceived.getSender());
                logger.info("Ajout de " + msgReceived.getSender().getLocalName() + " à la liste des agents preneurs.");
             //   return FishMarketPerformatif.TO_BID;
            }
            else {
           //     return FishMarketPerformatif.REP_BID_OK;
            }
        }
    }

    /**
     * <b>AttenteAutresOffres</b> attend d'autres offre de potentiels preneurs.
     * <ul>
     *     <li>S'il en revoit d'autres</li>
     * </ul>
     */
    // TODO : mettre un compteur ?
    private class AttenteAutresOffres extends OneShotBehaviour {
        @Override
        public void action() {
            logger.info("Arrivé dans la classe AttenteAutresOffres");
            MessageTemplate mt = MessageTemplate.MatchPerformative(FishMarketPerformatif.TO_BID); // PROPOSE
            ACLMessage msgReceived = myAgent.receive(mt);
            if (msgReceived != null) {
                agentsPreneur.add(msgReceived.getSender());
                logger.info("Ajout de " + msgReceived.getSender().getLocalName() + " à la liste des agents preneurs.");
            }
         //   return 0;
        }
    }


    /**
     * <b>Attribution</b> envoie un message TO_ATTRIBUTE au seul preneur qui a répondu à l'offre.
     */
    private class Attribution extends OneShotBehaviour {
        public void action() {
            logger.info("Arrivé dans la classe Attribution.");
            ACLMessage msg = new ACLMessage(FishMarketPerformatif.TO_ATTRIBUTE); //ACCEPT_PROPOSAL
            // TODO : modifier new AID
            msg.addReceiver(agentsPreneur.get(0));
            send(msg);
          //  return FishMarketPerformatif.TO_ATTRIBUTE;
        }
    }

    /**
     * <b>AttentePaiement</b> vérifie que le preneur confirme bien le message ACCEPT_PROPOSAL que le vendeur lui a envoyé
     */
    private static class AttentePaiement extends OneShotBehaviour {
        public void action() {
            logger.info("Arrivé dans la classe ReceivedConfirm.");
            MessageTemplate mt = MessageTemplate.MatchPerformative(FishMarketPerformatif.TO_PAY); //CONFIRM
            ACLMessage msg = myAgent.receive(mt);
            if (msg == null) {
                block();
            }
           // return FishMarketPerformatif.TO_PAY;
        }
    }

    /**
     * <b>SendAgree</b> envoie un message AGREE au marché avec comme contenu le poisson et supprime l'agent
     */
    private class Livraison extends OneShotBehaviour {
        public void action() {
            logger.info("Arrivé dans la classe SendAgree.");
            // Envoie le message AGREE (TO_GIVE) au marché pour l'informer de la fin de l'enchère
            ACLMessage msg = new ACLMessage(FishMarketPerformatif.TO_GIVE); //AGREE
            // TODO : mettre le nom de l'enchère pour récupérer le nom du poisson
            msg.setContent("poisson");
            msg.addReceiver(new AID("market", AID.ISLOCALNAME));
            send(msg);
            doDelete();
         //   return 0;
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
        gui.dispose();

        logger.info("Agent Vendeur " + getName() + " done.");
    }

}
