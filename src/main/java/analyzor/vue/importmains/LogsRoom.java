package analyzor.vue.importmains;

import analyzor.controleur.ControleurRoom;
import analyzor.vue.donnees.rooms.DTOPartieVisible;
import analyzor.vue.donnees.rooms.InfosRoom;
import analyzor.vue.reutilisables.fenetres.FenetreTroisiemeOrdre;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;


public class LogsRoom extends FenetreTroisiemeOrdre implements ActionListener {
    private final ControleurRoom controleurRoom;
    private InfosRoom infosRoom;
    private JLabel labelNom;
    private DefaultTableModel tableMains;
    private JButton reimporter;
    public LogsRoom(ControleurRoom controleurRoom, FenetreImport fenetreParente) {
        super(fenetreParente, "Mains non import\u00E9es", true);

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
        JScrollPane panneauDeroulant = new JScrollPane(tableVisible);
        panneauDeroulant.setPreferredSize(new Dimension(450, 200));
        panneauMains.add(panneauDeroulant);
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
        labelNom.setText("Mains non import\u00E9es pour : " + infosRoom.getNom());
    }

    public void setMainsNonImportees(List<DTOPartieVisible> mainsNonImportees) {
        
        tableMains.setRowCount(0);

        int nLignesAjoutees = 0;
        for (DTOPartieVisible dtoPartieVisible : mainsNonImportees) {
            String[] nouvelleDonnee =
                    new String[]{dtoPartieVisible.getCheminFichier(), dtoPartieVisible.getStatutImport()};
            tableMains.addRow(nouvelleDonnee);
            nLignesAjoutees++;
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
