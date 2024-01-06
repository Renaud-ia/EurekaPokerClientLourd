package analyzor.vue.reutilisables;

import javax.swing.*;
import java.awt.*;

public class BandeauScrollBar extends JScrollPane {
    public BandeauScrollBar(JPanel panel) {
        super(panel);
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
}
