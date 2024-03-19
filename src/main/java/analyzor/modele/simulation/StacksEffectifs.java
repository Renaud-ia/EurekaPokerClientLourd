package analyzor.modele.simulation;

/**
 * interface standard de construction d'une méthode de StackEffectif
 */
public abstract class StacksEffectifs {
    protected final int methode;

    StacksEffectifs(int methode) {
        this.methode = methode;
    }

    int getMethode() {
        return methode;
    }

    abstract void ajouterStackVillain(float stackVillain);

    public abstract int getDimensions();

    public abstract float[] getDonnees();

    public abstract float[] getPoidsStacks();

    public abstract long getIdGenere();

    @Override
    public abstract String toString();
}
