package analyzor.vue.reutilisables.fenetres;

import analyzor.vue.FenetrePrincipale;
import analyzor.vue.reutilisables.fenetres.FenetreEnfant;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class FenetreTroisiemeOrdre extends FenetreEnfant {
    private final FenetreSecondOrdre fenetreParente;
    public FenetreTroisiemeOrdre(FenetreSecondOrdre fenetreParente, String nom, boolean modal) {
        super(fenetreParente, nom, true);
        System.out.println("FENETRE CREEE");
        this.fenetreParente = fenetreParente;
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                fermer();
            }
        });
    }

    public void afficher() {
        recentrer();
        fenetreParente.setVisible(false);
        this.setVisible(true);
    }

    public void recentrer() {
        this.setLocation(fenetreParente.getLocation().x, fenetreParente.getLocation().y);
    }

    public void fermer() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(false);
                fenetreParente.afficher();
            }
        });

    }


}
