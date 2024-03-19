package analyzor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TestSuperposition extends JFrame {
    public TestSuperposition() {
            super();
            this.setSize(new Dimension(500, 500));
            JLayeredPane jLayeredPane = getLayeredPane();

            JPanel panneauFond = new JPanel();
            panneauFond.setSize(new Dimension(200,200));
            panneauFond.setBackground(Color.RED);
            jLayeredPane.add(panneauFond, JLayeredPane.DEFAULT_LAYER);

            JPanel panneauTexte = new JPanel();
            EmptyBorder bordureInterne = new EmptyBorder(10, 10, 10, 10);
            panneauTexte.setBorder(bordureInterne);
            panneauTexte.setBounds(50, 50, 100, 100);
            panneauTexte.setSize(new Dimension(100, 100));
            JTextArea label = new JTextArea("SLAFOIUGHEGG \n fezjgfehgegz");
            label.repaint();
            System.out.println(label.getSize());
            label.setEditable(false);
            panneauTexte.add(label);
            jLayeredPane.add(panneauTexte, JLayeredPane.MODAL_LAYER);

            jLayeredPane.setVisible(true);

            this.setVisible(true);
        }



    public static void main(String[] args) {
        TestSuperposition fenetreSuperposition = new TestSuperposition();
    }
}
