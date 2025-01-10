package marche;

import jade.core.Agent;
import jade.gui.GuiAgent;

public class Marche extends Agent {
    protected void setup(){
        System.out.println("Agent " + getAID().getName() + " started.");
    }
}
