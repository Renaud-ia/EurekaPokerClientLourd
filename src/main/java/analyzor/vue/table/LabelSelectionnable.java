package analyzor.vue.table;

import javax.swing.*;
import java.awt.*;

class LabelSelectionnable extends JLabel {
    private static final Color couleurFond = new Color(63, 63, 63);
    LabelSelectionnable(String nom) {
        super(nom);
        this.setBackground(couleurFond);
    }

    LabelSelectionnable() {
        super();
        this.setBackground(couleurFond);
    }

    void selectionner() {
        setOpaque(true);
        repaint();
    }

    void deselectionner() {
        setOpaque(false);
        repaint();
    }
}
