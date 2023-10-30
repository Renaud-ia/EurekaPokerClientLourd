package analyzor.vue.gestionformat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * un des JPanel de CardLayout LigneInfosCalcul
  */

public class InfoCalcul extends JPanel implements ActionListener {
    private final JLabel texteEtat;
    private final JButton boutonCalculer;
    private final LigneCalcul ligne;
    protected InfoCalcul(LigneCalcul ligne, String etat) {
        this.ligne = ligne;

        this.setLayout(new BorderLayout());
        texteEtat = new JLabel();
        texteEtat.setText(etat);
        this.add(texteEtat, BorderLayout.WEST);

        boutonCalculer = new JButton("CALCUL");
        boutonCalculer.addActionListener(this);
        this.add(boutonCalculer, BorderLayout.EAST);

    }

    protected void changerEtat(String etat) {
        texteEtat.setText(etat);
        this.repaint();
    }

    protected void setBoutonCalculer(boolean active) {
        boutonCalculer.setEnabled(active);
        this.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ligne.clicCalculer();
    }
}
