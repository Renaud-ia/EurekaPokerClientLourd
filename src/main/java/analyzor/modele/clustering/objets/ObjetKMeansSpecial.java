package analyzor.modele.clustering.objets;

public abstract class ObjetKMeansSpecial extends ObjetClusterisable{
    // observations qui seront prises en compte pour analyser les clusters
    // mais qui ne sont pas significatives prises isolément
    public abstract float[] observations();
}
