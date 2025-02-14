package analyzor.vue.gestionformat;

import analyzor.controleur.ControleurFormat;
import analyzor.vue.basiques.Images;
import analyzor.vue.basiques.Polices;
import analyzor.vue.donnees.format.DTOFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;

public class LigneFormat extends JPanel implements ActionListener {
    private final ControleurFormat controleurFormat;
    private final DTOFormat format;
    private final FenetreFormat panneauParent;
    private JButton boutonChoisir;
    private JLabel labelNomFormat;
    private JLabel dateFormat;
    private JButton gererFormat;
    public LigneFormat(ControleurFormat controleur, DTOFormat formatTrouve, FenetreFormat panneauParent) {
        super(new FlowLayout(FlowLayout.LEFT));
        this.controleurFormat = controleur;
        this.format = formatTrouve;
        this.panneauParent = panneauParent;

        initialiser();
    }

    private void initialiser() {
        boutonChoisir = new JButton("Consulter");
        boutonChoisir.setIcon(new ImageIcon(Images.consulterResultats));
        boutonChoisir.addActionListener(this);
        this.add(boutonChoisir);

        gererFormat = new JButton("G\u00E9rer");
        gererFormat.setIcon(new ImageIcon(Images.gererFormat));
        gererFormat.addActionListener(this);
        this.add(gererFormat);

        labelNomFormat = new JLabel();
        labelNomFormat.setFont(Polices.standard);
        this.add(labelNomFormat);

        dateFormat = new JLabel();
        dateFormat.setFont(Polices.standard);
        dateFormat.setForeground(Color.GRAY);
        this.add(dateFormat);

        actualiser();
    }

    public void actualiser() {
        boutonChoisir.setEnabled(format.estConsultable());
        labelNomFormat.setText(format.getNomFormat());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateFormat.setText("Cr\u00E9\u00E9 le " + format.getDateCreation().format(formatter));

        this.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boutonChoisir) {
            controleurFormat.formatSelectionne(format);
        }

        else if (e.getSource() == gererFormat) {
            panneauParent.gestionFormat(format);
        }
    }
}
