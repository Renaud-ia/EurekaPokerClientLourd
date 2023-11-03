package analyzor.modele.arbre;

import analyzor.modele.exceptions.*;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

/**
 * classificateur qui clusterise dynamiquement les situations selon la distribution des ranges
 * attention : on a besoin d'avoir calculé les ranges des Situation précédentes
 */
public class ClassificateurDynamique extends Classificateur{
    public ClassificateurDynamique() throws NonImplemente {
        throw new NonImplemente();
    }

    @Override
    public List<NoeudAvecRange> obtenirSituations(List<Entree> entreesSituation) {
        //todo
        return new ArrayList<>();
    }
}
