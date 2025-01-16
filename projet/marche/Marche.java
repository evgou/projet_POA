package marche;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import vendeur.Vendeur;
import vendeur.VendeurGUI;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class Marche extends jade.domain.df {
    private static final Logger logger = Logger.getLogger(Vendeur.class.getName());
    private MarcheGUI gui;

    private List<String> vendeurs;
    private List<String> preneurs;
    private List<String> offres;

    private final String serviceType = "fishmarket";

    @Override
    protected void setup() {
        logger.info("Agent" + getName() + " trying to subscribe for services of type " + serviceType);
        vendeurs = new ArrayList<>();
        preneurs = new ArrayList<>();
        offres = new ArrayList<>();

        Object[] args = getArguments();
        // Vérifie s'il y a les arguments nécessaires
        if (args != null && args.length > 1) {

            gui = new MarcheGUI(this);
            gui.show();
        }

        // Build the description used as template for subscription

        private DFAgentDescription getDescription() {
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();

            dfd.setName(getAID());
            dfd.addServices(FIPANames.InteractionProtocol.FIPA_REQUEST);
            dfd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
            dfd.addOntologies("fish-auction-ontology");

            // A "fipa-df" service is mandatory for the df federation
            sd.setName(getLocalName() + "fish-auction-df");
            sd.setType(serviceType);
            dfd.addServices(sd);
        }

        DFAgentDescription template = new DFAgentDescription;
        ServiceDescription templateSd = new ServiceDescription;
        templateSd.setType(serviceType);
        template.addServices(templateSd);

        // Subscription to services
        ACLMessage subs = DFService.createSubscriptionMessage(this, new AID ("market", AID.ISLOCALNAME), template, null);
    }




    @Override
    protected void onGuiEvent(GuiEvent ev) {
        logger.info("Commande reçue depuis l'IHM : " + ev.getAllParameter());
    }

    @Override
    protected void takeDown() {
        gui.dispose();
        logger.info("Agent "+ getName()+ " terminating.");
    }
}
