package preneur;

import jade.core.Agent;
//import jade.core.AID;
import jade.core.behaviours.*;


public class Preneur extends Agent {
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

			System.out.println("Agent nickname : "+myName);
            System.out.println(getAID().getName() + "------------------------->Behaviour started.");

			FSMBehaviour fsm = new FSMBehaviour(this) {
				public int onEnd() {
					System.out.println(getAID().getName() + "------------------------->Behaviour completed.");
					System.out.println("Agent " + getAID().getName() + " terminating.");
					return super.onEnd();
				}
			};

			// definition des etats
			fsm.registerFirstState (new PrintHelloWorldFirst(), "First");
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

	

	/**
	 * Inner class OneShotBehaviour
	 */

	private class PrintHelloWorldFirst extends OneShotBehaviour {
		public void action() {
			System.out.println(getAID().getName()+" Hello World, first time !");
		}        
	}

	// Classe PrintHelloWorld -> affiche "Hello Wolrd !" plusieurs fois
    private class PrintHelloWorld extends OneShotBehaviour {
		public void action() {
			System.out.println(getAID().getName()+" Hello World !");
			helloCount++;
		}
		public int onEnd() {
            // Return 1 for the first two executions, then 0 to move to "C"
			// 
            if (helloCount < 2) {
				return 1;
			} else if (helloCount == 2) {
				return 0;
			} else {
				return 2;
			}
		}
	}

	// Classe OneMoreTime 
    private class OneMoreTime extends OneShotBehaviour {
		public void action() {
			System.out.println(getAID().getName()+" One time more...");
		}
		public int onEnd() {
			// Revenir à l'état "Hello" (grace au retour 1) pour continuer une dernière fois, puis aller à "Bye"
			return 1;
		}
	}

	// Classe ByeWorld -> termine l'agent
    private class ByeWorld extends OneShotBehaviour {
		public void action() {
			System.out.println(getAID().getName()+" Bye World...");
			doDelete();
		}
	}

}