package analyzor.vue.donnees;

public class InfosSolution {
    private String variante;
    private boolean bounty;
    private int nJoueurs;
    private float stackMoyen;
    public InfosSolution() {

    }

    public void setVariante(String variante) {
        variante = variante;
    }

    public void setBounty(boolean bounty) {
        bounty = bounty;
    }

    public void setnJoueurs(int nJoueurs) {
        nJoueurs = nJoueurs;
    }

    public void setStackMoyen(int stackMoyen) {
        stackMoyen = stackMoyen;
    }

    public String getVariante() {
        return variante;
    }

    public String getStack() {
        return Integer.toString((int) stackMoyen);
    }

    public String getNombreDeJoueurs() {
        return Integer.toString(nJoueurs);
    }
}
