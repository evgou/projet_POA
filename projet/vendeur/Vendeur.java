package POA.projet_POA.projet.vendeur;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.util.logging.Logger;


public class Vendeur extends Agent{
    private static final Logger logger = Logger.getLogger(AgentInitiateur.class.getName());

    /**
     * Setup :
     */
    protected void setup() {
        logger.info("Agent Vendeur " + this.getName() + " is ready.");
        // Register the fishmarket service in the yellow pages
        DFAgentDescription template = new DFAgentDescription();
        template.setName(getAID());
        template.addServices(sd);
        try {
            DSFAgentDescription[] result = DFService.search(myAgent, template);
            sellerAgents = new AID[result.lenght];
            for (int i = 0; i < result.length; i++) {
                sellerAgents[i] = result[i].getName();
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        sd = new ServiceDescription();
        sd.setName(serviceName);
        sd.setType("Fishmarket");
        // Agents that want to use this service need to "know" the fish-auction-ontology
        sd.addOntologies("fish-auction-ontology");
        // Agents that want to use this service need to "speak" the FIPA-SL language
        sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
        // TODO terminer le code
        dfd.addServices(sd);
        DFService.register(this, new AID("market", AID.ISLOCALNAME), dfd);
    }
    private static ACLMessage sendRequest(String message) {
        ACLMessage request = new ACLMessage(ACLMessage.CFP);
        request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        request.addReceiver(new AID("A1", AID.ISLOCALNAME));
        request.setContent(message);
        return request;
    }

    protected void takeDown() {
        // Demande au DF de supprimer les services qui ont été inscrits par l'agent
        // Envoie le messgae TO_GIVE au marché pour l'en informer

        logger.info("Agent Vendeur " + this.getName() + " done.");
    }

}
