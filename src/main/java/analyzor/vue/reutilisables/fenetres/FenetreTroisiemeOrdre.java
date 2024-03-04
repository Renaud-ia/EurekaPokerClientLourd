package analyzor.vue.reutilisables.fenetres;

import analyzor.vue.FenetrePrincipale;
import analyzor.vue.reutilisables.fenetres.FenetreEnfant;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class FenetreTroisiemeOrdre extends FenetreEnfant {
    private final JDialog fenetreParente;
    public FenetreTroisiemeOrdre(JDialog fenetreParente, String nom, boolean modal) {
        super(fenetreParente, nom, true);
        System.out.println("FENETRE CREEE");
        this.fenetreParente = fenetreParente;
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                fenetreParente.setVisible(true);
            }
        });
    }

    public void afficher() {
        recentrer();
        fenetreParente.setVisible(false);
        this.setVisible(true);
    }

    public void recentrer() {
        this.setLocationRelativeTo(fenetreParente);
    }


}
