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

        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        texteEtat = new JLabel();
        texteEtat.setText(etat);
        this.add(texteEtat);

        this.add(Box.createHorizontalStrut(25));

        boutonCalculer = new JButton("CALCUL");
        boutonCalculer.setMargin(new Insets(1, 1, 1, 1));
        boutonCalculer.setPreferredSize(DimensionsFormat.dBoutonInfo);
        boutonCalculer.addActionListener(this);
        this.add(boutonCalculer);

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

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(300, 30);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(300, 30);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 30);
    }
}
