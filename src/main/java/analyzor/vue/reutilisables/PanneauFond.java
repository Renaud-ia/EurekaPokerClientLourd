package analyzor.vue.reutilisables;

import javax.swing.*;
import analyzor.vue.basiques.CouleursDeBase;

public class PanneauFond extends JPanel {
    public PanneauFond() {
        super();
        this.setBackground(CouleursDeBase.FOND_FENETRE);
        this.setOpaque(true);
    }
}
