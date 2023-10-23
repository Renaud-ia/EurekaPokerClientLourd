package analyzor.modele.poker.evaluation;

import analyzor.modele.poker.Board;
import analyzor.modele.poker.Carte;
import analyzor.modele.poker.ComboReel;
import analyzor.modele.utils.Combinations;

import java.util.*;


public class Evaluator {
    private static LookupTable table = new LookupTable();
    public Evaluator() {
    }

    public int evaluate(ComboReel cartesJoueur, Board board) {
        List<Integer> codesCartes = new ArrayList<>();
        int handSize = 0;
        for (Carte c : cartesJoueur.getCartes()) {
            int evalCard = EvaluationCard.newCard(c);
            codesCartes.add(evalCard);
            handSize++;
        }
        for (Carte c : board.getCartes()) {
            int evalCard = EvaluationCard.newCard(c);
            codesCartes.add(evalCard);
            handSize++;
        }

        if (handSize == (5)) {
            return evaluateFive(codesCartes);
        }
        else if (handSize > 5) {
            return evaluateMoreThanSix(codesCartes);
        }
        //todo que faire ici
        else throw new RuntimeException("Nombre de cartes incompatibles avec evaluator : " + handSize);
    }

    public int evaluateFive(List<Integer> cards) {
        if ((cards.get(0) & cards.get(1) & cards.get(2) & cards.get(3) & cards.get(4) & 0xF000) != 0) {
            int handOR = (cards.get(0) | cards.get(1) | cards.get(2) | cards.get(3) | cards.get(4)) >> 16;
            long prime = EvaluationCard.primeProductFromRankbits(handOR);
            return table.flushLookup.get(prime);
        } else {
            long prime = EvaluationCard.primeProductFromHand(cards);
            return table.unsuitedLookup.get(prime);
        }
    }

    public int evaluateMoreThanSix(List<Integer> cards) {
        int minimum = LookupTable.MAX_HIGH_CARD;

        Combinations<Integer> combinator = new Combinations<>(cards);
        for (List<Integer> combi : combinator.getCombinations(5)) {
            int score = evaluateFive(combi);
            if (score < minimum) minimum = score;
        }

        return minimum;
    }
}
