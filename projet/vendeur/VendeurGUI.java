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

public class VendeurGUI extends JFrame {

    private static final Logger logger = Logger.getLogger(VendeurGUI.class.getName());

    private final Vendeur agent;
    private JTextField commandField;
    private JTextField price;
    private JTextField pas;
    private JTextField name;


    public VendeurGUI(Vendeur agent) {
        this.agent = agent;

        setTitle(agent.getLocalName());
        setSize(1000, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //logger.info("Je suis là");

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(2, 5, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topPanel.add(new JLabel("Nom :"));
        name = new JTextField();
        topPanel.add(name);

        topPanel.add(new JLabel("Prix initial (euros) :"));
        price = new JTextField();
        topPanel.add(price);

        topPanel.add(new JLabel("Pas de variation (euros) :"));
        pas = new JTextField();
        topPanel.add(pas);

        JButton createButton = new JButton("Créer annonce");
        topPanel.add(createButton);

        add(topPanel, BorderLayout.NORTH);


        JPanel commandPanel = new JPanel();
        JTextField commandField = new JTextField(20);
        JButton sendButton = new JButton("Envoyer");
        sendButton.addActionListener((ActionEvent e) -> sendCommand());
        commandPanel.add(commandField);
        commandPanel.add(sendButton);

        add(commandPanel, BorderLayout.SOUTH);
    }

    public void addComponentsToPane(final Container pane) {
        setTitle(agent.getName());

        price = new JTextField("1000");
        pas = new JTextField("50");
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
