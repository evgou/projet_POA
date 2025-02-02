package preneur;

import jade.core.AID;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionInitiator;
import misc.FishMarketPerformatif;
import misc.Prix;
import jade.domain.DFService;
import java.util.Vector;
import java.util.logging.Logger;


public class PreneurAgent extends GuiAgent {

	private String myName;
	private float _budget;
	private float fondCourant;
	private static final Logger logger = Logger.getLogger(PreneurAgent.class.getName());

	protected void setup() {
		System.out.println("Agent " + getAID().getLocalName() + " started !");

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			myName = (String) args[0];
			PreneurAgentGUI gui = new PreneurAgentGUI(this);
			gui.showGui();

			try {
				// Envoie du message pour signaler sa présence à l'agent Marché
				ACLMessage msg = new ACLMessage(FishMarketPerformatif.TO_ANNOUNCE);
				msg.addReceiver(new AID("market", AID.ISLOCALNAME));
				msg.setContent("1");
				send(msg);
				logger.info("ACLMessage : " + msg.getPerformative());
			}
			catch (Exception e) {
				logger.severe("Erreur du message de " + getAID().getLocalName() + " : " + e.getMessage());
			}

			// Dans la méthode setup() de PreneurAgent
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("Fishmarket");
			dfd.addServices(sd);

			// Création du message d'abonnement au DF
			ACLMessage subscriptionMsg = DFService.createSubscriptionMessage(this, getDefaultDF(), dfd, new SearchConstraints());

			addBehaviour(new SubscriptionInitiator(this, subscriptionMsg) {
				@Override
				protected void handleInform(ACLMessage inform) {
					try {
						DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
						for (DFAgentDescription dfd : results) {
							AID vendeur = dfd.getName();
							// TODO : Trouver comment ajouter les offres à l'IHM
							gui.addOffer(vendeur.getLocalName(), "test", 40);
						}
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}
				}
			});

			this.automate();
		}
		else {
			// Make the agent terminate
			System.out.println("No name specified");
			doDelete();
		}
	}

	protected void takeDown() {
		System.out.println("Agent "+getAID().getName()+" terminating.");
	}

	protected void automate(){
		FSMBehaviour automatePreneur = new FSMBehaviour(){
			@Override
			public int onEnd() {
				logger.info(getAID().getName() + "-------------------------> Behaviour completed.");
				logger.info("Fin de l'enchère.");
				logger.info("Agent Vendeur " + getAID().getName() + " terminating.");
				return super.onEnd();
			}
		};

		//On définit les états de l'agent Preneur
		automatePreneur.registerFirstState(new departBehaviour(), "Départ");
		automatePreneur.registerState(new toBidBehaviour(), "To Bid");
		automatePreneur.registerState(new repBidBehaviour(), "Rep Bid");
		automatePreneur.registerState(new attributeBehaviour(), "Attribute");
		automatePreneur.registerState(new toPayBehaviour(), "To Pay");
		automatePreneur.registerState(new toGiveBehaviour(), "To Give");

		//On définit les transitions de l'agent Preneur
		automatePreneur.registerTransition("Départ", "To Bid", 1);
		automatePreneur.registerTransition("To Bid", "To Bid", 2);

	}

	private class departBehaviour extends OneShotBehaviour{
		public void action(){
			logger.info("action du depart behaviour");
			ACLMessage msg = myAgent.receive();
			if(msg != null && msg.getPerformative() == FishMarketPerformatif.TO_ANNOUNCE){
				logger.info(getAID().getName() + " a reçu une offre");
				try {
					Prix prix = (Prix) msg.getContentObject();
				} catch (UnreadableException e) {
					logger.severe("Erreur du message de " + getAID().getName() + " : " + e.getMessage());
					throw new RuntimeException(e);
				}

			}
		}
		public int onEnd(){
			return 1;
		}
	}

	private class toBidBehaviour extends OneShotBehaviour{
		public void action(){
			logger.info("action du to bid behaviour");
		}
		public int onEnd(){
			return 2;
		}
	}

	private class repBidBehaviour extends OneShotBehaviour{
		public void action(){
			logger.info("action du rep bid behaviour");
		}
		public int onEnd(){
			return 3;
		}
	}

	private class attributeBehaviour extends OneShotBehaviour{
		public void action(){
			logger.info("action du attribute behaviour");
		}
		public int onEnd(){
			return 4;
		}
	}

	private class toPayBehaviour extends OneShotBehaviour{
		public void action(){
			logger.info("action du to pay behaviour");
		}
		public int onEnd(){
			return 5;
		}
	}

	private class toGiveBehaviour extends OneShotBehaviour{
		public void action(){
			logger.info("action du to give behaviour");
		}
		public int onEnd(){
			return 6;
		}
	}

	@Override
	protected void onGuiEvent(GuiEvent ev) {
		logger.info("Commande reçue depuis l'IHM : " + ev.getAllParameter());
	}

	// Méthode pour afficher des messages sur l'IHM
	public void log(String message) {
		logger.info(message);
	}

	public void placeBid(String vendeur) {
		logger.info(this.myName + " est dans placeBid : " + vendeur);
	}

	public void onSelectionValidated(Vector<String> offres, boolean isSelected, float budget) {
		this._budget = budget;
		logger.info(this.myName + " est dans onSelectionValidated : " + isSelected);
	}
}