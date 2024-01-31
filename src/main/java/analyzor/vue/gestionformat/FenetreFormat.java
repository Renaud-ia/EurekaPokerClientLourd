package analyzor.vue.gestionformat;

import analyzor.controleur.ControleurFormat;
import analyzor.vue.donnees.format.DTOFormat;
import analyzor.vue.FenetrePrincipale;
import analyzor.vue.reutilisables.FenetreAvecMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FenetreFormat extends FenetreAvecMessage implements ActionListener {
    private final ControleurFormat controleur;
    private final GestionFormat gestionFormat;
    private final NouveauFormat nouveauFormat;
    private final List<LigneFormat> formatsVisibles;
    private JPanel panneauLignesInfos;
    private JLabel aucunFormat;
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
        ImageIcon iconeImage = new ImageIcon("icon_eureka.png");
        this.setIconImage(iconeImage.getImage());

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
        aucunFormat = new JLabel("Aucun format d\u00E9tect\u00E9, ajoutez un format avec le mode \u00E9dition");
        panneauGlobal.add(panneauLignesInfos);

        panneauGlobal.add(new JSeparator(JSeparator.HORIZONTAL));

        panneauBoutons = new JPanel();
        panneauBoutons.setLayout(new FlowLayout());
        boutonAjouter = new JButton("Ajouter un format");
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
        System.out.println("ACTUALISATION FENETRE FORMAT");
        if (formatsVisibles.isEmpty()) {
            panneauLignesInfos.add(aucunFormat);
        }
        else panneauLignesInfos.remove(aucunFormat);

        for (LigneFormat ligneFormat : formatsVisibles) {
            ligneFormat.actualiser();
        }

        this.pack();
    }

    // interface de construction de la vue

    public void ajouterFormat(DTOFormat infosFormat) {
        LigneFormat nouvelleLigne = new LigneFormat(controleur, infosFormat, this);
        formatsVisibles.add(nouvelleLigne);
        panneauLignesInfos.add(nouvelleLigne);
        mapLignesVues.put(infosFormat, nouvelleLigne);
        aucunFormat.setVisible(false);
        this.repaint();
        this.pack();
    }

    // gestion users sur composants

    // appelé directement par les lignes, on les supprime de l'interface si le controleur accepte
    protected void supprimerFormat(DTOFormat dtoFormat) {
        if (controleur.supprimerFormat(dtoFormat)) {
            LigneFormat ligneSupprimee = mapLignesVues.get(dtoFormat);
            if (ligneSupprimee == null) throw new RuntimeException("Ligne non trouvée dans vue");
            formatsVisibles.remove(ligneSupprimee);
            panneauLignesInfos.remove(ligneSupprimee);
            mapLignesVues.remove(dtoFormat);

            messageInfo("Le format a bien été supprimé");
            actualiser();
        }
        else {
            this.messageErreur("Suppression du format impossible");
        }
        this.repaint();
    }

    public void gestionFormat(DTOFormat format) {
        gestionFormat.setFormat(format);
        gestionFormat.setVisible(true);
    }

    public void creationFormat(DTOFormat format) {
        nouveauFormat.setVisible(false);
        if (controleur.creerFormat(format)) {
            messageInfo("Format créé avec succès");
        }
        else {
            messageErreur("Pas pu créer le format");
        }

        nouveauFormat.reset();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boutonAjouter) {
            nouveauFormat.setVisible(true);
        }
    }
}
