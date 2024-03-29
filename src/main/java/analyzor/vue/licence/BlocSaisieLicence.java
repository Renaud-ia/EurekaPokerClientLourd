package analyzor.vue.licence;


import analyzor.vue.basiques.Polices;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class BlocSaisieLicence extends JPanel implements MouseListener {
    private static final Pattern regexSaisieLicence =
            Pattern.compile("^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$");
    private JPanel panneauBoutons;
    private JFormattedTextField champSaisie;
    private JCheckBox caseRetractation;
    private JLabel lienCGU;
    BlocSaisieLicence() {
        super();
        initialiser();
    }

    private void initialiser() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel panneauSaisie = new JPanel();
        panneauSaisie.setLayout(new FlowLayout());

        try {
            MaskFormatter formatter = new MaskFormatter("****-****-****-****");
            formatter.setPlaceholderCharacter('*');
            champSaisie = new JFormattedTextField(formatter);
        }
        catch (ParseException parseException) {
            champSaisie = new JFormattedTextField();
        }
        champSaisie.setColumns(16);
        champSaisie.setEditable(false);
        panneauSaisie.add(champSaisie);

        panneauBoutons = new JPanel();
        panneauBoutons.setLayout(new FlowLayout());
        panneauSaisie.add(panneauBoutons);

        this.add(panneauSaisie);

        caseRetractation = new JCheckBox(
                "Je suis conscient que l'activation de la licence entraine \n" +
                        "le retrait de mon droit de r\u00E9tractation");
        caseRetractation.setFont(Polices.standard);
        caseRetractation.setSelected(false);
        this.add(caseRetractation);

        lienCGU = new JLabel("         Voir nos CGUV sur notre site pour plus d'infos");
        lienCGU.setFont(Polices.italiquePetit);
        lienCGU.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lienCGU.addMouseListener(this);
        this.add(lienCGU);

    }

    public void changerTexte(String cleLicence) {
        champSaisie.setText(cleLicence);
    }

    public void estEditable(boolean editable) {
        champSaisie.setEditable(editable);
        caseRetractation.setVisible(editable);
        lienCGU.setVisible(editable);
    }


    public String estValide() {
        champSaisie.setText(champSaisie.getText().toUpperCase());
        if (!caseRetractation.isSelected()) return "La case n'est pas cochée";

        Matcher matcher = regexSaisieLicence.matcher(champSaisie.getText());
        if (!matcher.find()) {
            return "Le format de clé saisi n'est pas bon";
        }

        return null;
    }

    public String getCleSaisie() {
        return champSaisie.getText();
    }

    void viderPanneauBoutons() {
        panneauBoutons.removeAll();
    }

    void ajouterBouton(JButton bouton) {
        panneauBoutons.add(bouton);
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (event.getSource() == lienCGU) {
            try {
                Desktop.getDesktop().browse(new URI("https://www.eureka-poker.fr/conditions/"));
            }
            catch (Exception ignored) {}
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
