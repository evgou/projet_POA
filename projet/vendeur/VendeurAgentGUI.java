package vendeur;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import javax.swing.*;
import javax.swing.plaf.metal.MetalIconFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;


public class VendeurAgentGUI extends JFrame {
    private final VendeurAgent agent;
    private JTextArea logArea;
    private JTextField commandField;
    private static final Logger logger = Logger.getLogger(VendeurAgentGUI.class.getName());


    public VendeurAgentGUI(VendeurAgent agent) {
        this.agent = agent;

        setTitle(agent.getLocalName());
        setSize(1000, 300);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel commandPanel = new JPanel();
        commandField = new JTextField(20);
        JButton sendButton = new JButton("Envoyer");
        sendButton.addActionListener((ActionEvent e) -> sendCommand());
        commandPanel.add(commandField);
        commandPanel.add(sendButton);

        add(commandPanel, BorderLayout.SOUTH);
    }

    public void updateLog(String message) {
        logArea.append(message + "\n");
    }

    public void showGui() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    private void sendCommand() {
        String command = commandField.getText();
        if (!command.isEmpty()) {
            GuiEvent event = new GuiEvent((Object)this, 2);
            event.addParameter((Object) command);
            agent.postGuiEvent(event);
        }
    }
}
