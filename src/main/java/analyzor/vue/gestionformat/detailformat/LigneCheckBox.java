package analyzor.vue.gestionformat.detailformat;

import analyzor.vue.gestionformat.NouveauFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LigneCheckBox extends JPanel implements ActionListener {
    private final NouveauFormat nouveauFormat;
    private JCheckBox checkBox;
    public LigneCheckBox(String nomChoix, NouveauFormat nouveauFormat) {
        super();
        this.nouveauFormat = nouveauFormat;
        this.setLayout(new FlowLayout());

        JLabel labelNom = new JLabel(nomChoix);
        this.add(labelNom);

        checkBox = new JCheckBox();
        checkBox.addActionListener(this);
        this.add(checkBox);
    }

    public boolean estCoche() {
        return checkBox.isSelected();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        nouveauFormat.remplissageAutomatiqueNom();
    }
}
