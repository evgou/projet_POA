package marche;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionInitiator;
import misc.FishMarketPerformatif;
import vendeur.Vendeur;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static jade.domain.DFService.createSubscriptionMessage;


public class Marche extends jade.domain.df {
    private static final Logger logger = Logger.getLogger(Vendeur.class.getName());
    private MarcheGUI gui;

    private List<String> vendeurs;
    private List<String> preneurs;
    private List<String> offres;

    @Override
    protected void setup() {
        vendeurs = new ArrayList<>();
        preneurs = new ArrayList<>();
        offres = new ArrayList<>();

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

        enregistrementService();

        addBehaviour(new SubscriptionInitiator(this, createSubscriptionMessage()) {
            protected void handleInform(ACLMessage inform) {
                AID preneur = inform.getSender();
                logger.info("Preneur abonné : " + preneur.getLocalName());

                // Envoyer la liste des enchères disponibles
                EnvoieListeEnchere(preneur);
            }
        });

        addBehaviour(new EvolutionPrixEnchere());


        // TODO : continuer le code

        // TODO : faire le register
        // TODO : faire un "deregister" (ou "unregister" plutot ?) quand une enchère est finie : Permet aux agents de se désinscrire
        // TODO : modify : Permet de modifier un enregistrement existant
        // TODO : search : Permet de rechercher des services ou des agents


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
        sd.setType("fishmarket");
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

    private void EnvoieListeEnchere(AID preneur) {
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


    private void enregistrementPreneur(String preneur)  {
        preneurs.add(preneur);
        logger.info("Nouveau preneur : " + preneur);
        // TODO : envoie des enchères courantes au preneur
    }

    private void enregistrementVendeur(String vendeur) {
        vendeurs.add(vendeur);
        logger.info("Nouveau vendeur : " + vendeur);
    }

    private void enregistrementOffre(String offre) {
        offres.add(offre);
        logger.info("Nouveau offre à : " + offre + " €.");
    }


    private class EvolutionPrixEnchere extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                switch(msg.getPerformative()) {
                    case ACLMessage.SUBSCRIBE:
                        enregistrementPreneur(msg.getSender().getLocalName());
                        break;
                    case FishMarketPerformatif.TO_ANNOUNCE:
                        enregistrementVendeur(msg.getSender().getLocalName());
                        String content = msg.getContent();
                        enregistrementOffre(content);
                }
            } else {
                block();
            }
        }
    }

    public void miseAJourGUI() {
        gui.updateTable(vendeurs, offres);
    }

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
