package analyzor.vue.donnees;

import java.util.ArrayList;
import java.util.List;

public class ConfigTable {
    private List<DTOJoueur> joueurs;

    public ConfigTable() {
        joueurs = new ArrayList<>();
    }

    public List<DTOJoueur> getJoueurs() {
        return joueurs;
    }

    public void ajouterJoueur(DTOJoueur nouveauJoueur) {
        joueurs.add(nouveauJoueur);
    }

    public void viderJoueurs() {
        joueurs.clear();
    }
}
