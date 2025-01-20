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

/**
 * On appelle cet agent (vendeur) avec en argument son nom et le nom du poisson de l'enchère
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

        // TODO : envoie d'un message au marché pour se faire connaitre
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(market);
        msg.setContent("0");
        send(msg);


        Object[] args = getArguments();
        // Vérifie s'il y a les arguments nécessaires
        if (args != null && args.length > 1) {

            gui = new VendeurGUI(this);
            gui.showGui();
            agentsPreneur = new ArrayList<>();



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