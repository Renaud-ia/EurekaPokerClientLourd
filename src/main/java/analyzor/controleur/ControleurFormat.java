package analyzor.controleur;

import analyzor.modele.parties.Variante;
import analyzor.vue.donnees.DAOFormat;
import analyzor.vue.gestionformat.FenetreFormat;
import analyzor.vue.vues.VuePrincipale;

public class ControleurFormat implements ControleurSecondaire {
    private final ControleurPrincipal controleurPrincipal;
    private final FenetreFormat vue;
    private final DAOFormat daoFormat;

    public ControleurFormat(VuePrincipale vuePrincipale, ControleurPrincipal controleur) {
        this.controleurPrincipal = controleur;
        daoFormat = new DAOFormat();
        this.vue = new FenetreFormat(vuePrincipale, this, daoFormat);
    }

    @Override
    public void demarrer() {
        lancerVue();
    }

    @Override
    public void lancerVue() {
        this.vue.setModeSelection(true);
        this.vue.setVisible(true);
    }

    @Override
    public void desactiverVue() {
        this.vue.setVisible(false);
    }

    public void creerFormat(
            Variante.PokerFormat pokerFormat,
            boolean ante,
            boolean ko,
            int nJoueurs,
            int minBuyIn,
            int maxBuyIn) {
        //todo récupérer nouvelles parties + enregistrer dans le Gestionnaire
        Long idBDD = 5412154L;
        int nParties = 0;
        int nouvellesParties = 0;
        daoFormat.ajouterFormat(idBDD, pokerFormat.toString(), ante, ko, nJoueurs, minBuyIn, maxBuyIn,
                nParties, nouvellesParties, false, false);
        this.vue.actualiser();
    }

    public void lancerCalcul(Long idBDD) {
        System.out.println("calcul lancé" + idBDD);
    }

    public void reinitialiser(Long idBDD) {
        System.out.println("réinitialiser" + idBDD);
    }
}
