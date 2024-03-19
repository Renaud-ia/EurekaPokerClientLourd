package analyzor.vue.gestionformat.detailformat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;

public class LigneComboBox extends JPanel {
    private JComboBox<String> choixFormat;
    public LigneComboBox(String nomChoix, String[] choix, ItemListener itemListener) {
        super();
        this.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel labelNom = new JLabel(nomChoix);
        this.add(labelNom);

        choixFormat = new JComboBox<>(choix);
        choixFormat.addItemListener(itemListener);
        choixFormat.setSelectedIndex(-1);
        this.add(choixFormat);
    }
}
