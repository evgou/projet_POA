package vendeur;

import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;

import java.util.List;
import java.util.ArrayList;

public class Vendeur2 extends FSMBehaviour {

    private List<FSMBehaviour> preneurs = new ArrayList<FSMBehaviour>();

    public Vendeur2(Agent a) {
        super(a);
    }

}

