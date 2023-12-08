package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.clustering.objets.ObjetIndexable;

import java.util.ArrayList;
import java.util.List;

public class ClusterDBSCAN<T extends ObjetClusterisable> extends BaseCluster<T> {
    public ClusterDBSCAN() {
        super();
    }

    public void ajouterObjet(ObjetIndexable<T> objet) {
        this.listeObjets.add(objet.getObjet());
    }
}
