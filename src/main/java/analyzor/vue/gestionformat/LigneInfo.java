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
        ImageIcon iconChoisir = new ImageIcon("icon_choisir.png");
        boutonChoisir.setMargin(new Insets(1, 1, 1, 1));
        boutonChoisir.setIcon(iconChoisir);
        boutonSupprimer = new JButton("SUPPRIMER");
        boutonChoisir.setMargin(new Insets(1, 1, 1, 1));
        this.setLayout(new FlowLayout());
        construireVueLigne();
    }

    private void construireVueLigne() {
        JTextField nomFormat = new JTextField(infosFormat.getNomFormat());
        nomFormat.setPreferredSize(DimensionsFormat.dNomFormat);
        nomFormat.setEditable(false);
        nomFormat.setBackground(Color.WHITE);
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

        JTextField minBuyIn = new JTextField("> " + infosFormat.getMinBuyIn() + "\u20AC");
        minBuyIn.setEditable(false);
        minBuyIn.setPreferredSize(DimensionsFormat.dBuyIn);
        this.add(minBuyIn);

        JTextField maxBuyIn = new JTextField("< " + infosFormat.getMaxBuyIn() + "\u20AC");
        maxBuyIn.setEditable(false);
        maxBuyIn.setPreferredSize(DimensionsFormat.dBuyIn);
        this.add(maxBuyIn);

        JTextField nParties = new JTextField(infosFormat.getNombreParties() + " parties");
        nParties.setPreferredSize(DimensionsFormat.dParties);
        nParties.setEditable(false);
        this.add(nParties);

        boutonAffiche = boutonSupprimer;
        boutonAffiche.addActionListener(this);
        boutonAffiche.setPreferredSize(DimensionsFormat.dBoutonInfo);
        this.add(boutonAffiche);

        this.repaint();
    }

    protected void setBoutons(boolean edition) {
        this.remove(boutonAffiche);
        if (edition) {
            boutonAffiche = boutonChoisir;
        }
        else {
            boutonAffiche = boutonSupprimer;
        }
        boutonAffiche.setEnabled(true);
        this.add(boutonAffiche);
        this.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boutonChoisir) {
            panneauParent.formatSelectionne(infosFormat.getIdBDD());
        }

        else if (e.getSource() == boutonSupprimer) {
            panneauParent.demandeSuppressionLigne(infosFormat.getIdBDD(), infosFormat.getIndexAffichage());
        }
    }

    public void desactiverBouton() {
        boutonAffiche.setEnabled(false);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(800, 30);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(800, 30);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 30);
    }
}
