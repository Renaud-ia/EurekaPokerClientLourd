package analyzor.vue.table;

import analyzor.vue.reutilisables.PanneauFonceArrondi;

import javax.swing.*;
import java.awt.*;

public class PanneauHaut extends JScrollPane {
    private final static JPanel panneau = new JPanel();
    public PanneauHaut() {
        super(panneau);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panneau.setLayout(new FlowLayout(FlowLayout.LEFT));
        setPreferredSize(new Dimension(500, CadreBandeau.hauteur + 10));
        setBorder(null);

        Dimension d = horizontalScrollBar.getPreferredSize();
        d.height = 5;
        horizontalScrollBar.setPreferredSize(d);
    }

    @Override
    public Component add(Component component) {
        panneau.add(component);
        return component;
    }

    @Override
    public void remove(Component component) {
        panneau.remove(component);
    }

    @Override
    public void removeAll() {
        panneau.removeAll();
    }
}
