package analyzor.modele.simulation;

import analyzor.modele.parties.Action;
import analyzor.modele.poker.RangeReelle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TablePoker {
    private static String[] nomsPosition = {"MP", "HJ", "CO", "BTN", "SB", "BB"};
    private List<Joueur> joueurs = new ArrayList<>();
    private List<Action> situations = new ArrayList<>();

    public TablePoker() {
        reset();
    }

    public void testInitialisation() {
        setJoueurs(6);
    }

    public void setJoueurs(int nombreJoueurs)  {
        /*
        On fixe le nom des positions des joueurs selon le nombre, va servir de référence plus tard
         */
        int index = 0;
        while (nombreJoueurs > nomsPosition.length + index) {
            String nomPos = "UTG";
            nomPos += (index > 0) ? "+ " + Integer.toString(index) : "";
            index++;
        }

        int i = 0;
        if (nombreJoueurs < nomsPosition.length) {
            i = nomsPosition.length - nombreJoueurs;
        }
        for (; i < nombreJoueurs; i++) {
            joueurs.add(new Joueur(nomsPosition[i]));
        }
    }

    public List<Joueur> getJoueurs() {
        return null;
    }

    public void setStack(String positionJoueur, int stack) {

    }

    public List<Action> listeActions() {
        //retourne la suite d'actions possibles
        return null;
    }

    public void changerAction(int indexSituation, String nomAction) {
        // on supprime toutes les actions suivantes, on fixe chaque situation sur fixée
        // on recalcule les situations
        this.calculerSituations();
    }

    public RangeReelle obtenirRange(int indexSituation, String nomAction) {
        return null;
    }

    public void reset() {
        joueurs.clear();
    }

    private void calculerSituations() {
        // on regarde si action suivante (=pas leaf et (le joueur n'est pas fold et seulement une action de plus par joueur))
        // on ajoute une situation, on fixe action sur fold

    }

}
