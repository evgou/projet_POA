package preneur;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PreneurAgentGUI extends JFrame {
    private final PreneurAgent agent;
    private JTextArea logArea;
    private JTextField commandField;

    public PreneurAgentGUI(PreneurAgent agent) {
        this.agent = agent;
        setTitle(agent.getLocalName());
        setSize(400, 300);
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
