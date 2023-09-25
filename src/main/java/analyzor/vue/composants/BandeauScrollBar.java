package analyzor.vue.composants;

import javax.swing.*;
import java.awt.*;

public class BandeauScrollBar extends JScrollPane {
    public BandeauScrollBar() {
        super();
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
}
