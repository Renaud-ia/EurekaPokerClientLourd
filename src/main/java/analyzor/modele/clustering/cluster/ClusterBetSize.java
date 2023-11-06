package analyzor.modele.clustering.cluster;

import analyzor.modele.parties.Entree;

import java.util.HashMap;
import java.util.List;

public class ClusterBetSize {
    private float betSize;
    // on regroupe les clusters par idNoeudAbstrait = action
    // comme ça pas besoin de le refaire ensuite
    private HashMap<Long, List<Entree>> entrees;
}
