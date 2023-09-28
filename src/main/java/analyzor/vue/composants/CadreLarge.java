package analyzor.vue.composants;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class CadreLarge extends JPanel {
    public CadreLarge() {
        super();
        setLayout(new BorderLayout());
        EmptyBorder bordureInterne = new EmptyBorder(25, 25, 25, 25);
        Border bordureArrondie = BorderFactory.createLineBorder(Color.GRAY);
        CompoundBorder bordureTotale = new CompoundBorder(bordureArrondie, bordureInterne);
        setBorder(bordureTotale);
        setBackground(Color.GRAY); // Couleur de fond bleue
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}
