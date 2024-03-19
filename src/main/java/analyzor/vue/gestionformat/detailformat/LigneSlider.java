package analyzor.vue.gestionformat.detailformat;

import analyzor.vue.gestionformat.NouveauFormat;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class LigneSlider extends JPanel implements ChangeListener {
    private final NouveauFormat nouveauFormat;
    private JSlider slider;
    private JLabel valeurLabel;
    public LigneSlider(String nomChoix, int valeurMin, int valeurMax, int valeurInitiale, NouveauFormat nouveauFormat) {
        super();
        this.nouveauFormat = nouveauFormat;
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel labelnom = new JLabel(nomChoix);
        this.add(labelnom);

        slider = new JSlider(valeurMin, valeurMax, valeurInitiale);
        slider.addChangeListener(this);
        this.add(slider);
        slider.setMinorTickSpacing(valeurMin);
        slider.setMajorTickSpacing(valeurMax);
        slider.setPaintTicks(true);

        valeurLabel = new JLabel(String.valueOf(valeurInitiale));
        this.add(valeurLabel);
    }

    public int getValeurSlider() {
        return slider.getValue();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // besoin de cette ligne quand on fixe la valeur initiale du slider
        //if (valeurLabel == null) return;
        valeurLabel.setText(String.valueOf(slider.getValue()));
        nouveauFormat.remplissageAutomatiqueNom();
    }
}
