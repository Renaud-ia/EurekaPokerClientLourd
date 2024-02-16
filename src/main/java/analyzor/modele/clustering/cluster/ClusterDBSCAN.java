package analyzor.modele.clustering.cluster;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.clustering.objets.ObjetIndexable;

public class ClusterDBSCAN<T extends ObjetClusterisable> extends ClusterDeBase<T> {
    public ClusterDBSCAN() {
        super();
    }

    public void ajouterObjet(ObjetIndexable<T> objet) {
        this.listeObjets.add(objet.getObjet());
    }
}
