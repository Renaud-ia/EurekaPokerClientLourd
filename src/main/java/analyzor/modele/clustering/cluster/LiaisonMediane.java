package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * algorithme implémentant la méthode de liaison médiane
 * médiane des distances des points des différents clusters
 * @param <T> un objet clusterisable contenu dans les clusters
 */
public class LiaisonMediane<T extends ObjetClusterisable> extends StrategieLiaison<T> {
    @Override
    public float calculerDistance(ClusterHierarchique<T> cluster1, ClusterHierarchique<T> cluster2) {
        List<Float> distances = new ArrayList<>();
        for (T objet1 : cluster1.getObjets()) {
            for (T objet2 : cluster2.getObjets()) {
                float distance = objet1.distance(objet2);
                distances.add(distance);
            }
        }
        Collections.sort(distances);
        int medianIndex = distances.size() / 2;
        // Si le nombre de distances est impair, retourner l'élément médian.
        // Si le nombre est pair, retourner la moyenne des deux éléments médians.
        if (distances.size() % 2 == 1) {
            return distances.get(medianIndex);
        } else {
            return (distances.get(medianIndex - 1) + distances.get(medianIndex)) / 2.0f;
        }
    }
}
