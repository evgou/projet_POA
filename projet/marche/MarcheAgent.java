package marche;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class MarcheAgent extends jade.domain.df {
    private static final Logger logger = Logger.getLogger(MarcheAgent.class.getName());
    private MarcheAgentGUI gui;

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
        gui = new MarcheAgentGUI(this);
        gui.showGui();

        addBehaviour(new RegisterAgents(this, 2000));

        // TODO : continuer le code

        // TODO : faire le register
        // TODO : faire un "deregister" (ou "unregister" plutot ?) quand une enchère est finie : Permet aux agents de se désinscrire
        // TODO : modify : Permet de modifier un enregistrement existant
        // TODO : search : Permet de rechercher des services ou des agents


    }

    private class RegisterAgents extends WakerBehaviour {

        public RegisterAgents(Agent a, long timeout) {
            super(a, timeout);
        }

        protected void onWake() {
            logger.info("On se réveille");
            ACLMessage msg = myAgent.receive();
            while (msg != null) {
                System.out.println("On a reçu un message : " + msg.getContent());
                if (msg.getPerformative() == ACLMessage.INFORM) {
                    logger.info("On est dans le second if");
                    if (msg.getContent().contentEquals("0")) {
                        vendeurs.add(msg.getSender().getLocalName());
                        logger.info("Liste des vendeurs : " + vendeurs);
                    }
                    if (msg.getContent().contentEquals("1")) {
                        preneurs.add(msg.getSender().getLocalName());
                        logger.info("Liste des preneurs : " + preneurs);
                    }
                }
                msg = myAgent.receive();
            }
        }
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
        sd.setType(serviceType);
        dfd.addServices(sd);
        return dfd;
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
