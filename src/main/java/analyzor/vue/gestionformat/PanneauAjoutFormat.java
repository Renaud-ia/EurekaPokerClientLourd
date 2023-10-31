package analyzor.vue.gestionformat;

import analyzor.controleur.ControleurFormat;
import analyzor.modele.parties.Variante;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class PanneauAjoutFormat extends JPanel implements ActionListener {
    private ControleurFormat controleur;
    private HashMap<String, Variante.PokerFormat> nomsFormats;
    private JComboBox<String> fChoixFormat;
    private JCheckBox fAnte;
    private JCheckBox fBounty;
    private JSpinner fJoueurs;
    private JSpinner fMinBuyIn;
    private JSpinner fMaxBuyIn;
    private JButton bCreer;
    private SpinnerNumberModel spinnerModelBuyIn1 = new SpinnerNumberModel(0, 0, 20000, 1);
    private SpinnerNumberModel spinnerModelBuyIn2 = new SpinnerNumberModel(0, 0, 20000, 1);
    private SpinnerNumberModel spinnerModelJoueurs = new SpinnerNumberModel(0, 0, 10, 1);
    protected PanneauAjoutFormat(ControleurFormat controleur) {
        this.controleur = controleur;
        this.setLayout(new FlowLayout());
        creerPanneau();
        setEtat(false);
    }

    private void creerPanneau() {
        nomsFormats = new HashMap<>();
        nomsFormats.put("MTT", Variante.PokerFormat.MTT);
        nomsFormats.put("SPIN", Variante.PokerFormat.SPIN);
        nomsFormats.put("Cash-Game", Variante.PokerFormat.CASH_GAME);
        fChoixFormat = new JComboBox<>(nomsFormats.keySet().toArray(new String[0]));
        fChoixFormat.setPreferredSize(DimensionsFormat.dNomFormat);
        this.add(fChoixFormat);

        fAnte = new JCheckBox("ante");
        fAnte.setPreferredSize(DimensionsFormat.dAnte);
        this.add(fAnte);

        fBounty = new JCheckBox("bounty");
        fBounty.setPreferredSize(DimensionsFormat.dBounty);
        this.add(fBounty);

        JLabel labelJoueurs = new JLabel("Joueurs :");
        fJoueurs = new JSpinner(spinnerModelJoueurs);
        Dimension dimensionsJoueurs =
                new Dimension(DimensionsFormat.dJoueurs.width - labelJoueurs.getWidth(), DimensionsFormat.dJoueurs.height);
        fJoueurs.setPreferredSize(dimensionsJoueurs);
        this.add(labelJoueurs);
        this.add(fJoueurs);

        JLabel labelMinBuyIn = new JLabel("Buy-in > :");
        Dimension dimensionBuyIn =
                new Dimension(DimensionsFormat.dBuyIn.width - labelMinBuyIn.getWidth(), DimensionsFormat.dBuyIn.height);
        fMinBuyIn = new JSpinner(spinnerModelBuyIn1);
        ajouterEuro(fMinBuyIn);
        fMinBuyIn.setPreferredSize(dimensionBuyIn);
        this.add(labelMinBuyIn);
        this.add(fMinBuyIn);

        JLabel labelMaxBuyIn = new JLabel("Buy-in < :");
        fMaxBuyIn = new JSpinner(spinnerModelBuyIn2);
        ajouterEuro(fMaxBuyIn);
        fMaxBuyIn.setPreferredSize(DimensionsFormat.dBuyIn);
        this.add(labelMaxBuyIn);
        this.add(fMaxBuyIn);

        bCreer = new JButton("Ajouter un format");
        bCreer.setPreferredSize(DimensionsFormat.dBoutonAjouter);
        bCreer.addActionListener(this);
        this.add(bCreer);

    }

    private void ajouterEuro(JSpinner spinner) {
        // TODO : créer un
    }

    protected void setEtat(boolean active) {
        for (Component composant : this.getComponents()) {
            composant.setEnabled(active);
        }

    }

    @Override
    public void actionPerformed(ActionEvent evenement) {
        if (evenement.getSource() == bCreer) {
            controleur.creerFormat(
                    nomsFormats.get((String) fChoixFormat.getSelectedItem()),
                    fAnte.isSelected(),
                    fBounty.isSelected(),
                    (int) fJoueurs.getValue(),
                    (int) fMinBuyIn.getValue(),
                    (int) fMaxBuyIn.getValue()
                    );
        }
    }
}
