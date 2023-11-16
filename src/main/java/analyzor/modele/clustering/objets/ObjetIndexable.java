package analyzor.modele.clustering.objets;

public class ObjetIndexable<T extends ObjetClusterisable> {
    public static int compte = 0;
    private final T objetStocke;
    private final int index;

    public ObjetIndexable(T objet) {
        this.objetStocke = objet;
        this.index = compte++;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ObjetIndexable)) return false;
        return  (((ObjetIndexable<?>) o).index == index);
    }

    @Override
    public int hashCode() {
        return index;
    }

    public T getObjet() {
        return objetStocke;
    }
}
