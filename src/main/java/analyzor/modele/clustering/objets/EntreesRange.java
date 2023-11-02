package analyzor.modele.clustering.objets;

// utilisé pour le calcul de ranges
public class EntreesRange extends ObjetClusterisable {
    //todo
    @Override
    protected float[] valeursClusterisables() {
        return new float[0];
    }

    @Override
    public float distance(ObjetClusterisable autreObjet) {
        return 0;
    }

}
