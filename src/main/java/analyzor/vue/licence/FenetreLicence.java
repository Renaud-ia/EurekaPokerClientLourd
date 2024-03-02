package analyzor.vue.licence;

import analyzor.controleur.ControleurLicence;
import analyzor.vue.donnees.licence.LicenceDTO;
import analyzor.vue.reutilisables.fenetres.FenetreSecondOrdre;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FenetreLicence extends FenetreSecondOrdre implements ActionListener {
    // todo vérifier et imposer une mise en forme de licence
    private final ControleurLicence controleurLicence;
    private final LicenceDTO licenceDTO;
    private JLabel labelStatutLicence;
    private BlocSaisieLicence champCleLicence;
    private JPanel panneauBoutons;
    private JButton boutonSupprimer;
    private JButton boutonAjouter;

    public FenetreLicence(JFrame fenetrePrincipale, LicenceDTO licenceDTO, ControleurLicence controleurLicence) {
        super(fenetrePrincipale, "Gestion de la licence", true);
        this.controleurLicence = controleurLicence;
        this.licenceDTO = licenceDTO;

        creerStructure();
    }

    private void creerStructure() {
        JPanel panneauGlobal = new JPanel();
        panneauGlobal.setLayout(new BoxLayout(panneauGlobal, BoxLayout.Y_AXIS));

        JPanel panneauInformations = new JPanel();
        labelStatutLicence = new JLabel();
        panneauInformations.add(labelStatutLicence);

        panneauGlobal.add(panneauInformations);


        JPanel panneauSaisie = new JPanel();
        panneauSaisie.setLayout(new FlowLayout());

        champCleLicence = new BlocSaisieLicence();
        panneauSaisie.add(champCleLicence);

        panneauBoutons = new JPanel();
        panneauSaisie.add(panneauBoutons);

        // on initialise les boutons sans les ajouter
        boutonAjouter = new JButton("Ajouter");
        boutonAjouter.addActionListener(this);
        boutonSupprimer = new JButton("Supprimer");
        boutonSupprimer.addActionListener(this);

        panneauGlobal.add(panneauSaisie);

        this.add(panneauGlobal);
    }

    public void rafraichir() {
        panneauBoutons.removeAll();

        if (licenceDTO.getCleLicence() != null) {
            champCleLicence.changerTexte(licenceDTO.getCleLicence());

            if (licenceDTO.estActive()) {
                champCleLicence.estEditable(false);
                panneauBoutons.add(boutonSupprimer);
                labelStatutLicence.setText("Clé de licence active.");
            }

            else {
                champCleLicence.estEditable(true);
                panneauBoutons.add(boutonAjouter);
                labelStatutLicence.setText("Clé de licence incorrecte.");
            }
        }

        else {
            labelStatutLicence.setText("Aucune clé de licence enregistrée.");
            champCleLicence.estEditable(true);
            panneauBoutons.add(boutonAjouter);
        }
        this.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boutonAjouter) {
            String messageErreurSaisie = champCleLicence.estValide();
            if (messageErreurSaisie != null) {
                messageErreur(messageErreurSaisie);
                return;
            }
            controleurLicence.activerLicence(champCleLicence.getCleSaisie());
        }

        else if (e.getSource() == boutonSupprimer) {
            int choix = JOptionPane.showConfirmDialog(this,
                    "Voulez-vous vraiment supprimer la licence ?\n" +
                            "Cette licence ne pourra pas être réactivée",
                    "Confirmation", JOptionPane.YES_NO_OPTION);

            if (choix == JOptionPane.YES_OPTION) {
                controleurLicence.supprimerLicence();
            }

        }
    }
}
