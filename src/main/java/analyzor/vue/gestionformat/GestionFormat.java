package analyzor.vue.gestionformat;

import analyzor.controleur.ControleurFormat;
import analyzor.controleur.WorkerAffichable;
import analyzor.vue.donnees.format.DTOFormat;
import analyzor.vue.donnees.format.FormConsultationFormat;
import analyzor.vue.gestionformat.detailformat.LigneSimple;
import analyzor.vue.reutilisables.DialogAvecMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * fenêtre de gestion des formats
 * permet de changer le nom, réinitialiser et lancer le calcul
 */
class GestionFormat extends DialogAvecMessage implements ActionListener {
    private final ControleurFormat controleurFormat;
    private final FenetreFormat fenetreFormat;
    private JPanel panneauContenu;
    private FormConsultationFormat formatGere;
    private JTextField nomFormat;
    private JButton boutonChangerNom;
    private JPanel panneauBoutons;
    private JButton supprimerFormat;
    private JButton reinitialiserFormat;
    private JPanel panelCalcul;
    private JButton calculerRanges;
    private JButton stopCalcul;

    GestionFormat(ControleurFormat controleurFormat, FenetreFormat fenetreParente) {
        super(fenetreParente, "Gestion du format", true);

        this.controleurFormat = controleurFormat;
        this.fenetreFormat = fenetreParente;

        this.setLocationRelativeTo(null);

        initialiser();
    }

    // méthode publique

    /**
     * appelé dès que l'édition du format gère, la fenêtre revoit elle même son contenu
     * @param format le format sélectionné pour gestion
     */
    void setFormat(DTOFormat format) {
        this.formatGere = new FormConsultationFormat(format);
        actualiserContenu();
        actualiserWorker();
    }


    // méthodes privées d'actualisation

    private void initialiser() {
        JPanel panneauGlobal = new JPanel();
        panneauGlobal.setLayout(new BoxLayout(panneauGlobal, BoxLayout.Y_AXIS));
        panneauContenu = new JPanel();
        panneauContenu.setLayout(new BoxLayout(panneauContenu, BoxLayout.Y_AXIS));
        panneauGlobal.add(panneauContenu);

        panneauBoutons = new JPanel();
        panneauBoutons.setLayout(new FlowLayout());

        supprimerFormat = new JButton("Supprimer");
        supprimerFormat.addActionListener(this);
        panneauBoutons.add(supprimerFormat);

        reinitialiserFormat = new JButton("Reinitialiser");
        reinitialiserFormat.addActionListener(this);
        panneauBoutons.add(reinitialiserFormat);

        panneauGlobal.add(panneauBoutons);

        panelCalcul = new JPanel();
        panelCalcul.setLayout(new FlowLayout());

        // on crée juste les boutons de calcul
        calculerRanges = new JButton("Lancer le calcul");
        calculerRanges.addActionListener(this);

        stopCalcul = new JButton("Pause");
        stopCalcul.addActionListener(this);

        panneauGlobal.add(panelCalcul);

        this.add(panneauGlobal);
    }

    /**
     * à chaque changement de format, on va tout redessiner car dépend du format
     */
    private void actualiserContenu() {
        panneauContenu.removeAll();

        JPanel ligneNom = new JPanel();
        ligneNom.setLayout(new FlowLayout());
        JLabel labelNomFormat = new JLabel("Nom du format");
        ligneNom.add(labelNomFormat);
        nomFormat = new JTextField(formatGere.getNomFormat());
        ligneNom.add(nomFormat);
        boutonChangerNom = new JButton("Changer");
        boutonChangerNom.addActionListener(this);
        ligneNom.add(boutonChangerNom);
        panneauContenu.add(ligneNom);

        LigneSimple ligneFormat = new LigneSimple("Format : ", formatGere.getNomFormat());
        panneauContenu.add(ligneFormat);

        if (formatGere.aAnte()) {
            LigneSimple ligneAnteMin = new LigneSimple("Ante minimum : ", formatGere.getMinAnte());
            panneauContenu.add(ligneAnteMin);
            LigneSimple ligneAnteMax = new LigneSimple("Ante maximum : ", formatGere.getMaxAnte());
            panneauContenu.add(ligneAnteMax);
        }

        if (formatGere.aRake()) {
            LigneSimple ligneRakeMin = new LigneSimple("Rake minimum : ", formatGere.getMinRake());
            panneauContenu.add(ligneRakeMin);
            LigneSimple ligneRakeMax = new LigneSimple("Rake maximum : ", formatGere.getMaxRake());
            panneauContenu.add(ligneRakeMax);
        }

        if (formatGere.bountyExiste()) {
            LigneSimple ligneBounty = new LigneSimple("Bounty : ", formatGere.getBounty());
            panneauContenu.add(ligneBounty);
        }

        LigneSimple ligneNombreJoueurs = new LigneSimple("Nombre de joueurs : ", formatGere.getNombreJoueurs());
        panneauContenu.add(ligneNombreJoueurs);

        LigneSimple ligneBuyInMin = new LigneSimple("Buy in minmum : ", formatGere.getMinBuyIn());
        panneauContenu.add(ligneBuyInMin);
        LigneSimple ligneBuyInMax = new LigneSimple("Buy in maximum : ", formatGere.getMaxBuyIn());
        panneauContenu.add(ligneBuyInMax);

        LigneSimple ligneDateCreation = new LigneSimple("Date de création : ", formatGere.getDateCreation());
        panneauContenu.add(ligneDateCreation);

        panneauContenu.repaint();
        panneauContenu.revalidate();

        this.repaint();
        this.pack();
    }

    private void actualiserWorker() {
        panelCalcul.removeAll();
        WorkerAffichable workerAffichable = controleurFormat.genererWorker(formatGere.getFormat());
        JProgressBar progressBar = workerAffichable.getProgressBar();
        panelCalcul.add(progressBar);

        panelCalcul.add(calculerRanges);
        this.repaint();
        this.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boutonChangerNom) {
            String nouveauNom = nomFormat.getText();
            if (controleurFormat.changerNomFormat(formatGere.getFormat(), nouveauNom)) {
                formatGere.changerNom(nouveauNom);
                messageInfo("Le nom a bien été changé");
                fenetreFormat.actualiser();
            }
            else {
                messageErreur("Pas réussi à changer le nom");
            }
        }

        else if (e.getSource() == supprimerFormat) {
            int choix = JOptionPane.showConfirmDialog(this,
                    "Voulez-vous vraiment supprimer ce format ?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);

            if (choix == JOptionPane.YES_OPTION) {
                this.setVisible(false);
                fenetreFormat.supprimerFormat(formatGere.getFormat());
            }

        }

        else if (e.getSource() == reinitialiserFormat) {
            int choix = JOptionPane.showConfirmDialog(this,
                    "Voulez-vous vraiment réinitialiser ce format ? Les ranges seront supprimées",
                    "Confirmation", JOptionPane.YES_NO_OPTION);

            if (choix == JOptionPane.YES_OPTION) {
                if (controleurFormat.reinitialiser(formatGere.getFormat())) {
                    messageInfo("Format réinitialisé avec succès");
                }

                else {
                    messageErreur("Une erreur est survenue pendant la réinitialisation");
                }
            }
        }

        else if (e.getSource() == calculerRanges) {
            panelCalcul.remove(calculerRanges);
            panelCalcul.add(stopCalcul);
            this.repaint();
            this.revalidate();
            controleurFormat.lancerWorker();
        }

        else if (e.getSource() == stopCalcul) {
            controleurFormat.arreterWorker();
            actualiserWorker();
        }
    }
}
