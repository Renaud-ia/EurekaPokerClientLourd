package analyzor.vue.table;

import analyzor.vue.reutilisables.PanneauFonceArrondi;
import analyzor.vue.reutilisables.PanneauFond;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PanneauRange extends PanneauFonceArrondi {
    public PanneauRange() {
        super();
        this.setLayout(new GridLayout(13, 13));
    }

}
