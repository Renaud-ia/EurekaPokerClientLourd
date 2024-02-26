package analyzor.vue.licence;

import analyzor.controleur.ControleurLicence;
import analyzor.vue.donnees.licence.LicenceDTO;
import analyzor.vue.reutilisables.DialogAvecMessage;
import analyzor.vue.reutilisables.FenetreAvecMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FenetreLicence extends FenetreAvecMessage implements ActionListener {
    // todo vérifier et imposer une mise en forme de licence
    private final ControleurLicence controleurLicence;
    private final LicenceDTO licenceDTO;
    private JTextField champCleLicence;
    private JPanel panneauBoutons;
    private JButton boutonSupprimer;
    private JButton boutonAjouter;

    public FenetreLicence(JFrame fenetrePrincipale, LicenceDTO licenceDTO, ControleurLicence controleurLicence) {
        super(fenetrePrincipale, "Gestion de la licence", true);
        this.controleurLicence = controleurLicence;
        this.licenceDTO = licenceDTO;

        creerStructure();
        rafraichir();
    }

    private void creerStructure() {
        JPanel panneauGlobal = new JPanel();
        panneauGlobal.setLayout(new FlowLayout());

        champCleLicence = new JTextField();
        panneauGlobal.add(champCleLicence);

        panneauBoutons = new JPanel();
        panneauGlobal.add(panneauBoutons);

        // on initialise les boutons sans les ajouter
        boutonAjouter = new JButton("Ajouter");
        boutonAjouter.addActionListener(this);
        boutonSupprimer = new JButton("Supprimer");
        boutonSupprimer.addActionListener(this);

        this.add(panneauGlobal);
    }

    public void rafraichir() {
        panneauBoutons.removeAll();

        if (licenceDTO.estActive()) {
            champCleLicence.setText(licenceDTO.getCleLicence());
            champCleLicence.setEditable(false);
            panneauBoutons.add(boutonSupprimer);
        }

        else {
            champCleLicence.setText("");
            champCleLicence.setEditable(true);
            panneauBoutons.add(boutonAjouter);
        }

        this.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boutonAjouter) {
            controleurLicence.activerLicence(champCleLicence.getText());
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
