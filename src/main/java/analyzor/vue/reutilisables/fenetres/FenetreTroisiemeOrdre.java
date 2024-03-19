package analyzor.vue.reutilisables.fenetres;

import analyzor.vue.FenetrePrincipale;
import analyzor.vue.reutilisables.fenetres.FenetreEnfant;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class FenetreTroisiemeOrdre extends FenetreEnfant {
    private final FenetreSecondOrdre fenetreParente;
    private boolean empecherFermeture;
    public FenetreTroisiemeOrdre(FenetreSecondOrdre fenetreParente, String nom, boolean modal) {
        super(fenetreParente, nom, true);
        this.fenetreParente = fenetreParente;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!empecherFermeture) fermer();
            }
        });

        empecherFermeture = false;
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

    @Override
    public void desactiverControles() {
        super.desactiverControles();
        empecherFermeture = true;
    }

    @Override
    public void reactiverControles() {
        super.reactiverControles();
        empecherFermeture = false;
    }


}
