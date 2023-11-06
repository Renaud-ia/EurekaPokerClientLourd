package analyzor.modele.arbre.noeuds;

import analyzor.modele.parties.Entree;
import analyzor.modele.poker.RangeDenombrable;
import analyzor.modele.poker.RangeReelle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * noeud construit par les différents classificateurs
 * peut ensuite être utilisé par showdown/dénombrement/equilibrage
 * todo : faire les méthodes pour obtenir les données
 */
public class NoeudDenombrable {
    private final HashMap<NoeudAction, List<Entree>> entreesCorrespondantes;
    private RangeDenombrable rangeHero;
    private List<RangeReelle> rangesVillains;
    private NoeudAction[] arbreActions;
    private int[] observations;

    public NoeudDenombrable(RangeDenombrable range) {
        this.rangeHero = range;
        this.entreesCorrespondantes = new HashMap<>();
    }

    public void ajouterEntree(NoeudAction noeudAction, Entree nouvelleEntree) {
        List<Entree> listeEntrees = entreesCorrespondantes.computeIfAbsent(noeudAction, k -> new ArrayList<>());
        listeEntrees.add(nouvelleEntree);
    }
}
