package analyzor.vue.donnees;

import java.util.ArrayList;
import java.util.List;

public class ConfigTable {
    private List<DTOJoueur> joueurs;
    private boolean bounty;

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

    public void setBounty(boolean ko) {
        this.bounty = ko;
    }

    public boolean getBounty() {
        return bounty;
    }

    public boolean estInitialisee() {
        return !joueurs.isEmpty();
    }
}
