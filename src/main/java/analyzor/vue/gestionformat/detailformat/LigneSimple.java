package analyzor.vue.gestionformat.detailformat;

import analyzor.vue.gestionformat.LigneFormat;

import javax.swing.*;
import java.awt.*;
import java.util.jar.JarEntry;

public class LigneSimple extends JPanel {
    public LigneSimple(String nomInfo, String valeurInfo) {
        super();
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel labelnom = new JLabel(nomInfo);
        this.add(labelnom);

        JLabel labelValeur = new JLabel(valeurInfo);
        this.add(labelValeur);
    }
}
