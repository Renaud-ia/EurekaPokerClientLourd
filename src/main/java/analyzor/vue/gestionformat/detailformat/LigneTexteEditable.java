package analyzor.vue.gestionformat.detailformat;

import analyzor.vue.gestionformat.NouveauFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LigneTexteEditable extends JPanel implements ActionListener {
    private final NouveauFormat nouveauFormat;
    private JTextField textField;
    private JButton editer;
    private JButton auto;
    private boolean modifie;
    public LigneTexteEditable(String nom, NouveauFormat nouveauFormat) {
        super();

        this.nouveauFormat = nouveauFormat;

        this.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel labelNom = new JLabel(nom);
        this.add(labelNom);

        textField = new JTextField();
        textField.setEditable(false);
        this.add(textField);

        editer = new JButton("Editer");
        editer.addActionListener(this);
        this.add(editer);

        auto = new JButton("Auto");
        auto.addActionListener(this);

        this.modifie = false;
    }

    public boolean estModifiable() {
        return !modifie;
    }


    public String getValeur() {
        return textField.getText();
    }

    public void setValeur(String string) {
        textField.setText(string);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == editer) {
            this.remove(editer);
            textField.setEditable(true);
            modifie = true;
            this.add(auto);

            this.revalidate();
            this.repaint();
        }

        if (e.getSource() == auto) {
            this.remove(auto);
            textField.setEditable(false);
            modifie = false;
            this.add(editer);

            this.revalidate();
            this.repaint();

            nouveauFormat.remplissageAutomatiqueNom();
        }
    }
}
