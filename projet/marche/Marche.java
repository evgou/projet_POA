package marche;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import vendeur.Vendeur;

import java.util.logging.Logger;


public class Marche {
    private static final Logger logger = Logger.getLogger(Vendeur.class.getName());

    private String serviceType = "Fishmarket";
    protected void setup() {
        logger.info("Agent" + myAgent.getLocalName + " trying to subscribe for services of type " + serviceType);

        // Build the description used as template for subscription
        DFAgentDescription template = new DFAgentDescription;
        ServiceDescription templateSd = new ServiceDescription;
        templateSd.setType(serviceType);
        template.addService(templateSd);

        // Subscription to services
        ACLMessage subs = DFService.createSubscriptionMessage(myAgent, new AID ("market", AID.ISLOCALNAME), template, null);
    }

}
