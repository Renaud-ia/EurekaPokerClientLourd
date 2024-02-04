package analyzor.vue.importmains;

import analyzor.controleur.ControleurRoom;
import analyzor.vue.donnees.rooms.DTOPartieVisible;
import analyzor.vue.donnees.rooms.InfosRoom;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;

/**
 * fenêtre qui affiche les mains non importées pour une room donnée
 */
public class LogsRoom extends JDialog implements ActionListener {
    private final ControleurRoom controleurRoom;
    private InfosRoom infosRoom;
    private JLabel labelNom;
    private DefaultTableModel tableMains;
    private JButton reimporter;
    public LogsRoom(ControleurRoom controleurRoom, JDialog fenetreParente) {
        super(fenetreParente, "Mains non importées", true);

        this.controleurRoom = controleurRoom;
        initialiser();
    }

    private void initialiser() {
        JPanel panneauGlobal = new JPanel();
        panneauGlobal.setLayout(new BoxLayout(panneauGlobal, BoxLayout.Y_AXIS));

        labelNom = new JLabel();
        panneauGlobal.add(labelNom);

        JPanel panneauMains = new JPanel();
        String[] columnNames = {"Chemin du fichier", "Statut d'import"};
        tableMains = new DefaultTableModel(null, columnNames);
        JTable tableVisible = new JTable(tableMains);
        panneauMains.add(new JScrollPane(tableVisible));
        panneauGlobal.add(panneauMains);

        JPanel panneauBoutons = new JPanel();
        reimporter = new JButton("Retenter l'import");
        reimporter.addActionListener(this);
        panneauBoutons.add(reimporter);
        panneauGlobal.add(panneauBoutons);

        this.add(panneauGlobal);
    }

    public void setNomRoom(InfosRoom infosRoom) {
        this.infosRoom = infosRoom;
        labelNom.setText("Mains non importées pour : " + infosRoom.getNom());
    }

    public void setMainsNonImportees(List<DTOPartieVisible> mainsNonImportees) {
        // efface les données
        tableMains.setRowCount(0);

        int nLignesAjoutees = 0;
        for (DTOPartieVisible dtoPartieVisible : mainsNonImportees) {
            String[] nouvelleDonnee =
                    new String[]{dtoPartieVisible.getCheminFichier(), dtoPartieVisible.getStatutImport()};
            tableMains.addRow(nouvelleDonnee);
            nLignesAjoutees++;
        }

        // on complète avec des lignes vides
        while (nLignesAjoutees++ < 5) {
            Object[] data = new Object[]{"", ""};
            tableMains.addRow(data);
        }

        this.pack();
    }

    public void reset() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == reimporter) {
            controleurRoom.retenterImport(infosRoom);
        }
    }
}
