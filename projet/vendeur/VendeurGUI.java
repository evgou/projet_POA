package vendeur;

import jade.gui.GuiEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import java.util.logging.Logger;

public class VendeurGUI extends JFrame {

    private static final Logger logger = Logger.getLogger(VendeurGUI.class.getName());

    private final Vendeur agent;
    private JTextField price;
    private JTextField delay;
    private JTextField step;
    private JTextField name;


    public VendeurGUI(Vendeur agent) {
        this.agent = agent;

        setTitle(agent.getLocalName());
        setSize(1000, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        logger.info("Je suis là");

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);


        JPanel commandPanel = new JPanel();
        JTextField commandField = new JTextField(20);
        JButton sendButton = new JButton("Envoyer");
        sendButton.addActionListener((ActionEvent e) -> sendCommand());
        commandPanel.add(commandField);
        commandPanel.add(sendButton);

        add(commandPanel, BorderLayout.SOUTH);
    }

    public void addComponentsToPane(final Container pane) {
        setTitle(Vendeur.getName());

        price = new JTextField("500");
        delay = new JTextField("10");
        step = new JTextField("50");
        name = new JTextField("Poisson");
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
