package analyzor.controleur;

import analyzor.modele.simulation.TablePoker;
import analyzor.vue.donnees.InfosAction;
import analyzor.vue.donnees.InfosSolution;
import analyzor.vue.vues.VueAccueil;
import analyzor.vue.vues.VuePrincipale;

public class ControleurAccueil implements ControleurSecondaire {
    private TablePoker tablePoker = new TablePoker();
    private VuePrincipale vuePrincipale;
    private VueAccueil vueAccueil;
    private boolean modeManuel = true;
    //todo : ajouter les solutions
    public ControleurAccueil(VuePrincipale vuePrincipale) {
        vueAccueil = new VueAccueil(vuePrincipale, this);

        construireVue();
    }

    public void construireVue() {
        // à chaque rechargement solution / table / action, va reconstruire les informations de la table
        InfosSolution infosSolution = new InfosSolution();
        vueAccueil.afficherSolution(infosSolution);
        vueAccueil.afficherTable();
        InfosAction infosAction = new InfosAction("UTG");
        infosAction.ajouterAction("raises", 3);
        infosAction.ajouterAction("fold", 0);
        vueAccueil.ajouterAction(infosAction);
    }

    public void clickSolution() {


    }

    public void clickGestionTable() {


    }

    public void clickAction(String ActionClicke) {
        if (modeManuel) {
            System.out.println(ActionClicke);
            InfosAction infosAction = new InfosAction("UTG");
            infosAction.ajouterAction("raises", 3);
            infosAction.ajouterAction("fold", 0);
            vueAccueil.ajouterAction(infosAction);
        }
    }

    public void clickBoard() {

    }

    public void clickCombo() {

    }

    @Override
    public void demarrer() {
        //todo => remettre le code là dedans

    }

    @Override
    public void lancerVue() {
        vueAccueil.setVisible(true);
    }

    @Override
    public void desactiverVue() {
        vueAccueil.setVisible(false);
    }
}
