package vendeur;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.logging.Logger;

/**
 * On appelle cet agent (vendeur) avec en argument son nom et le nom du poisson de l'enchère
 */
public class Vendeur extends Agent {
    private static final Logger logger = Logger.getLogger(Vendeur.class.getName());
    private AID preneur = null;
    // Valeur du prix et du pas par défaut pour le moment
    // TODO : modifier
    private int price = 1000;
    private int pas = 50;

    /**
     * setup : créé l'automate FSM
     */
    protected void setup() {
        logger.info("Agent Vendeur " + this.getName() + " is ready.");

        Object[] args = getArguments();
        // Vérifie s'il y a les arguments nécessaires
        if (args != null && args.length > 1) {
            // FSM Behaviour
            FSMBehaviour fsm = new FSMBehaviour(this) {
                public int onEnd() {
                    logger.info(getAID().getName() + "-------------------------> Behaviour completed.");
                    logger.info("Agent Vendeur " + getAID().getName() + " terminating.");
                    return super.onEnd();
                }
            };

            // Définition des états
            fsm.registerFirstState (new SendPrice(), "CFP");
            fsm.registerState(new WaitForMsg(this, 20000), "Wait");
            fsm.registerState(new ReceivedPropose(), "Propose");
            fsm.registerState(new HandleRefuse(), "Refuse");
            fsm.registerState(new SendInform(), "Inform");
            fsm.registerState(new SendAcceptProposal(), "Accept_proposal");
            fsm.registerState(new ReceivedConfirm(), "Confirm");
            fsm.registerLastState(new SendAgree(), "Agree");

            // Définition des transactions
            fsm.registerDefaultTransition("CFP", "Wait");
            fsm.registerTransition("Wait", "CFP", 1);
            fsm.registerTransition("Wait", "Propose", 2);
            fsm.registerTransition("Propose", "Refuse", 3);
            fsm.registerTransition("Propose", "Inform", 4);
            fsm.registerTransition("Refuse", "CFP", 5);
            fsm.registerDefaultTransition("Inform", "Accept_proposal");
            fsm.registerDefaultTransition("Accept_proposal", "Confirm");
            fsm.registerDefaultTransition("Confirm", "Agree");

            addBehaviour(fsm);


            // TODO myGui =



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
                DFService.register(this, new AID("market", AID.ISLOCALNAME), dfd);
            }
            catch (FIPAException fe) {
                fe.printStackTrace();
            }

            // Gestion de l'abonnement des preneurs



        }
        else {
            // Termine l'agent
            System.out.println("Pas de nom de poisson spécifié.");
            doDelete();
        }
    }

    // Définiton de tous les états possibles de l'automate

    /**
     * SendPrice envoie le prix de base au marché
     */
    private class SendPrice extends OneShotBehaviour {
        public void action() {
            // TODO : envoie de l'enchère au marché avec un message CFP qui contient le prix
            logger.info("Arrivé dans la classe SendPrice.");
            ACLMessage msg = new ACLMessage(ACLMessage.CFP);
            msg.setContent(String.valueOf(price));
            msg.addReceiver(new AID("market", AID.ISLOCALNAME));
            send(msg);
        }
    }

    /**
     * <b>WaitForMsg</b> vérifie si après passé un délai de temps, il recoit des messages ou pas.
     *   - si reception d'un message, on passe à l'état "Propose" qui gèrera le nombre de messages reçus
     *   - sinon, on baisse le prix en fonction du pas et on renvoit le nouveau prix
     */
    private class WaitForMsg extends WakerBehaviour {
        public WaitForMsg(Agent a, long timeout) {
            super(a, timeout);
        }

        @Override
        protected int onWake() {
            logger.info("Arrivé dans la classe WaitForMsg");
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                preneur = msg.getSender();
                return 2;
            }
            else {
                price = price - pas;
                return 1;
            }
        }
    }

    /**
     * <b>ReceivedPropose</b> recoit le premier message et est en attente d'autres messages
     *   - si d'autres messages sont reçus, on passe à l'état "Refuse
     *   - si aucun autre message n'est reçu, c'est le seul preneur qui a fait une offre qui gagne l'enchère,
     *   on va donc vers l'état SendInform pour l'en informer
     */
    private class ReceivedPropose extends OneShotBehaviour {
        public void action() {
            logger.info("Arrivé dans la classe ReceivedPropose.");
            // Vérification si un deuxième message est arrivé
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                return 3;
            }
            else {
                return 4;
            }
        }
    }

    /**
     * <b>HandleRefuse</b> augmente le prix et repasse à l'état CFP pour renvoyer de nouveaux messages
     * // TODO : warning : faire attention car il faut le renvoyer aux preneux abonnés
     *
     */
    private class HandleRefuse extends OneShotBehaviour {
        public void action() {
            logger.info("Arrivé dans la classe HandleRefuse.");
            // Augmentation du prix car plusieurs preneurs
            price = price + pas;
            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            // TODO : implémenter la liste agentsPreneur
            for (int i = 0; i < agentsPreneur.lenght; i++) {
                cfp.addReceiver(agentsPreneur[i]);
            }
            cfp.setContent(String.valueOf(price));
            myAgent.send(cfp);

            return 0;
            // TODO : doit retourner à l'état Wait
        }
    }


    /**
     * <b>SendInform</b> : récupère l'agent preneur qui a "gagné" les enchère et achetra donc le poisson
     * et envoie un message INFORM à cet agent preneur (pour REP_BID_OK)
     */
    private class SendInform extends OneShotBehaviour {
        public void action() {
            logger.info("Arrivé dans la classe SendInform.");
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                ACLMessage msgInform = new ACLMessage(ACLMessage.INFORM);
                msgInform.addReceiver(preneur);
                send(msgInform);
            }
            else {
                block();
            }
        }
    }

    /**
     * <b>SendAcceptProposal</b> envoie un message ACCCEPT_PROPOSAL au preneur choisi
     */
    private class SendAcceptProposal extends OneShotBehaviour {
        public void action() {
            logger.info("Arrivé dans la classe SendAcceptProposal.");
            ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
            msg.addReceiver(new AID("preneur", AID.ISLOCALNAME));
            send(msg);
        }
    }

    /**
     * <b>ReceivedConfirm</b> vérifie que le preneur confirme bien le message ACCEPT_PROPOSAL que le vendeur lui a envoyé
     */
    private class ReceivedConfirm extends OneShotBehaviour {
        public void action() {
            logger.info("Arrivé dans la classe ReceivedConfirm.");
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
            ACLMessage msg = myAgent.receive(mt);
            if (msg == null) {
                block();
            }
        }
    }

    /**
     * <b>SendAgree</b> envoie un message AGREE au marché avec comme contenu le poisson et supprime l'agent
     */
    private class SendAgree extends OneShotBehaviour {
        public void action() {
            logger.info("Arrivé dans la classe SendAgree.");
            // Envoie le message AGREE (TO_GIVE) au marché pour l'informer de la fin de l'enchère
            ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
            // TODO : mettre le nom de l'enchère pour récupérer le nom du poisson
            msg.setContent("poisson");
            msg.addReceiver(new AID("market", AID.ISLOCALNAME));
            send(msg);
            doDelete();
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
