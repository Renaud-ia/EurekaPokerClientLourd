package analyzor.vue.donnees;


public class InfosSolution {
    private String variante;
    private Boolean bounty;
    private Integer nJoueurs;
    public InfosSolution() {

    }

    public void setVariante(String variante) {
        this.variante = variante;
    }

    public void setBounty(boolean bounty) {
        this.bounty = bounty;
    }

    public void setnJoueurs(int nJoueurs) {
        this.nJoueurs = nJoueurs;
    }


    public String getVariante() {
        if (this.variante == null) return "-";
        return variante;
    }

    public String getNombreDeJoueurs() {
        if (this.nJoueurs == null) return "-";
        return Integer.toString(nJoueurs);
    }

    public String getBounty() {
        if (this.bounty == null) return "-";
        if (bounty) {
            return "oui";
        }
        else return "non";
    }
}
