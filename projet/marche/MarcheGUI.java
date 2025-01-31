package marche;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import javax.swing.*;
import javax.swing.plaf.metal.MetalIconFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;


public class MarcheGUI extends JFrame {
    private final Marche agent;
    private JTextArea logArea;
    private static final Logger logger = Logger.getLogger(MarcheGUI.class.getName());


    public MarcheGUI(Marche agent) {
        this.agent = agent;

        setTitle(agent.getLocalName());
        setSize(1000, 300);
        //setIconImage(Toolkit.getDefaultToolkit().getImage("/home/alicia/Documents/M2/POA/projet_POA/projet/misc/images/poisson.png"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        logger.info("Je suis là");


        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

    }

    public void updateTable() {
        // TODO : implémenter la mise à jour
    }

    public void updateLog(String message) {
        logArea.append(message + "\n");
    }

    public void showGui() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

}
