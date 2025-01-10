package preneur;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import java.util.logging.Logger;

public class PreneurAgent extends GuiAgent {

	private String myName;
	private static final Logger logger = Logger.getLogger(PreneurAgent.class.getName());

	protected void setup() {
		System.out.println("Agent " + getAID().getLocalName() + " started !");

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			myName = (String) args[0];
			//gui = new

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

	@Override
	protected void onGuiEvent(GuiEvent ev) {
		logger.info("Commande reçue depuis l'IHM : " + ev.getAllParameter());
	}

	// Méthode pour afficher des messages sur l'IHM
	public void log(String message) {
		logger.info(message);
	}
}




/*

************************************************************************************************************************
*																													   *
*						Squelette initial de ce que pourrait être le preneur                                           *
*						(basé sur le TP3)                                                                              *
*						                                                                                               *
************************************************************************************************************************
package preneur;


import jade.core.Agent;
//import jade.core.AID;
import jade.core.behaviours.*;
import jade.gui.GuiAgent;


public class Preneur extends GuiAgent {
	// Define agent properties here
	private String myName;
	private int helloCount = 0;

	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		System.out.println("Agent " + getAID().getName() + " started.");

		// Get the name of the agent as a start-up argument
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			myName = (String) args[0];

			FSMBehaviour fsm = new FSMBehaviour();

			// definition des états
			fsm.registerFirstState (new WaitAnnounce(), "First");
			fsm.registerState(new PrintHelloWorld(), "Hello");
			fsm.registerState(new OneMoreTime(), "OneMore");
			fsm.registerLastState(new ByeWorld(),"Bye");

			// definition des transactions
        	fsm.registerDefaultTransition("First", "Hello");
			fsm.registerTransition("Hello", "Hello", 1);
			fsm.registerTransition("Hello", "OneMore", 0);
			fsm.registerTransition("OneMore", "Hello", 1);
        	fsm.registerTransition("Hello", "Bye", 2);

			addBehaviour(fsm);
			
		}
		else {
			// Make the agent terminate
			System.out.println("No name specified");
			doDelete();
		}
	}

}
*/