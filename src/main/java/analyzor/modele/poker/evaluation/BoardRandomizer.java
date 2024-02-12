package analyzor.modele.poker.evaluation;

import analyzor.modele.poker.Board;
import analyzor.modele.poker.Carte;
import analyzor.modele.poker.Deck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardRandomizer {
    private final Map<Integer, Integer> nSimus;
    private final Deck deck;
    public BoardRandomizer(HashMap<Integer, Integer> nSimus) {
        // configuration des simulations
        this.nSimus = nSimus;
        this.deck = new Deck();
    }

    public void retirerCarte(Carte carte) {
        deck.retirerCarte(carte);
    }

    public List<Board> obtenirEchantillon(Board boardActuel, int sizeBoard) {
        //TODO problème peu significatif (surtout flop et turn)
        List<Board> echantillonBoard = new ArrayList<>();
        if (boardActuel.taille() == sizeBoard) {
            echantillonBoard.add(boardActuel);
            return echantillonBoard;
        }
        int cartesNecessaires = sizeBoard - boardActuel.taille();
        // valeur exacte si 1 carte restante
        if (cartesNecessaires == 1) return tousLesBoards(boardActuel);

        int numSimulations = nSimus.get(cartesNecessaires);

        for (int i = 0; i < numSimulations; i++) {
            // todo si on a au moins 3 cartes nécessaires => on part d'un board Subset
            //  (random ? ou alors on prend juste le bon nombre de cartes par subset pour arriver au nombre voulu)
            // il faut aussi prendre des couleurs random pour le subset
            Board boardCopie = boardActuel.copie();
            Carte[] cartesAjoutees = new Carte[cartesNecessaires];
            for (int j = 0; j < cartesNecessaires; j++) {
                Carte nouvelleCarte = deck.carteRandom();
                boardCopie.ajouterCarte(nouvelleCarte);
                cartesAjoutees[j] = nouvelleCarte;
            }
            deck.ajouterCartes(cartesAjoutees);
            echantillonBoard.add(boardCopie);
        }

        return echantillonBoard;
    }

    private List<Board> tousLesBoards(Board boardActuel) {
        List<Board> tousLesBoards = new ArrayList<>();
        List<Carte> toutesLesCartes = deck.cartesRestantes();
        for (Carte carte : toutesLesCartes) {
            Board boardCopie = boardActuel.copie();
            boardCopie.ajouterCarte(carte);
            tousLesBoards.add(boardCopie);
        }
        return tousLesBoards;
    }

    public void ajouterCartes(Carte carteAjoutee) {
        deck.ajouterCarte(carteAjoutee);
    }
}
