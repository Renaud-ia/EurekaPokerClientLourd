package analyzor.vue.donnees.table;

import analyzor.modele.parties.Variante;
import analyzor.vue.donnees.table.DTOJoueur;

import java.util.ArrayList;
import java.util.List;

public class ConfigTable {
    private Variante.PokerFormat pokerFormat;
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

    public void setPokerFormat(Variante.PokerFormat pokerFormat) {
        this.pokerFormat = pokerFormat;
    }

    public Variante.PokerFormat getFormat() {
        return pokerFormat;
    }
}
