package marche;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.util.logging.Logger;


public class Marche {
    String serviceType = "Fishmarket";
    logger.info("Agent" + myAgent.getLocalName + " trying to subscribe for services of type " + serviceType);

    // Build the description used as template for subscription
    DFAgentDescription template = new DFAgentDescription;
    ServiceDescription templateSd = new ServiceDexcription;
    templateSd.setType(ServiceType);
    template.addService(templateSd);

    // Subscription to services
    ACLMessage subs = DFService.createSubscriptionMessage(myAgent, new AID ("market", AID.ISLOCALNAME), template, null);
}
