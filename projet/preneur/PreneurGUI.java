package preneur;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;
import java.util.logging.Logger;


public class PreneurGUI extends JFrame {
    private final Preneur agent;
    private JTextField budgetField;
    private JRadioButton autoButton, manualButton;
    private JTable offersTable;
    private DefaultTableModel tableModel;
    private JButton validationButton;
    private JPanel bidButtonsPanel;
    private static final Logger logger = Logger.getLogger(PreneurGUI.class.getName());


    public PreneurGUI(Preneur agent) {
        this.agent = agent;


        setTitle(agent.getLocalName());
        setSize(1000, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Panel du haut pour le budget et le mode
        JPanel topPanel = new JPanel(new FlowLayout());

        // Zone de budget
        JLabel budgetLabel = new JLabel("Budget:");
        budgetField = new JTextField(10);
        topPanel.add(budgetLabel);
        topPanel.add(budgetField);

        // Boutons radio pour le mode
        ButtonGroup modeGroup = new ButtonGroup();
        autoButton = new JRadioButton("Automatique");
        manualButton = new JRadioButton("Manuel");
        modeGroup.add(autoButton);
        modeGroup.add(manualButton);
        manualButton.setSelected(true);

        topPanel.add(autoButton);
        topPanel.add(manualButton);

        add(topPanel, BorderLayout.NORTH);

        // Table des offres au centre
        String[] columnNames = {"Sélection", "Vendeur", "Lot", "Prix courant"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        offersTable = new JTable(tableModel);
        offersTable.getColumnModel().getColumn(0).setMaxWidth(70);
        JScrollPane scrollPane = new JScrollPane(offersTable);
        add(scrollPane, BorderLayout.CENTER);

        // Panel du bas pour les boutons
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Bouton de validation
        validationButton = new JButton("Valider la sélection");
        validationButton.addActionListener(e -> validateSelection());
        bottomPanel.add(validationButton, BorderLayout.NORTH);

        // Panel pour les boutons d'enchères (initialement vide)
        bidButtonsPanel = new JPanel(new FlowLayout());
        bidButtonsPanel.setVisible(false);
        bottomPanel.add(bidButtonsPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // Rendre la fenêtre visible
        setVisible(true);
    }

    // Méthode pour valider la sélection
    private void validateSelection() {
        // Vérifier si un budget a été entré en mode automatique
        if (autoButton.isSelected()) {
            try {
                double budget = Double.parseDouble(budgetField.getText());
                if (budget <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Veuillez entrer un budget valide (supérieur à 0)",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez entrer un budget valide",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Récupérer les offres sélectionnées
        Vector<String> selectedVendeurs = new Vector<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((Boolean) tableModel.getValueAt(i, 0)) {
                selectedVendeurs.add((String) tableModel.getValueAt(i, 1));
            }
        }

        if (selectedVendeurs.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner au moins une offre",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Désactiver les éléments de sélection
        budgetField.setEnabled(false);
        autoButton.setEnabled(false);
        manualButton.setEnabled(false);
        validationButton.setEnabled(false);
        offersTable.getColumnModel().getColumn(0).setMinWidth(0);
        offersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        offersTable.getColumnModel().getColumn(0).setWidth(0);

        // Si mode manuel, afficher les boutons d'enchère
        if (manualButton.isSelected()) {
            bidButtonsPanel.removeAll();
            for (String vendeur : selectedVendeurs) {
                JButton bidButton = new JButton("Enchérir - " + vendeur);
                bidButton.addActionListener(e -> agent.placeBid(vendeur));
                bidButtonsPanel.add(bidButton);
            }
            bidButtonsPanel.setVisible(true);
            bidButtonsPanel.revalidate();
            bidButtonsPanel.repaint();
        }

        // Informer l'agent des sélections
        float budget = autoButton.isSelected() ? Float.parseFloat(budgetField.getText()) : 0;
        agent.onSelectionValidated(selectedVendeurs, autoButton.isSelected(), budget);
    }

    // Méthode pour ajouter une nouvelle offre à la table
    public void addOffer(String vendeur, String lot, double prix) {
        tableModel.addRow(new Object[]{false, vendeur, lot, prix});
    }

    public void showGui() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}