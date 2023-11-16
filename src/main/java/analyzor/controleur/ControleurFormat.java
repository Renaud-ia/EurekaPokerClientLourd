package analyzor.controleur;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.GestionnaireFormat;
import analyzor.modele.parties.Variante;
import analyzor.vue.donnees.DAOFormat;
import analyzor.vue.gestionformat.FenetreFormat;
import analyzor.vue.vues.VuePrincipale;

import java.util.List;

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
        List<FormatSolution> listFormats = GestionnaireFormat.formatsDisponibles();
        for (FormatSolution format : listFormats) {
            daoFormat.ajouterFormat(format.getId(), format.getNomFormat().toString(), format.getAnte(), format.getKO(),
                    format.getNombreJoueurs(), (float) format.getMinBuyIn(), (float) format.getMaxBuyIn(),
                    format.getNombreParties(), format.getNouvellesParties(), format.getPreflopCalcule(), format.getFlopCalcule());
        }
        this.vue.actualiser();

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

    public boolean creerFormat(
            Variante.PokerFormat pokerFormat,
            boolean ante,
            boolean ko,
            int nJoueurs,
            int minBuyIn,
            int maxBuyIn) {

        FormatSolution nouveauFormat = new FormatSolution(pokerFormat, ante, ko, nJoueurs, minBuyIn, maxBuyIn);
        FormatSolution formatCree=  GestionnaireFormat.ajouterFormat(nouveauFormat);

        if (formatCree == null) return false;

        Long idBDD = formatCree.getId();
        int nParties = formatCree.getNombreParties();

        int nouvellesParties = 0;
        daoFormat.ajouterFormat(idBDD, pokerFormat.toString(), ante, ko, nJoueurs, minBuyIn, maxBuyIn,
                nParties, nouvellesParties, false, false);
        this.vue.actualiser();

        return true;
    }

    public WorkerAffichable lancerCalcul(Long idBDD) {

        return new WorkerTest("calcul", 500);
    }

    public void reinitialiser(Long idBDD) {
        // todo supprimer les ranges associées
        System.out.println("réinitialiser" + idBDD);
    }

    public void formatSelectionne(Long idBDD) {
        //todo sélectionner la partie dans écran d'accueil
    }

    public void supprimerFormat(Long idBDD, int indexAffichage) {
        GestionnaireFormat.supprimerFormat(idBDD);
        daoFormat.supprimerFormat(indexAffichage);
        this.vue.actualiser();
    }

    public void ajouterParties(Long idBDD) {
        //todo à faire : quand importation changer le nombre de mains
    }
}
