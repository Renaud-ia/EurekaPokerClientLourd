package analyzor.vue.gestionformat;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// contient les boutons de changement de mode et fermer la fenêtre
public class PanneauBoutons extends JPanel implements ActionListener {
    private final FenetreFormat fenetreParente;
    private JButton boutonMode;
    private final JButton boutonSelection;
    private final JButton boutonEdition;
    private final JButton boutonFermer;

    protected PanneauBoutons(FenetreFormat fenetreParente) {
        this.fenetreParente = fenetreParente;
        this.setLayout(new MigLayout("", "[grow][]", "[]10[]")); // 10 pixels d'espace entre les boutons
        boutonSelection = new JButton("Mode s\u00E9lection");
        boutonEdition = new JButton("Mode \u00E9dition");
        boutonFermer = new JButton("Fermer");
        initialiserBoutons();
    }

    private void initialiserBoutons() {
        // TODO : on vérifie le mode la fenêtre parente
        boutonMode = boutonEdition;
        boutonMode.setPreferredSize(new Dimension(100, 15));
        boutonMode.addActionListener(this);
        this.add(boutonMode, "cell 0 0, align center"); // colonne 0, ligne 0, aligné à droite
        boutonFermer.addActionListener(this);
        this.add(boutonFermer, "cell 1 0, align right"); // colonne 0, ligne 1, aligné à droite
        setBoutons();
    }

    protected void setBoutons() {
        this.remove(boutonMode);
        if (fenetreParente.isModeEdition()) {
            boutonMode = boutonSelection;
        }
        else {
            boutonMode = boutonEdition;
        }
        boutonMode.addActionListener(this);
        boutonMode.setPreferredSize(new Dimension(100, 15));
        this.add(boutonMode, 0); // ajoute le bouton au début
        this.revalidate();
        this.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent evenement) {
        if (evenement.getSource() == boutonFermer) {
            this.fenetreParente.desactiverVue();
        }
        else if (evenement.getSource() == boutonMode) {
            if (fenetreParente.isModeEdition()) {
                fenetreParente.setModeSelection(true);
            }
            else {
                fenetreParente.setModeEdition(true);
            }
            setBoutons();
        }
    }

    public void setActif(boolean etat) {
        boutonMode.setEnabled(etat);
    }
}
