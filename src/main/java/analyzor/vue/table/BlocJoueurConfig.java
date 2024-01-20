package analyzor.vue.table;

import analyzor.vue.donnees.DTOJoueur;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BlocJoueurConfig extends JPanel implements ActionListener {
    private final FenetreConfiguration fenetreConfiguration;
    private final DTOJoueur joueur;
    private final boolean bounty;
    private JSpinner spinnerStack;
    private JSpinner spinnerBounty;
    private JCheckBox heroCheckBox;

    public BlocJoueurConfig(FenetreConfiguration fenetreConfiguration, DTOJoueur joueur, boolean bounty) {
        this.fenetreConfiguration = fenetreConfiguration;
        this.joueur = joueur;
        this.bounty = bounty;

        this.setLayout(new FlowLayout());

        initialiser();
    }

    private void initialiser() {
        JLabel labelNomJoueur = new JLabel(joueur.getNom() + " : ");
        this.add(labelNomJoueur);

        JLabel labelStack = new JLabel("stack");
        this.add(labelStack);

        spinnerStack = new JSpinner();
        spinnerStack.setValue(joueur.getStack());
        this.add(spinnerStack);

        if (bounty) {
            spinnerBounty = new JSpinner();
            spinnerBounty.setValue(joueur.getBounty());
            this.add(spinnerBounty);
        }

        heroCheckBox = new JCheckBox("hero");
        heroCheckBox.addActionListener(this);
        this.add(heroCheckBox);
    }

    public void deselectionnerHero() {
        heroCheckBox.setSelected(false);
    }

    // va modifier le DTO joueur avec les valeurs actuelles
    public void enregistrerDonnees() {
        joueur.setStack(((Integer) spinnerStack.getValue()).floatValue());
        if (bounty) {
            joueur.setBounty(((Integer) spinnerBounty.getValue()).floatValue());
        }
        joueur.setHero(heroCheckBox.isSelected());
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == heroCheckBox) {
            if (heroCheckBox.isSelected()) {
                fenetreConfiguration.heroSelectionne(this);
            }
        }
    }
}
