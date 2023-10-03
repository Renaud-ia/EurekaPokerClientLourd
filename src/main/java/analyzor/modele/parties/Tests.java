package analyzor.modele.parties;

public class Tests {
    public static void main(String[] args) {
        Situation situation = new Situation(0, 3, 1, 22);
        Situation situationTrouvee = (Situation) RequetesBDD.getOrCreate(situation, true);
    }
}
