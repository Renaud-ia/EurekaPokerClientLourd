package analyzor.modele.clustering.cluster;

public interface ObjetClusterisable {
    float[] valeursClusterisables();
    float distance(ObjetClusterisable autreObjet);
    @Override
    int hashCode();
    @Override
    boolean equals(Object objet);
}
