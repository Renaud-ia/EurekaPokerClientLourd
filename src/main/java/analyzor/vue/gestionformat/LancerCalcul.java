package analyzor.vue.gestionformat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * un des JPanel de CardLayout LigneInfosCalcul
 */
public class LancerCalcul extends JPanel implements ActionListener {
    private JButton boutonCalculer;
    private JButton boutonReinitialiser;
    private JButton boutonOk;
    private JCheckBox boxPreflop;
    private JCheckBox boxFlop;
    private final LigneCalcul ligne;
    public LancerCalcul(LigneCalcul ligneCalcul, boolean preflopCalcule, boolean flopCalcule, int nParties) {
        this.ligne = ligneCalcul;
        this.setLayout(new FlowLayout());
        boxPreflop = new JCheckBox("Pr\u00E9flop");
        boxPreflop.setEnabled(nParties > 0);
        boxPreflop.setSelected(preflopCalcule);
        this.add(boxPreflop);

        boxFlop = new JCheckBox("Flop");
        boxFlop.setEnabled(nParties > 0);
        boxFlop.setSelected(flopCalcule);
        boxFlop.addActionListener(this);
        this.add(boxFlop);

        boutonCalculer = new JButton("Lancer le calcul");
        if (nParties == 0) boutonCalculer.setEnabled(false);
        boutonCalculer.addActionListener(this);
        this.add(boutonCalculer);

        boutonReinitialiser = new JButton("R\u00E9initialiser");
        boutonReinitialiser.addActionListener(this);
        this.add(boutonReinitialiser);

        boutonOk = new JButton("OK");
        boutonOk.addActionListener(this);
        this.add(boutonOk);

        this.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent evenement) {
        if (evenement.getSource() == boutonCalculer) {
            ligne.clicLancerCalcul();
        }
        else if (evenement.getSource() == boutonReinitialiser) {
            ligne.clickReinitialiser();
        }

        else if (evenement.getSource() == boutonOk){
            ligne.clicOk();
        }

        else if (evenement.getSource() == boxFlop) {
            if (boxFlop.isSelected()) boxPreflop.setSelected(true);
        }
    }

    public void setParties(int nombreParties) {
        if (nombreParties > 0) {
            boxPreflop.setEnabled(true);
            boxFlop.setEnabled(true);
            boutonCalculer.setEnabled(true);
        }
    }
}
