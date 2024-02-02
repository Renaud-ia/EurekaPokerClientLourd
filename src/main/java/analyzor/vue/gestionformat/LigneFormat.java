package analyzor.vue.gestionformat;

import analyzor.controleur.ControleurFormat;
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
    private JLabel statutFormat;
    private JButton gererFormat;
    public LigneFormat(ControleurFormat controleur, DTOFormat formatTrouve, FenetreFormat panneauParent) {
        super();
        this.controleurFormat = controleur;
        this.format = formatTrouve;
        this.panneauParent = panneauParent;

        initialiser();
    }

    private void initialiser() {
        this.setLayout(new FlowLayout());

        boutonChoisir = new JButton("Choisir ce format");
        boutonChoisir.addActionListener(this);
        this.add(boutonChoisir);

        labelNomFormat = new JLabel();
        this.add(labelNomFormat);

        dateFormat = new JLabel();
        this.add(dateFormat);

        statutFormat = new JLabel();
        this.add(statutFormat);

        gererFormat = new JButton("Gérer");
        gererFormat.addActionListener(this);
        this.add(gererFormat);

        actualiser();
    }

    public void actualiser() {
        System.out.println("ACTUALISATION objet : " + format);
        // pour test
        boutonChoisir.setEnabled(true);
        //boutonChoisir.setEnabled(format.selectionnable());
        labelNomFormat.setText(format.getNomFormat());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateFormat.setText("Créé le " + format.getDateCreation().format(formatter));
        statutFormat.setText(format.getStatut());

        this.repaint();
        this.revalidate();
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
