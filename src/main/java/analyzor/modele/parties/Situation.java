package analyzor.modele.parties;

public class Situation {
    public Situation(int rang, int nJoueursActifs, int tour, int position) {
        this.rang = rang;
        this.nJoueursActifs = nJoueursActifs;
        this.tour = tour;
        this.position = position;
    }
    private int rang;
    private int nJoueursActifs;
    private int tour;
    private int position;

}
