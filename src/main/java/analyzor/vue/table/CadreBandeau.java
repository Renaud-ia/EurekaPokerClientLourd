package analyzor.vue.table;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class CadreBandeau extends JPanel {
    public CadreBandeau(String name) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        EmptyBorder bordureInterne = new EmptyBorder(5, 5, 5, 5);
        Border bordureArrondie = BorderFactory.createLineBorder(Color.GRAY);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(bordureArrondie, name);
        CompoundBorder bordureTotale = new CompoundBorder(titledBorder, bordureInterne);
        setBorder(bordureTotale);
        setBackground(Color.WHITE);
    }
    public CadreBandeau() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        EmptyBorder bordureInterne = new EmptyBorder(5, 5, 5, 5);
        Border bordureArrondie = BorderFactory.createLineBorder(Color.GRAY);
        CompoundBorder bordureTotale = new CompoundBorder(bordureArrondie, bordureInterne);
        setBorder(bordureTotale);
        setBackground(Color.WHITE);
    }
}
