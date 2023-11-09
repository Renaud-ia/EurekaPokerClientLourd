package analyzor.modele.arbre.noeuds;

import analyzor.modele.arbre.OppositionRanges;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
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
    private OppositionRanges oppositionRanges;
    private List<NoeudAction> arbreActions;
    private int[] observations;

    public NoeudDenombrable() {
        this.entreesCorrespondantes = new HashMap<>();
    }

    public void ajouterNoeud(NoeudAction noeudAction, List<Entree> entrees) {
        this.entreesCorrespondantes.put(noeudAction, entrees);
    }

    public void ajouterRanges(OppositionRanges oppositionRanges) {
        this.oppositionRanges = oppositionRanges;
    }

}
