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
    public LigneSlider(String nomChoix, int valeurMin, int valeurMax, NouveauFormat nouveauFormat) {
        super();
        this.nouveauFormat = nouveauFormat;
        this.setLayout(new FlowLayout());
        JLabel labelnom = new JLabel(nomChoix);
        this.add(labelnom);

        slider = new JSlider(valeurMin, valeurMax);
        slider.addChangeListener(this);
        this.add(slider);
        slider.setMinorTickSpacing(valeurMin);
        slider.setMajorTickSpacing(valeurMax);
        slider.setPaintTicks(true);

        valeurLabel = new JLabel("0");
        this.add(valeurLabel);
    }

    public int getValeurSlider() {
        return slider.getValue();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        valeurLabel.setText(String.valueOf(slider.getValue()));
        nouveauFormat.remplissageAutomatiqueNom();
    }
}
