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
import misc.FishMarketPerformatif;
import vendeur.Vendeur;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


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
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        enregistrementService();




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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class EvolutionPrixEnchere extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(FishMarketPerformatif.TO_ANNOUNCE);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String price = msg.getContent();
                String vendeur = msg.getSender().getLocalName();
                logger.info("Nouveau prix pour " + vendeur + " de " + price + " €.");
                int indexVendeur = vendeurs.indexOf(vendeur);
                if (indexVendeur != -1) {
                    offres.set(indexVendeur, price);
                } else {
                    vendeurs.add(vendeur);
                    offres.add(price);
                }
            } else {
                block();
            }
        }
    }


    protected void onGuiEvent(GuiEvent ev) {
        logger.info("Commande reçue depuis l'IHM : " + ev.getAllParameter());
    }

    @Override
    protected void takeDown() {
        gui.dispose();
        logger.info("Agent "+ getName()+ " terminating.");
    }
}
