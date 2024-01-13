package analyzor.vue.donnees;

public class InfosAction {
    private final String nomAction;
    private final float betSize;
    private final int indexAction;

    public InfosAction(String nomAction, float betSize, int indexActionModele) {
        this.nomAction = nomAction;
        this.betSize = betSize;
        this.indexAction = indexActionModele;
    }

    public String getNom() {
        StringBuilder reprAction = new StringBuilder();
        reprAction.append(nomAction);
        if (betSize > 0) {
            reprAction.append(" ").append(betSize);
        }

        return reprAction.toString();
    }

    public int getIndex() {
        return indexAction;
    }
}
