package analyzor.vue.licence;

import analyzor.controleur.ControleurLicence;
import analyzor.vue.donnees.licence.LicenceDTO;
import analyzor.vue.reutilisables.fenetres.FenetreSecondOrdre;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FenetreLicence extends FenetreSecondOrdre implements ActionListener {
    private final static int MARGE_INTERNE = 10;
    private final ControleurLicence controleurLicence;
    private final LicenceDTO licenceDTO;
    private JLabel labelStatutLicence;
    private BlocSaisieLicence champCleLicence;
    private JButton boutonReverifier;
    private JButton boutonSupprimer;
    private JButton boutonAjouter;

    public FenetreLicence(JFrame fenetrePrincipale, LicenceDTO licenceDTO, ControleurLicence controleurLicence) {
        super(fenetrePrincipale, "Gestion de la licence", true);
        this.controleurLicence = controleurLicence;
        this.licenceDTO = licenceDTO;

        creerStructure();
        this.setResizable(false);
    }

    private void creerStructure() {
        JPanel panneauGlobal = new JPanel();
        EmptyBorder bordureInterne = new EmptyBorder(MARGE_INTERNE, MARGE_INTERNE, MARGE_INTERNE, MARGE_INTERNE);
        panneauGlobal.setBorder(bordureInterne);

        panneauGlobal.setLayout(new BoxLayout(panneauGlobal, BoxLayout.Y_AXIS));

        JPanel panneauInformations = new JPanel();
        labelStatutLicence = new JLabel();
        panneauInformations.add(labelStatutLicence);
        labelStatutLicence.setAlignmentX(Component.LEFT_ALIGNMENT);

        panneauGlobal.add(panneauInformations);


        JPanel panneauSaisie = new JPanel();
        panneauSaisie.setLayout(new FlowLayout());

        champCleLicence = new BlocSaisieLicence();
        panneauSaisie.add(champCleLicence);
        champCleLicence.setAlignmentX(Component.LEFT_ALIGNMENT);

        
        boutonAjouter = new JButton("Ajouter");
        boutonAjouter.addActionListener(this);
        boutonReverifier = new JButton("Rev\u00E9rifier");
        boutonReverifier.addActionListener(this);
        boutonSupprimer = new JButton("Supprimer");
        boutonSupprimer.addActionListener(this);

        panneauGlobal.add(panneauSaisie);

        this.add(panneauGlobal);
    }

    public void rafraichir() {
        champCleLicence.viderPanneauBoutons();

        if (licenceDTO.getCleLicence() != null) {
            champCleLicence.changerTexte(licenceDTO.getCleLicence());

            if (licenceDTO.estActive()) {
                champCleLicence.estEditable(false);
                champCleLicence.ajouterBouton(boutonSupprimer);
                labelStatutLicence.setText("Cl\u00E9 de licence active.");
            }

            else if (licenceDTO.verificationImpossible()) {
                champCleLicence.estEditable(false);
                champCleLicence.ajouterBouton(boutonReverifier);
                champCleLicence.ajouterBouton(boutonSupprimer);
                labelStatutLicence.setText("La cl\u00E9 de licence n'a pas pu \u00EAtre v\u00E9rifi\u00E9e, v\u00E9rifiez votre connexion.");
            }

            else {
                champCleLicence.estEditable(true);
                champCleLicence.ajouterBouton(boutonAjouter);
                labelStatutLicence.setText("Cl\u00E9 de licence incorrecte.");
            }
        }

        else {
            labelStatutLicence.setText("Aucune cl\u00E9 de licence enregistr\u00E9e.");
            champCleLicence.estEditable(true);
            champCleLicence.ajouterBouton(boutonAjouter);
        }
        this.pack();
        recentrer();
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
                            "Cette licence ne pourra pas \u00EAtre r\u00E9activ\u00E9e",
                    "Confirmation", JOptionPane.YES_NO_OPTION);

            if (choix == JOptionPane.YES_OPTION) {
                controleurLicence.supprimerLicence();
            }

        }

        else if (e.getSource() == boutonReverifier) {
            controleurLicence.reverifierLicence();
        }
    }

    public void licenceSupprimee() {
        champCleLicence.viderPanneauBoutons();
        champCleLicence.estEditable(true);
        champCleLicence.ajouterBouton(boutonAjouter);
        this.pack();
        this.recentrer();
    }
}
