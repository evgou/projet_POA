package preneur;

import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import java.util.logging.Logger;

// TODO : Pour plus de compréhension, faire des constantes avec les états : TO_ANNOUNCE 1 etc...

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
		FSMBehaviour automatePreneur = new FSMBehaviour();

		//On définit les états de l'agent Preneur
		automatePreneur.registerFirstState(new departBehaviour(), "Départ");
		automatePreneur.registerState(new toBidBehaviour(), "To Bid");
		automatePreneur.registerState(new repBidBehaviour(), "Rep Bid");
		automatePreneur.registerState(new attributeBehaviour(), "Attribute");
		automatePreneur.registerState(new toPayBehaviour(), "To Pay");
		automatePreneur.registerState(new toGiveBehaviour(), "To Give");

		//On définit les transitions de l'agent Preneur
		automatePreneur.registerTransition("Départ", "To Bid", 1);

	}

	private class departBehaviour extends OneShotBehaviour{
		public void action(){
			logger.info("action du depart behaviour");
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
}