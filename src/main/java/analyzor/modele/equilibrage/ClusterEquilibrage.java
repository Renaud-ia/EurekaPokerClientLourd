package analyzor.modele.equilibrage;

public class ClusterEquilibrage implements ObjetEquilibrage {
    
    @Override
    public int[] strategiePlusProbableSansFold() {
        return new int[0];
    }

    @Override
    public float[] getShowdowns() {
        return new float[0];
    }

    @Override
    public float getPCombo() {
        return 0;
    }

    @Override
    public int[] getObservations() {
        return new int[0];
    }

    @Override
    public void setProbaAction(int indexAction, float[] probas) {

    }

    @Override
    public void setProbaFold(float[] probas) {

    }

    @Override
    public boolean notFolded() {
        return false;
    }

    @Override
    public float testerChangementFold(int sensChangement) {
        return 0;
    }

    @Override
    public float testerChangementStrategie(int indexChangement, int sensChangement) {
        return 0;
    }

    @Override
    public void appliquerChangementStrategie() {

    }

    @Override
    public int getPFold() {
        return 0;
    }

    @Override
    public int[] getStrategieSansFold() {
        return new int[0];
    }

    @Override
    public int[] getStrategie() {
        return new int[0];
    }
}
