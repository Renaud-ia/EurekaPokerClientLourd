package analyzor.vue.donnees;

import analyzor.modele.parties.Move;

public class InfosAction {
    private final Move move;
    private final float betSize;
    private final int indexAction;

    public InfosAction(Move move, float betSize, int indexActionModele) {
        this.move = move;
        this.betSize = betSize;
        this.indexAction = indexActionModele;
    }

    public String getNom() {
        StringBuilder reprAction = new StringBuilder();
        reprAction.append(move.toString());
        if (move == Move.RAISE) {
            // si il n'y a pas de chiffres après la virgule on affiche un int
            if (betSize * 10 == ((int) betSize) * 10) {
                reprAction.append(" ").append((int) betSize);
            }
            else {
                reprAction.append(" ").append(betSize);
            }
        }

        return reprAction.toString();
    }

    public int getIndex() {
        return indexAction;
    }
}
