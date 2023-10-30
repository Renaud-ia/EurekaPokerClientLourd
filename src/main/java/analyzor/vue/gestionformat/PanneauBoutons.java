package analyzor.vue.gestionformat;

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
        this.setLayout(new FlowLayout());
        boutonSelection = new JButton("Mode s\u00E9lection");
        boutonEdition = new JButton("Mode \u00E9dition");
        boutonFermer = new JButton("Fermer");
        initialiserBoutons();
    }

    private void initialiserBoutons() {
        // TODO : on vérifie le mode la fenêtre parente
        boutonMode = boutonEdition;
        boutonMode.addActionListener(this);
        this.add(boutonMode);
        boutonFermer.addActionListener(this);
        this.add(boutonFermer);
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
            setBoutons();
            if (fenetreParente.isModeEdition()) {
                fenetreParente.setModeSelection(true);
            }
            else {
                fenetreParente.setModeEdition(true);
            }
        }
    }
}
