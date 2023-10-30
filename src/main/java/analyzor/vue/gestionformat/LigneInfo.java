package analyzor.vue.gestionformat;

import analyzor.vue.donnees.DAOFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LigneInfo extends JPanel implements ActionListener {
    private final PanneauLignesInfos panneauParent;
    private final DAOFormat.InfosFormat infosFormat;
    private final JButton boutonSupprimer;
    private final JButton boutonChoisir;
    private JButton boutonAffiche;
    public LigneInfo(PanneauLignesInfos panneauParent, DAOFormat.InfosFormat infosFormat) {
        this.panneauParent = panneauParent;
        this.infosFormat = infosFormat;
        boutonChoisir = new JButton("CHOISIR");
        boutonSupprimer = new JButton("Supprimer");
        this.setLayout(new FlowLayout());
        construireVueLigne();
    }

    private void construireVueLigne() {
        JTextField nomFormat = new JTextField(infosFormat.getNomFormat());
        nomFormat.setPreferredSize(DimensionsFormat.dNomFormat);
        nomFormat.setEditable(false);
        this.add(nomFormat);

        JTextField ante;
        if (infosFormat.isAnte()) {
            ante = new JTextField("avec ante");
        }
        else ante = new JTextField("sans ante");
        ante.setPreferredSize(DimensionsFormat.dAnte);
        ante.setEditable(false);
        this.add(ante);

        JTextField ko;
        if (infosFormat.isKo()) {
            ko = new JTextField("avec KO");
        }
        else ko = new JTextField("sans KO");
        ko.setPreferredSize(DimensionsFormat.dBounty);
        ko.setEditable(false);
        this.add(ko);

        JTextField nJoueurs = new JTextField(infosFormat.getnJoueurs() + " joueurs");
        nJoueurs.setEditable(false);
        nJoueurs.setPreferredSize(DimensionsFormat.dJoueurs);
        this.add(nJoueurs);

        JTextField minBuyIn = new JTextField("> " + infosFormat.getMinBuyIn());
        minBuyIn.setEditable(false);
        minBuyIn.setPreferredSize(DimensionsFormat.dBuyIn);
        this.add(minBuyIn);

        JTextField maxBuyIn = new JTextField("< " + infosFormat.getMaxBuyIn());
        maxBuyIn.setEditable(false);
        maxBuyIn.setPreferredSize(DimensionsFormat.dBuyIn);
        this.add(maxBuyIn);

        JTextField nParties = new JTextField(infosFormat.getNombreParties() + " parties");
        nParties.setPreferredSize(DimensionsFormat.dParties);
        nParties.setEditable(false);
        this.add(nParties);

        boutonAffiche.addActionListener(this);
        boutonAffiche = boutonChoisir;
        this.add(boutonAffiche);

        this.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
