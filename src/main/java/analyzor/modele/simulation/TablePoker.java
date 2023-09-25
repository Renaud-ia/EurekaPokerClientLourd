package analyzor.modele.simulation;

import analyzor.modele.parties.Action;

import java.util.ArrayList;
import java.util.List;

public class TablePoker {
    private List<Joueur> joueurs = new ArrayList<>();
    public TablePoker(){
        reset();
    }

    public void testInitialisation() {
        fixerNJoueurs(6);
    }

    public void fixerNJoueurs(int nombreJoueurs) {
        for (int i = 0; i < nombreJoueurs; i++) {
            joueurs.add(new Joueur());
        }
    }

    public List<Action> listeActions() {
        //retourne la suite d'actions possibles
        return null;
    }

    public void reset() {
        joueurs.clear();
    }
}
