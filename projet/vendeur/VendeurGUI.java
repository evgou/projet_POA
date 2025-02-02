package vendeur;

import jade.gui.GuiEvent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;


public class VendeurGUI extends JFrame {

    private static final Logger logger = Logger.getLogger(VendeurGUI.class.getName());

    private static final String Publication = "Publication";
    private final Vendeur agent;
    private JTextField champPrice;
    private JTextField champPas;
    private JTextField champName;
    private JTextField champTempsAttente;
    private JButton sendButton;
    private JTable tableauEnchere;
    private DefaultTableModel modeleTableau;

    public VendeurGUI(Vendeur agent) {
        this.agent = agent;
        logger.info("Vendeur GUI initialized");

        setTitle(agent.getLocalName());
        setSize(800, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void addComponentToPanel(Container contentPane) {

        champName = new JTextField();
        champPrice = new JTextField();
        champPas = new JTextField();
        champTempsAttente = new JTextField();

        sendButton = new JButton();
        sendButton.setText("Créer l'enchère");
        sendButton.setActionCommand(Publication);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("Publication")) {
                    debutEnchere();
                }
            }
        });

        JPanel enchere = new JPanel();
        enchere.setLayout(new GridLayout(2, 4));
        enchere.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // enchere
        enchere.add(new JLabel("Nom"));
        enchere.add(new JLabel("Prix initial (€)"));
        enchere.add(new JLabel("Pas de variation (€)"));
        enchere.add(new JLabel("Temps d'attente"));
        enchere.add(new JLabel(""));
        enchere.add(champName);
        enchere.add(champPrice);
        enchere.add(champPas);
        enchere.add(champTempsAttente);
        enchere.add(sendButton);

        contentPane.add(enchere, BorderLayout.NORTH);

        // tableauEnchere
        String[] columnNames = {"Nom de l'enchère", "Prix", "Preneurs abonnés"};
        modeleTableau = new DefaultTableModel(columnNames, 0);
        tableauEnchere = new JTable(modeleTableau);
        JScrollPane scrollPane = new JScrollPane(tableauEnchere);
        contentPane.add(scrollPane, BorderLayout.CENTER);


    }

    private void debutEnchere() {
        try {
            // Récupération des paramètres
            String name = champName.getText();
            int price = Integer.parseInt(champPrice.getText());
            int pas = Integer.parseInt(champPas.getText());
            int temps = Integer.parseInt(champTempsAttente.getText());

            // Création d'un événement pour Vendeur
            GuiEvent event = new GuiEvent(this, 1);
            event.addParameter(name);
            event.addParameter(price);
            event.addParameter(pas);
            event.addParameter(temps);

            // Envoi de l'événement à l'agent vendeur
            agent.postGuiEvent(event);

            DefaultTableModel model = (DefaultTableModel) tableauEnchere.getModel();
            model.addRow(new Object[]{name, price, ""});

            agent.sendOffre();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer des nombres valides.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }


    public void showGui() {
        addComponentToPanel(getContentPane());
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

}
