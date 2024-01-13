package analyzor.vue.reutilisables;

import analyzor.vue.couleurs.CouleursDeBase;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class CadreLarge extends JPanel {
    public CadreLarge() {
        super();
        setLayout(new BorderLayout());
        EmptyBorder bordureInterne = new EmptyBorder(25, 25, 25, 25);
        setBorder(bordureInterne);
        setBackground(CouleursDeBase.FOND_FONCE); // Couleur de fond bleue
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}
