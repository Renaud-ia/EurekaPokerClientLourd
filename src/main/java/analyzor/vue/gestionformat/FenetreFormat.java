package analyzor.vue.gestionformat;

import analyzor.controleur.ControleurFormat;
import analyzor.vue.basiques.Images;
import analyzor.vue.donnees.format.DTOFormat;
import analyzor.vue.FenetrePrincipale;
import analyzor.vue.reutilisables.fenetres.FenetreSecondOrdre;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FenetreFormat extends FenetreSecondOrdre implements ActionListener {
    private final ControleurFormat controleur;
    private final GestionFormat gestionFormat;
    private final NouveauFormat nouveauFormat;
    private final List<LigneFormat> formatsVisibles;
    private JPanel panneauLignesInfos;
    private JPanel aucunFormat;
    private JPanel panneauBoutons;
    private JButton boutonAjouter;
    private final HashMap<DTOFormat, LigneFormat> mapLignesVues;
    public FenetreFormat(FenetrePrincipale fenetrePrincipale, ControleurFormat controleur) {
        super(fenetrePrincipale, "Gestion des formats", true);
        this.controleur = controleur;

        formatsVisibles = new ArrayList<>();
        gestionFormat = new GestionFormat(controleur, this);
        nouveauFormat = new NouveauFormat(this, controleur);
        mapLignesVues = new HashMap<>();

        this.setResizable(false);
        this.setLocationRelativeTo(fenetrePrincipale);
        this.setIconImage(Images.icone);

        inialiserPanneaux();
    }

    /**
     * on crée la structure globale de la page
     */
    private void inialiserPanneaux() {
        JPanel panneauGlobal = new JPanel();
        panneauGlobal.setLayout(new BoxLayout(panneauGlobal, BoxLayout.Y_AXIS));

        panneauLignesInfos = new JPanel();
        panneauLignesInfos.setLayout(new BoxLayout(panneauLignesInfos, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(panneauLignesInfos, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        panneauGlobal.add(scrollPane);

        // on initialise juste ce label
        aucunFormat = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aucunFormat.add(new JLabel("Aucun format d\u00E9tect\u00E9, ajoutez un format"));

        panneauGlobal.add(Box.createRigidArea(new Dimension(800, 20)));

        panneauGlobal.add(new JSeparator(JSeparator.HORIZONTAL));

        panneauBoutons = new JPanel();
        panneauBoutons.setPreferredSize(new Dimension(800, 40));
        panneauBoutons.setLayout(new FlowLayout());
        boutonAjouter = new JButton("Cr\u00E9er un format");
        boutonAjouter.setIcon(new ImageIcon(Images.ajouterFormat));
        boutonAjouter.addActionListener(this);
        panneauBoutons.add(boutonAjouter);
        panneauGlobal.add(panneauBoutons);

        this.add(panneauGlobal);
        this.pack();
    }

    /**
     * va regarder tous les éléments modifiés dans DAO et va actualiser l'affichage
     * IMPORTANT => une fois consulté, les éléments modifiés ne sont plus référencés
     * garantit que les deux panneaux sont identiques
     */
    public void actualiser() {
        if (formatsVisibles.isEmpty()) {
            panneauLignesInfos.add(aucunFormat);
        }
        else panneauLignesInfos.remove(aucunFormat);

        for (LigneFormat ligneFormat : formatsVisibles) {
            ligneFormat.actualiser();
        }

        panneauLignesInfos.revalidate();
        panneauLignesInfos.repaint();

        this.pack();
        this.setLocationRelativeTo(fenetreParente);
    }

    // interface de construction de la vue

    public void ajouterFormat(DTOFormat infosFormat) {
        LigneFormat nouvelleLigne = new LigneFormat(controleur, infosFormat, this);
        formatsVisibles.add(nouvelleLigne);
        panneauLignesInfos.add(nouvelleLigne);
        mapLignesVues.put(infosFormat, nouvelleLigne);
        aucunFormat.setVisible(false);

        panneauLignesInfos.revalidate();
        panneauLignesInfos.repaint();
        this.repaint();
        this.pack();
    }

    // gestion users sur composants

    // appelé directement par les lignes, on les supprime de l'interface si le controleur accepte
    protected void supprimerFormat(DTOFormat dtoFormat) {
        gestionFormat.fermer();
        if (controleur.supprimerFormat(dtoFormat)) {
            LigneFormat ligneSupprimee = mapLignesVues.get(dtoFormat);
            if (ligneSupprimee == null) throw new RuntimeException("Ligne non trouvée dans vue");
            formatsVisibles.remove(ligneSupprimee);
            panneauLignesInfos.remove(ligneSupprimee);
            mapLignesVues.remove(dtoFormat);

            messageInfo("Le format a bien \u00E9t\u00E9 supprim\u00E9");
            actualiser();
        }
        else {
            this.messageErreur("Suppression du format impossible");
        }
        this.repaint();
    }

    public void gestionFormat(DTOFormat format) {
        gestionFormat.setFormat(format);
        gestionFormat.afficher();
    }

    public void creationFormat(DTOFormat format) {
        nouveauFormat.fermer();
        if (controleur.creerFormat(format)) {
            messageInfo("Format cr\u00E9\u00E9 avec succ\u00E8s");
        }
        else {
            messageErreur("Pas pu cr\u00E9er le format");
        }

        nouveauFormat.reset();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boutonAjouter) {
            this.setVisible(false);
            nouveauFormat.afficher();
        }
    }

    public void calculTermine(boolean annule) {
        actualiser();
        gestionFormat.calculTermine(annule);
    }

    public JDialog getFenetreGestion() {
        return gestionFormat;
    }
}
