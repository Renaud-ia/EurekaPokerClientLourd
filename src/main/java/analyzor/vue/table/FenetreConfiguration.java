package analyzor.vue.table;

import analyzor.controleur.ControleurTable;
import analyzor.vue.FenetrePrincipale;
import analyzor.vue.donnees.ConfigTable;
import analyzor.vue.donnees.DTOJoueur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class FenetreConfiguration extends JDialog implements ActionListener {
    private final ControleurTable controleurTable;
    private final ConfigTable configTable;
    private JPanel panneauJoueurs;
    private JPanel panneauBoutons;
    private JButton boutonValider;
    private JButton boutonExit;
    private final List<BlocJoueurConfig> blocJoueurs;
    public FenetreConfiguration(FenetrePrincipale fenetrePrincipale, ControleurTable controleurTable,
                                ConfigTable configTable) {
        super(fenetrePrincipale, "Configuration de la table", true);

        this.controleurTable = controleurTable;
        this.configTable = configTable;
        this.blocJoueurs = new ArrayList<>();

        setLocationRelativeTo(fenetrePrincipale);
        initialiser();
        rafraichir();
    }

    public void afficher() {
        rafraichir();
        this.setVisible(true);
    }

    private void rafraichir() {
        panneauJoueurs.removeAll();
        blocJoueurs.clear();

        if (!configTable.estInitialisee()) {
            JLabel labelErreur = new JLabel("Veuillez d'abord sélectionner un format");
            panneauJoueurs.add(labelErreur);
            boutonValider.setEnabled(false);
            panneauJoueurs.repaint();
        }

        else {
            // on va afficher les joueurs
            for (int i = configTable.getJoueurs().size() - 1; i >= 0; i--) {
                DTOJoueur joueur = configTable.getJoueurs().get(i);
                BlocJoueurConfig nouveauBloc = new BlocJoueurConfig(this, joueur, configTable.getBounty());
                panneauJoueurs.add(nouveauBloc);
                blocJoueurs.add(nouveauBloc);
            }

            boutonValider.setEnabled(true);
        }

        this.pack();
    }

    private void initialiser() {
        JPanel panneauPrincipal = new JPanel();
        panneauPrincipal.setLayout(new BoxLayout(panneauPrincipal, BoxLayout.Y_AXIS));

        panneauJoueurs = new JPanel();
        panneauJoueurs.setLayout(new BoxLayout(panneauJoueurs, BoxLayout.Y_AXIS));

        panneauPrincipal.add(panneauJoueurs);

        panneauBoutons = new JPanel();
        panneauBoutons.setLayout(new FlowLayout());

        boutonValider = new JButton("Valider");
        boutonValider.addActionListener(this);
        panneauBoutons.add(boutonValider);

        boutonExit = new JButton("Annuler");
        boutonExit.addActionListener(this);
        panneauBoutons.add(boutonExit);

        panneauPrincipal.add(panneauBoutons);

        this.add(panneauPrincipal);
    }

    void heroSelectionne(BlocJoueurConfig blocJoueurConfig) {
        for (BlocJoueurConfig blocJoueur : blocJoueurs) {
            if (blocJoueur == blocJoueurConfig) continue;
            blocJoueur.deselectionnerHero();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boutonValider) {
            for (BlocJoueurConfig blocJoueurConfig : blocJoueurs) {
                blocJoueurConfig.enregistrerDonnees();
            }

            controleurTable.configurationSelectionnee();
            setVisible(false);
        }
        else if (e.getSource() == boutonExit) {
            setVisible(false);
        }
    }
}
