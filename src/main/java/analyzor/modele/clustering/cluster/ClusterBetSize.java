package analyzor.modele.clustering.cluster;

import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

public class ClusterBetSize implements ClusterEntree {
    private float betSize;
    // on regroupe les clusters par idNoeudAbstrait = action
    // comme ça pas besoin de le refaire ensuite
    private List<Entree> entrees;

    public ClusterBetSize() {
        entrees = new ArrayList<>();
    }


    public void ajouterEntree(Entree entree) {
        this.entrees.add(entree);
    }

    public void setBetSize(float betSize) {
        this.betSize = betSize;
    }

    public int getEffectif() {
        return entrees.size();
    }

    public float getBetSize() {
        return betSize;
    }
}
