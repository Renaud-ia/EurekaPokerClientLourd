package analyzor.modele.equilibrage;

// interface pour faire fonctionner ProbaEquilibrage
public interface ObjetEquilibrage {
    public int[] strategiePlusProbableSansFold();
    public float[] getShowdowns();
    public float getPCombo();
    public int[] getObservations();
    public void setProbaAction(int indexAction, float[] probas);
    public void setProbaFold(float[] probas);
    public boolean notFolded();

    float testerChangementFold(int sensChangement);

    float testerChangementStrategie(int indexChangement, int sensChangement);

    void appliquerChangementStrategie();

    int getPFold();

    int[] getStrategieSansFold();

    int[] getStrategie();
}
