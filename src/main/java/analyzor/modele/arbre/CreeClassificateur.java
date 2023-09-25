package analyzor.modele.arbre;

import analyzor.modele.parties.Situation;

public class CreeClassificateur {
    public Classificateur CreeClassificateur() {
        return new ClassificateurCumulatif();
    }
    public Classificateur CreeClassificateur(Situation situation) {
        //todo
        return null;
    }
}
