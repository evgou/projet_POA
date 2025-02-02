package marche;

import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import misc.FishMarketPerformatif;
//import preneur.Preneur;
import vendeur.Vendeur;

import javax.swing.*;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.Integer.valueOf;


public class Marche extends jade.domain.df {
    private static final Logger logger = Logger.getLogger(Vendeur.class.getName());
    private MarcheGUI gui;
    //private Map<AID, Prix> vendeursAgents = new HashMap<>();
    private AID vendeur;
    private Set<AID> preneurAgents = new HashSet<>();


    // Liste des offres / vendeurs (1 offre par vendeur)
    public static Map<String, Prix> encheres = new HashMap<>();
    //public static Map<String, AID> preneurs = new HashMap<>();

    @Override
    protected void setup() {

        gui = new MarcheGUI(this);
        gui.showGui();

        try {
            AID parentName = getDefaultDF();

            // Execute the setup of jade.domain.df which includes all the default behaviours of a df
            // (i.e. register, unregister, modify and search)
            super.setup();

            // Use this method to modify the current description of this df
            setDescriptionOfThisDF(getDescription());

            // Register and federate this df with de default df
            // to be registered as a child df, agent's description must declare a "fipa-df" service
            DFService.register(this, parentName, getDescription());
            //register the parent df
            addParent(parentName, getDescription());

            // Show the default gui of this DF
            showGui();

        } catch (Exception e) {
            e.printStackTrace();
        }

        envoieSubscribeVendeur();

        addBehaviour(new EvolutionPrixEnchere());
        addBehaviour(new GestionFinEnchere());

    }


    private DFAgentDescription getDescription() {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        dfd.setName(getAID());
        dfd.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
        dfd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);

        dfd.addOntologies("fish-auction-ontology");

        //  A "fipa-df" service is mandatory for the df federation
        sd.setName(getLocalName() + "fish-auction-df");
        sd.setType("fipa-df");
        dfd.addServices(sd);
        return dfd;
    }


    private void searchServiceDescription(AID aid) {
        logger.info("Rentrer dans searchServiceDescription");
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Fishmarket");  // Recherche uniquement les vendeurs enregistrés sous "Fishmarket"
            template.addServices(sd);
            SearchConstraints sc = new SearchConstraints();
            sc.setMaxResults(-1L);

            DFAgentDescription[] result = DFService.search(this, template, sc);
            logger.info("Nombre de vendeurs trouvés : " + result.length);


            for (DFAgentDescription dfd : result) {
                AID vendeur = dfd.getName();  // Récupère l'AID du vendeur
                for (jade.util.leap.Iterator it = dfd.getAllServices(); it.hasNext(); ) {
                    ServiceDescription service = (ServiceDescription) it.next();
                    logger.info("Vendeur " + vendeur.getName() + " offre le service : " + service.getName());
                }
            }
        } catch (FIPAException e) {
            throw new RuntimeException(e);
            //e.printStackTrace();
        }
    }


    private class EvolutionPrixEnchere extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            //logger.info("Reception du message : " + (msg == null ? "null" : msg.getContent()));
            if (msg != null) {

                //searchServiceDescription(msg.getSender());
                switch (msg.getPerformative()) {

                    case FishMarketPerformatif.TO_ANNOUNCE:
                        try {
                            logger.info("Contenu du message : " + msg.getContentObject());
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }
                        AID vendeurAID = msg.getSender();
                        vendeur = vendeurAID;
                        Prix prix = null;
                        try {
                            prix = (Prix) msg.getContentObject();
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }
                        encheres.put(vendeurAID.getName(), prix);
                        logger.info("Enregistrement de l'offre pour " + vendeurAID.getName());
                        gui.updateTable();
                        envoieOffrePeneurs(vendeurAID, prix);
                        break;

                    case ACLMessage.SUBSCRIBE:
                        AID preneur = msg.getSender();
                        logger.info("Le preneur " + preneur.getName() + " veut s'abonner.");
                        preneurAgents.add(preneur);
                        logger.info("Liste des preneurs : " + preneurAgents);
                        envoieSubscribeVendeur();
                        break;
                }
            } else {
                block();
            }
        }
    }

    private void envoieOffrePeneurs(AID vendeurAID, Prix prix) {
        for (AID preneur : preneurAgents) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(preneur);
            msg.setContent(String.valueOf(prix));
            send(msg);
        }
    }


    /**
     * <b>GestionFinEnchere</b> permet de supprimer une enchère du marché si celle-ci a été attribuée
     * (reception du messgae <i>TO_GIVE</i> de la part du vendeur).
     */
    private class GestionFinEnchere extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(FishMarketPerformatif.TO_GIVE));
            if (msg != null) {
                AID vendeur = msg.getSender();
                SuppressionEnchere(vendeur);
            } else {
                block();
            }
        }
    }


    private void envoieListeEnchere(AID preneur) {

        List listEncheres = new ArrayList();
        for (Map.Entry<String, Prix> offre : encheres.entrySet()) {
            listEncheres.add(offre.getKey());
        }

        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        reply.addReceiver(preneur);
        reply.setContent(listEncheres.toString());
        send(reply);

        logger.info("Liste des enchères envoyée à " + preneur.getLocalName());
    }

    private void envoieSubscribeVendeur() {
        ACLMessage subs = new ACLMessage(ACLMessage.SUBSCRIBE);
        subs.addReceiver(new AID ("Vincent", AID.ISLOCALNAME));
        subs.setContent(preneurAgents.toString());
        send(subs);
        logger.info("Message SUBSCRIBE envoyé à " + new AID ("Vincent", AID.ISLOCALNAME));
    }

    public void miseAJourGUI() {
        SwingUtilities.invokeLater(() -> {
            // TODO : à faire
        });
    }

    /**
     * Est appelé dans la classe <b>GestionFinEnchere</b> afin de dé-enregistrer l'agent vendeur
     * @param vendeur : agent qui a terminé son enchère
     */
    public void SuppressionEnchere(AID vendeur) {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(vendeur);
            DFService.deregister(this, dfd);
            // TODO : modifier l'affichage graphique

            logger.info("Enchère de " + vendeur.getLocalName() + " terminée.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onGuiEvent(GuiEvent ev) {
        logger.info("Commande reçue depuis l'IHM : " + ev.getAllParameter());
    }

    @Override
    protected void takeDown() {
        gui.dispose();
        logger.info("Agent " + getName() + " terminating.");
    }
}
