package analyzor.vue.gestionformat.detailformat;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;

public class LigneDate extends JPanel {
    private JFormattedTextField champSaisie;
    public LigneDate(String nomValeur, String dateInitiale) {
        super();
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel labelnom = new JLabel(nomValeur);
        this.add(labelnom);

        try {
            MaskFormatter formatter = new MaskFormatter("**/**/****");
            formatter.setPlaceholderCharacter('*');
            champSaisie = new JFormattedTextField(formatter);
        }
        catch (ParseException parseException) {
            champSaisie = new JFormattedTextField();
        }
        champSaisie.setColumns(10);
        champSaisie.setEditable(true);
        champSaisie.setText(dateInitiale);
        this.add(champSaisie);
    }

    public String getValeurChampSaisie() {
        return champSaisie.getText();
    }
}
