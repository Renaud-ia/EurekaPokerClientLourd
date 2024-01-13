package analyzor.vue.table;

import javax.swing.*;
import java.awt.*;

class LabelSelectionnable extends JLabel {
    LabelSelectionnable(String nom) {
        super(nom);

    }

    LabelSelectionnable() {
        super();
        this.setBackground(Color.cyan);
    }

    void selectionner() {
        setOpaque(true);
    }

    void deselectionner() {
        setOpaque(false);
    }
}
