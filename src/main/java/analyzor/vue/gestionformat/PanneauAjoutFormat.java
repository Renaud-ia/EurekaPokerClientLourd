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

        fJoueurs = new JSpinner();
        fJoueurs.setPreferredSize(DimensionsFormat.dJoueurs);
        this.add(fJoueurs);

        fMinBuyIn = new JSpinner();
        fMinBuyIn.setPreferredSize(DimensionsFormat.dBuyIn);
        this.add(fMinBuyIn);

        fMaxBuyIn = new JSpinner();
        fMaxBuyIn.setPreferredSize(DimensionsFormat.dBuyIn);
        this.add(fMaxBuyIn);

        bCreer = new JButton("Ajouter");
        bCreer.setPreferredSize(DimensionsFormat.dBoutonAjouter);
        bCreer.addActionListener(this);
        this.add(bCreer);

    }

    protected void setEtat(boolean active) {
        for (Component composant : this.getComponents()) {
            composant.setEnabled(active);
        }

    }

    @Override
    public void actionPerformed(ActionEvent evenement) {
        if (evenement.getSource() == bCreer) {

        }
    }
}
