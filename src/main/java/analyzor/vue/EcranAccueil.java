package analyzor.vue;

import javax.swing.*;

/**
 * page de chargement lorsqu'on lance le logiciel
 */
public class EcranAccueil extends JDialog {
    EcranAccueil(FenetrePrincipale fenetrePrincipale) {
        super(fenetrePrincipale, "Lancement", true);
    }

    void demarrer() {

    }

    /**
     * la fenêtre ne sera jamais réutilisée
     */
    void arreter() {
        this.dispose();
    }
}
