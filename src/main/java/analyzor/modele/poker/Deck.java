package analyzor.modele.poker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Deck {
    private final boolean[] cartes = new boolean[Carte.CARTE_MAX + 1];
    private final Random random = new Random();

    public Deck() {
        remplir();
    }
    public void remplir() {
        for (Carte carte : GenerateurCombos.toutesLesCartes) {
            this.cartes[carte.toInt()] = true;
        }
    }
    public void retirerCarte(Carte carteRetiree) {
        this.cartes[carteRetiree.toInt()] = false;
    }

    private void ajouterCartes(Carte[] cartesAjoutees) {
        for (int i = 0; i < cartesAjoutees.length; i++) {
            Carte carteAjoutee = cartesAjoutees[i];
            this.cartes[carteAjoutee.toInt()] = true;
        }
    }

    public List<Board> obtenirEchantillon(Board boardActuel, int sizeBoard, int nSimus) {
        List<Board> echantillonBoard = new ArrayList<>();
        int cartesNecessaires = sizeBoard - boardActuel.taille();

        for (int i = 0; i < nSimus; i++) {
            Board boardCopie = boardActuel.copie();
            Carte[] cartesAjoutees = new Carte[cartesNecessaires];
            for (int j = 0; j < cartesNecessaires; j++) {
                Carte nouvelleCarte = carteRandom();
                boardCopie.ajouterCarte(nouvelleCarte);
                cartesAjoutees[j] = nouvelleCarte;
            }
            ajouterCartes(cartesAjoutees);
            echantillonBoard.add(boardCopie);
        }

        return echantillonBoard;
    }

    public Carte carteRandom() {
        //TODO optimisation : si beaucoup de cartes en moins va être lent...
        while (true) {
            int index = random.nextInt(Carte.CARTE_MAX + 1);
            if (cartes[index]) {
                //on retire la carte du deck
                cartes[index] = false;
                return new Carte(index);
            }
        }
    }
}
