package analyzor.vue.donnees;

// todo on pourrait fusionner avec DTOFORMAT
public class InfosSolution {
    private String variante;
    private boolean bounty;
    private int nJoueurs;
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


    public String getVariante() {
        return variante;
    }

    public String getNombreDeJoueurs() {
        return Integer.toString(nJoueurs);
    }

    public String getBounty() {
        if (bounty) {
            return "oui";
        }
        else return "non";
    }
}
