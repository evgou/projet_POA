package marche;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.logging.Logger;


public class MarcheGUI extends JFrame {

    private static final Logger logger = Logger.getLogger(MarcheGUI.class.getName());

    private final Marche agent;
    private JTable table;
    private DefaultTableModel tableModel;

    public MarcheGUI(Marche agent) {
        this.agent = agent;

        setTitle(agent.getLocalName());
        setSize(800, 300);
        //setIconImage(Toolkit.getDefaultToolkit().getImage("/home/alicia/Documents/M2/POA/projet_POA/projet/misc/images/poisson.png"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        logger.info("Marche GUI initialized");
    }



    private void addComponentToPanel(Container contentPane) {
        String[] columnNames = {"Vendeur", "Nom du lot", "Prix courant de l'enchère"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        contentPane.add(scrollPane, BorderLayout.CENTER);
    }

    public void updateTable() {
        tableModel.setRowCount(0);
        for (Map.Entry<String, Prix> entry : Marche.encheres.entrySet()) {
            tableModel.addRow(new String[]{entry.getKey(), "poisson", String.valueOf(entry.getValue().getPrice())});
        }
    }

    public void showGui() {
        addComponentToPanel(getContentPane());
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

}
