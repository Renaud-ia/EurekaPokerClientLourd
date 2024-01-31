package analyzor.vue.gestionformat.detailformat;

import analyzor.vue.gestionformat.NouveauFormat;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class LigneSpinner extends JPanel implements ChangeListener {
    private final NouveauFormat nouveauFormat;
    JSpinner spinner;
    public LigneSpinner(String nomValeur, int minValeur, int maxValeur, NouveauFormat nouveauFormat) {
        super();
        this.nouveauFormat = nouveauFormat;

        this.setLayout(new FlowLayout());
        JLabel labelnom = new JLabel(nomValeur);
        this.add(labelnom);

        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(minValeur, minValeur, maxValeur, 1);
        spinner = new JSpinner(spinnerNumberModel);
        spinner.addChangeListener(this);
        this.add(spinner);
    }

    public int getValeurSlider() {
        return (int) spinner.getValue();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        nouveauFormat.remplissageAutomatiqueNom();
    }
}
