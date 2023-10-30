package analyzor.vue.vues;

import analyzor.controleur.ControleurRoom;
import analyzor.vue.composants.CadreLarge;
import analyzor.vue.Couleurs;
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
    private JTable tableau;
    private final int largeurFenetre;
    private final int hauteurFenetre;
    private final JPanel panneauFenetre = new JPanel();
    private final ControleurRoom controleur;
    private final InfosRoom infosRoom;
    private JButton boutonConfigurer;
    public VueRooms(VuePrincipale frame, ControleurRoom controleur, InfosRoom infosRoom) {
        super(frame, "Gestion des rooms", false);
        this.controleur = controleur;
        this.infosRoom = infosRoom;
        largeurFenetre = (int) (frame.getLargeurEcran() * 0.7);
        hauteurFenetre = (int) (frame.getHauteurEcran() * 0.7);
        parametrerFenetre();
    }

    public void parametrerFenetre() {
        setSize(largeurFenetre, hauteurFenetre);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Fermez la JDialog lors de la fermeture
        setLocationRelativeTo(null);
        setBackground(Couleurs.FOND_CLAIR);
        configurerBoutons();
    }

    private void configurerBoutons() {
        boutonConfigurer = new JButton("Configurer");
        boutonConfigurer.addActionListener(this);
    }

    public void actualiser() {
        panneauFenetre.setBackground(Couleurs.FOND_CLAIR);
        panneauFenetre.removeAll();
        panneauFenetre.setLayout(new FlowLayout());

        JPanel panneauTable = dessinerTable();
        panneauFenetre.add(panneauTable);

        JPanel panneauBouton = new CadreLarge();
        boutonConfigurer.setPreferredSize(new Dimension(100, 30));
        panneauBouton.add(boutonConfigurer);
        panneauFenetre.add(panneauBouton);

        this.add(panneauFenetre);
        this.pack();
        this.setVisible(true);
    }

    public JPanel dessinerTable() {
        JPanel panneauTable = new CadreLarge();
        panneauTable.setLayout(new BorderLayout());

        DefaultTableModel model = new TableNonModifiable();
        //todo : obtenir les colonnes par infosRoom ???
        model.addColumn("Site");
        model.addColumn("Fichiers");
        model.addColumn("Mains");
        model.addColumn("Dossiers");
        model.addColumn("Etat");

        for (int i=0; i<infosRoom.nRooms(); i++) {
            Object[] donneesLigne = infosRoom.getDonneesRooms(i);
            model.addRow(donneesLigne);
        }

        //todo : faire notre propre modèle de tableau pour personnaliser les cellules
        tableau = new JTable(model);
        JTableHeader header = tableau.getTableHeader();
        header.setReorderingAllowed(false);

        ListSelectionModel selectionModel = new DefaultListSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableau.setSelectionModel(selectionModel);

        panneauTable.add(tableau.getTableHeader(), BorderLayout.NORTH);
        panneauTable.add(tableau, BorderLayout.CENTER);

        return panneauTable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int ligneSelectionne = tableau.getSelectedRow();
        controleur.roomSelectionnee(ligneSelectionne);
    }

    public int getLargeurFenetre() {
        return this.largeurFenetre;
    }

    public int getHauteurFenetre() {
        return this.hauteurFenetre;
    }
}
