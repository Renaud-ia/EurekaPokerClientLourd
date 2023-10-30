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
    public LancerCalcul(LigneCalcul ligneCalcul, boolean preflopCalcule, boolean flopCalcule) {
        this.ligne = ligneCalcul;

        this.setLayout(new FlowLayout());
        boxPreflop = new JCheckBox("Préflop");
        boxPreflop.setEnabled(!preflopCalcule);
        this.add(boxPreflop);

        boxFlop = new JCheckBox("Flop");
        boxFlop.setEnabled(!preflopCalcule);
        this.add(boxFlop);

        boutonCalculer = new JButton("Lancer le calcul");
        this.add(boutonCalculer);

        boutonReinitialiser = new JButton("Réinitialiser");
        this.add(boutonReinitialiser);

        boutonOk = new JButton("OK");
        this.add(boutonOk);

        this.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent evenement) {
        if (evenement.getSource() == boutonCalculer) {
            ligne.clicCalculer();
        }
        else if (evenement.getSource() == boutonReinitialiser) {
            ligne.clickReinitialiser();
        }

        else {
            ligne.clicOk();
        }
    }
}
