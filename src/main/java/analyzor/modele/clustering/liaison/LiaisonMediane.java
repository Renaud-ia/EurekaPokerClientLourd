package analyzor.modele.clustering.liaison;

import analyzor.modele.clustering.cluster.ClusterFusionnable;
import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class LiaisonMediane<T extends ObjetClusterisable> extends StrategieLiaison<T> {
    @Override
    public float calculerDistance(ClusterFusionnable<T> cluster1, ClusterFusionnable<T> cluster2) {
        List<Float> distances = new ArrayList<>();
        for (T objet1 : cluster1.getObjets()) {
            for (T objet2 : cluster2.getObjets()) {
                float distance = objet1.distance(objet2);
                distances.add(distance);
            }
        }
        Collections.sort(distances);
        int medianIndex = distances.size() / 2;
        
        
        if (distances.size() % 2 == 1) {
            return distances.get(medianIndex);
        } else {
            return (distances.get(medianIndex - 1) + distances.get(medianIndex)) / 2.0f;
        }
    }
}
