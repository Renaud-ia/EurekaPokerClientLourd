package analyzor.vue.licence;


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

/**
 * bloc pour saisir la licence
 * ne permet de rentrer que des clés de bon format
 * et affiche une case de rétractation
 */
class BlocSaisieLicence extends JPanel implements MouseListener {
    private static final Pattern regexSaisieLicence =
            Pattern.compile("^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$");
    private JFormattedTextField champSaisie;
    private JCheckBox caseRetractation;
    private JLabel lienCGU;
    BlocSaisieLicence() {
        super();
        initialiser();
    }

    private void initialiser() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

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

        this.add(champSaisie);

        caseRetractation = new JCheckBox(
                "Je suis conscient que l'activation de la licence entraine \n" +
                        "le retrait de mon droit de rétractation");
        caseRetractation.setSelected(false);
        this.add(caseRetractation);

        lienCGU = new JLabel("         Voir nos CGUV sur notre site pour plus d'infos");
        lienCGU.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lienCGU.addMouseListener(this);
        this.add(lienCGU);

    }

    public void changerTexte(String cleLicence) {
        champSaisie.setText(cleLicence);
    }

    public void estEditable(boolean b) {
        champSaisie.setEditable(b);
    }

    /**
     * retourne le message lié au remplissage du bloc
     * @return null si tout est bon, le message d'erreur sinon
     */
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
