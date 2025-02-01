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
import misc.FishMarketPerformatif;
import vendeur.Vendeur;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class Marche extends jade.domain.df {
    private static final Logger logger = Logger.getLogger(Vendeur.class.getName());
    private MarcheGUI gui;



    // Liste des offres / vendeurs (1 offre par vendeur)
    public static Map<String, Offre> offres = new HashMap<>();
    public static AID[] preneurs = new AID[5];
    // public static String[] offres =  new String[5];

    @Override
    protected void setup() {

        /*
        for (int i = 0; i < 5; i++) {
            vendeurs[i] = null;
            preneurs[i] = null;
            offres[i] = "";
        }
        */

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

        //enregistrementService();

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
        sd.setType("Fishmarket");
        dfd.addServices(sd);
        return dfd;
    }

    private void enregistrementService() {
        try {
            DFAgentDescription dfd = getDescription();
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class EvolutionPrixEnchere extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            logger.info("Reception du message : " + (msg == null ? "null" : msg.getContent()));
            if (msg != null) {
                switch(msg.getPerformative()) {
                    case ACLMessage.SUBSCRIBE:
                        enregistrementPreneur(msg.getSender());
                        AID preneur = msg.getSender();
                        logger.info("Le preneur " + preneur.getName() + " veut s'abonner.");
                        // TODO
                        //for (AID vendeur : vendeurs) {
                        //    envoyerSubscribeVendeur(vendeur, preneur);
                        //}
                        break;

                    case FishMarketPerformatif.TO_ANNOUNCE:
                        AID vendeurAID = msg.getSender();
                        String price = msg.getContent();
                        offres.put(vendeurAID.getName(), new Offre(vendeurAID, price));
                        logger.info("Enregistrement de l'offre pour " + vendeurAID.getName());
                        gui.updateTable();

                        // TODO A SUPPRIMER APR7S RECEPTION PRENEUR
                        if (Integer.parseInt(price) < 50) {
                            ACLMessage sub = new ACLMessage(ACLMessage.SUBSCRIBE);
                            sub.addReceiver(vendeurAID);
                            sub.ad
                            send(sub);
                        }
                        break;
                }
            } else {
                block();
            }
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
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Fishmarket");

            DFAgentDescription[] result = DFService.search(this, dfd);
            StringBuilder listeEncheres = new StringBuilder();

            for (DFAgentDescription desc : result) {
                listeEncheres.append(desc.getName().getLocalName()).append(" ");
            }

            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.addReceiver(preneur);
            reply.setContent(listeEncheres.toString());
            send(reply);

            logger.info("Liste des enchères envoyée à " + preneur.getLocalName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void enregistrementPreneur(AID preneur)  {
        for (int i = 0; i < preneurs.length; i++) {
            if (preneurs[i] == null) {
                preneurs[i] = preneur;
                logger.info("Nouveau preneur : " + preneur);
                envoieListeEnchere(preneur);
                return;
            }
        }
    }

    /**
     * Dans cette classe, les vendeurs sont stockés dans le tableau <b>vendeurs</b> sous forme d'AID.

    private void majListVendeur() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Fishmarket");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            vendeurs = new AID[result.length];
            for (int i = 0; i < result.length; i++) {
                vendeurs[i] = result[i].getName();
                logger.info("Enchère reçue " + vendeurs[i]);
            }
            miseAJourGUI();
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        logger.info("Mise à jour du tableau des vendeurs.");
    }

    private void enregistrementOffre(AID vendeur, String offre) {
        boolean vendeurTrouve = false;
        for (int i = 0; i < vendeurs.length; i++) {
            if (vendeurs[i].equals(vendeur)) {
                offres[i] = offre;
                logger.info("Nouvelle offre pour " + vendeur + " au prix de " + offre);
                vendeurTrouve = true;
                break;
            }
        }
        if (!vendeurTrouve) {
            for (int i = 0; i < vendeurs.length; i++) {
                if (vendeurs[i] == null) {
                    vendeurs[i] = vendeur;
                    offres[i] = offre;
                    logger.info("Nouveau vendeur : " + vendeur.getLocalName() + " ajouté avec une offre à " + offre);
                    vendeurTrouve = true;
                    break;
                }
            }

        }
        gui.updateTable(vendeur, offre);
    }
     */

    private void envoyerSubscribeVendeur(AID vendeur, AID preneur) {
        ACLMessage subs = new ACLMessage(ACLMessage.SUBSCRIBE);
        subs.addReceiver(vendeur);
        subs.setContent("Abonnement du preneur " + preneur.getName());
        send(subs);
        logger.info("Message SUBSCRIBE envoyé à " + vendeur.getName() + " de " + preneur.getName());
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
