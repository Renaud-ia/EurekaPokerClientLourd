package analyzor.controleur;

import analyzor.modele.simulation.TablePoker;
import analyzor.vue.donnees.InfosSolution;
import analyzor.vue.vues.VueAccueil;
import analyzor.vue.vues.VuePrincipale;

public class ControleurAccueil {
    private TablePoker tablePoker = new TablePoker();
    private VuePrincipale vuePrincipale;
    private VueAccueil vueAccueil;
    //todo : ajouter les solutions
    public ControleurAccueil(VuePrincipale vuePrincipale) {
        vueAccueil = new VueAccueil(vuePrincipale, this);
        tablePoker.testInitialisation();
        construireVue();
    }

    public void construireVue() {
        // à chaque rechargement solution / table / action, va reconstruire les informations de la table
        InfosSolution infosSolution = new InfosSolution();
        vueAccueil.afficherSolution(infosSolution);
        vueAccueil.afficherTable();
    }

    public void clickSolution() {
        construireVue();

    }

    public void clickGestionTable() {
        construireVue();
    }

    public void clickAction() {

    }

    public void clickBoard() {

    }

    public void clickCombo() {

    }

}
