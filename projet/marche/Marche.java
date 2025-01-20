package marche;

import jade.core.Agent;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

public class Marche extends GuiAgent {
    protected void setup(){
        System.out.println("Agent " + getAID().getName() + " started.");
    }

    @Override
    protected void onGuiEvent(GuiEvent ev) {
        System.out.println("Helloooooooooooooooo, je suis dans ke Marchéééééé !")
    }
}
