package analyzor.vue.composants;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class CadreClassique extends JPanel {
    public CadreClassique(String name) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        EmptyBorder bordureInterne = new EmptyBorder(5, 5, 5, 5);
        Border bordureArrondie = BorderFactory.createLineBorder(Color.GRAY);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(bordureArrondie, name);
        CompoundBorder bordureTotale = new CompoundBorder(titledBorder, bordureInterne);
        setBorder(bordureTotale);
        setBackground(Color.WHITE); // Couleur de fond bleue
    }
    public CadreClassique() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        EmptyBorder bordureInterne = new EmptyBorder(5, 5, 5, 5);
        Border bordureArrondie = BorderFactory.createLineBorder(Color.GRAY);
        CompoundBorder bordureTotale = new CompoundBorder(bordureArrondie, bordureInterne);
        setBorder(bordureTotale);
        setBackground(Color.WHITE); // Couleur de fond bleue
    }
}
