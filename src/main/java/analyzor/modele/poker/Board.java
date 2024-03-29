package analyzor.modele.poker;

import analyzor.modele.poker.evaluation.EvaluationCard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Board {
    private final List<Carte> cartes;


    public Board() {this.cartes = new ArrayList<>();}
    public Board(List<Carte> cartesBoard) {
        cartes = cartesBoard;
    }

    public Board(String cartesBoard) {
        cartesBoard = cartesBoard.replace(" ", "");
        this.cartes = new ArrayList<>();


        if (cartesBoard.length() % 2 != 0) {
            throw new IllegalArgumentException("Format de board saisi invalide");
        }

        for (int i = 0; i < cartesBoard.length(); i += 2) {
            char currentRank = cartesBoard.charAt(i);
            char currentSuit = cartesBoard.charAt(i + 1);
            cartes.add(new Carte(currentRank, currentSuit));
        }
    }


    public Board(int intBoard) {



        int nCartes = 0;
        int codeBoard = intBoard;
        while (codeBoard != 0) {
            codeBoard >>= Carte.N_BITS_CARTE;
            nCartes++;
        }

        cartes = new ArrayList<>();
        for (int cartesRestantes = nCartes; cartesRestantes > 0; cartesRestantes--) {
            int masque = (1 << Carte.N_BITS_CARTE) - 1;
            int intCard = (intBoard >> ((cartesRestantes - 1) * Carte.N_BITS_CARTE)) & masque;
            cartes.add(new Carte(intCard));
        }
    }

    public int asInt() {
        int boardInt = 0;
        for (Carte carte : cartes) {
            boardInt = (boardInt << Carte.N_BITS_CARTE) | carte.toInt();
        }
        return boardInt;
    }

    public List<Carte> getCartes() {

        return cartes;
    }

    public void ajouterCarte(Carte carte) {
        this.cartes.add(carte);
    }

    public int taille() {
        return cartes.size();
    }

    public Board copie() {
        List<Carte> copieBoard = new ArrayList<>();
        for (Carte carte : this.cartes) {
            Carte carteCopiee = carte.copie();
            copieBoard.add(carteCopiee);
        }

        return new Board(copieBoard);
    }


    public int gtoRank() {
        int nBitsSuit = 2;
        List<Carte> cartesTriees = new ArrayList<>(cartes);

        cartesTriees.sort(Comparator.comparingInt(Carte::getIntRank));

        int primeProduct = 1;
        int suitProduct = 0;
        int indexSuit = 0;
        Integer nouveauSuitInt;
        HashMap<Integer, Integer> nouvelIndexSuit = new HashMap<>();
        for (Carte carte : cartesTriees) {
            primeProduct *= EvaluationCard.PRIMES[carte.getIntRank()];
            nouveauSuitInt = nouvelIndexSuit.get(carte.getIntSuit());
            if (nouveauSuitInt == null) {
                nouveauSuitInt = indexSuit;
                nouvelIndexSuit.put(carte.getIntSuit(), indexSuit);
                indexSuit++;
            }
            suitProduct = (suitProduct | nouveauSuitInt) << nBitsSuit;
        }

        return (primeProduct << (nBitsSuit * cartesTriees.size())) | suitProduct;
    }

    @Override
    public String toString() {
        StringBuilder repr = new StringBuilder();
        repr.append("Board [");
        for (int i = 0; i < cartes.size(); i++) {
            repr.append(cartes.get(i));
            if (i < cartes.size() - 1) {
                repr.append(", ");
            }
        }
        repr.append("]");
        return repr.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        else if (!(o instanceof Board)) return false;
        else return ((Board) o).asInt() == this.asInt();
    }
}
