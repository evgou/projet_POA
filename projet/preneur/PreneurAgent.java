package preneur;

import com.sun.org.apache.xpath.internal.objects.XString;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

public class PreneurAgent extends GuiAgent {

	private String myName;

	protected void setup() {

	}

	@Override
	protected void onGuiEvent(GuiEvent ev) {

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

			// definition des etats
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