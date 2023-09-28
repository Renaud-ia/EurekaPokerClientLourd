package analyzor.vue.vues;

import analyzor.controleur.ControleurRoom;
import analyzor.vue.composants.TableNonModifiable;
import analyzor.vue.donnees.InfosRoom;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VueRooms extends JDialog implements ActionListener {
    //todo rajouter import manuel ??
    //todo : est ce qu'il ne faudrait pas garder une référence à infosRoom dans les attributs
    private DefaultTableModel model;
    private JTable tableau;
    private final int largeurFenetre;
    private final int hauteurFenetre;
    private final ControleurRoom controleur;
    private final InfosRoom infosRoom;
    public VueRooms(VuePrincipale frame, ControleurRoom controleur, InfosRoom infosRoom) {
        super(frame, "Gestion des rooms", false);
        this.controleur = controleur;
        this.infosRoom = infosRoom;
        largeurFenetre = (int) (frame.getLargeurEcran() * 0.7);
        hauteurFenetre = (int) (frame.getHauteurEcran() * 0.7);

    }

    public void construireVue() {
        this.setLayout(new BorderLayout());
        setSize(largeurFenetre, hauteurFenetre);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Fermez la JDialog lors de la fermeture
        setLocationRelativeTo(null);
        afficherRooms();
        setVisible(true);
    }

    public void afficherRooms() {
        this.model = new TableNonModifiable();

        //todo : obtenir les colonnes par infosRoom ???
        this.model.addColumn("Site");
        this.model.addColumn("Fichiers");
        this.model.addColumn("Mains");
        this.model.addColumn("Dossiers");
        this.model.addColumn("Etat");


        for (int i=0; i<infosRoom.nRooms(); i++) {
            Object[] donneesLigne = infosRoom.getDonneesRooms(i);
            model.addRow(donneesLigne);
            System.out.println("Room affichée");
        }

        //todo : faire notre propre modèle de tableau pour personnaliser les cellules
        tableau = new JTable(this.model);
        JTableHeader header = tableau.getTableHeader();
        header.setReorderingAllowed(false);

        ListSelectionModel selectionModel = new DefaultListSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableau.setSelectionModel(selectionModel);

        // flow Layout affiche mal les tableaux
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(largeurFenetre, (int) (hauteurFenetre * 0.5)));

        panel.add(tableau.getTableHeader(), BorderLayout.NORTH);
        panel.add(tableau, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(100, 30));
        JButton bouton = new JButton("Configurer");
        bouton.addActionListener(this);
        buttonPanel.add(bouton);
        panel.add(buttonPanel, BorderLayout.EAST);

        this.add(panel, BorderLayout.CENTER);
    }

    public void actualiserLigne(int indexLigne, InfosRoom infosRoom) {
        Object[] donneesLigne = infosRoom.getDonneesRooms(indexLigne);
        int indexColonne = 0;
        for (Object donneeCase : donneesLigne) {
            this.model.setValueAt(donneeCase, indexLigne, indexColonne);
        }
        this.model.fireTableRowsUpdated(indexLigne, indexColonne);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int ligneSelectionne = tableau.getSelectedRow();
        controleur.roomSelectionnee(ligneSelectionne);
    }

    public void modifierRoom(int indexLigne) {
        model.setValueAt("Nouvelle valeur", indexLigne, 0);
    }

    public int getLargeurFenetre() {
        return this.largeurFenetre;
    }

    public int getHauteurFenetre() {
        return this.hauteurFenetre;
    }
}
