package analyzor.modele.auth;

public class Utilisateur {
    public Utilisateur() {
        this.authentifie = true;
    }
    public boolean estAuthentifie() {
        return this.authentifie;
    }
    private boolean authentifie;
}
