package analyzor.vue.table;

import analyzor.vue.donnees.table.RangeVisible;
import analyzor.vue.reutilisables.PanneauFonceArrondi;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * bandeau sous la range qui permet d'exporter la range
 */
public class TexteRange extends PanneauFonceArrondi implements ActionListener {
    private final RangeVisible rangeVisible;
    private JTextField texteCopiable;
    private JComboBox listeLogiciels;
    private JButton boutonCopier;
    public static final int HAUTEUR_BANDEAU = 40;
    public static final int HAUTEUR_COMPOSANTS = 25;
    public String choixFlopZilla = "Flopzilla/GTO+";
    public String choixPio = "PioSOLVER";
    public TexteRange(RangeVisible rangeVisible, int largeurRange) {
        super();
        this.rangeVisible = rangeVisible;
        MARGE_HORIZONTALE = 1;
        MARGE_VERTICALE = 1;
        this.setLayout(new FlowLayout());

        texteCopiable = new JTextField();
        texteCopiable.setEditable(false);
        this.add(texteCopiable);

        String[] donnees = {choixFlopZilla, choixPio};
        listeLogiciels = new JComboBox<>(donnees);
        listeLogiciels.addActionListener(this);
        this.add(listeLogiciels);

        boutonCopier = new JButton("Copier");
        boutonCopier.addActionListener(this);
        this.add(boutonCopier);

        setLargeur(largeurRange);
    }

    public void setTexte() {
        texteCopiable.setText(rangeVisible.chaineCaracteres());
    }

    public void setLargeur(int largeurRange) {
        this.setPreferredSize(new Dimension(largeurRange, HAUTEUR_BANDEAU));
        Dimension dimensionTexte =
                new Dimension((largeurRange - listeLogiciels.getWidth() - boutonCopier.getWidth() - 20), HAUTEUR_COMPOSANTS);
        texteCopiable.setPreferredSize(dimensionTexte);
    }

    public void actualiser(int largeurRange) {
        setLargeur(largeurRange);
        setTexte();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boutonCopier) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(texteCopiable.getText());
            clipboard.setContents(stringSelection, null);
        }
    }
}
