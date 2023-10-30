package analyzor.vue.composants;

import analyzor.vue.Couleurs;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class CadreLarge extends JPanel {
    public CadreLarge() {
        super();
        setLayout(new BorderLayout());
        EmptyBorder bordureInterne = new EmptyBorder(25, 25, 25, 25);
        setBorder(bordureInterne);
        setBackground(Couleurs.FOND_FONCE); // Couleur de fond bleue
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}
