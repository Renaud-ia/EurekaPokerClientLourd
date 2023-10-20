package analyzor.modele.poker.evaluation;

import analyzor.modele.poker.Board;
import analyzor.modele.poker.Carte;
import analyzor.modele.poker.ComboReel;
import analyzor.modele.poker.RangeReelle;

import java.util.ArrayList;
import java.util.List;

public class CalculatriceEquite {
    private final int nSimuFlop;
    private final int nSimuTurn;
    private final int nSimuRiver;
    private final float pctRangeHero;
    private final float pctRangeVillain;
    private final int nPercentiles;
    private final Evaluator evaluateur = new Evaluator();
    public CalculatriceEquite(
            int nSimuFlop,
            int nSimuTurn,
            int nSimuRiver,
            float pctRangeHero,
            float pctRangeVillain,
            int nPercentiles
    ) {
        this.nSimuFlop = nSimuFlop;
        this.nSimuTurn = nSimuTurn;
        this.nSimuRiver = nSimuRiver;
        this.pctRangeHero = pctRangeHero;
        this.pctRangeVillain = pctRangeVillain;
        this.nPercentiles = nPercentiles;
    }

    private float equiteMainBoard(ComboReel comboHero, Board board, List<RangeReelle> rangesVillains) {
        int nombreVillains = rangesVillains.size();

        List<Carte> toutesLesCartes = new ArrayList<>();
        toutesLesCartes.addAll(comboHero.getCartes());
        toutesLesCartes.addAll(board.getCartes());

        int tailleEchantillon = 0;
        List<List<ComboReel>> combosVillains = new ArrayList<>();
        //todo problème, on veut avoir un échantillon de même taille sinon bug!!!!
        for (RangeReelle range : rangesVillains) {
            RangeReelle rangeCopiee = range.copie();
            for (Carte carteSupprimee : toutesLesCartes) {
                rangeCopiee.retirerCarte(carteSupprimee);
            }
            List<ComboReel> echantillon = rangeCopiee.obtenirEchantillon(pctRangeVillain);
            combosVillains.add(echantillon);
            tailleEchantillon = echantillon.size();
        }
        if (tailleEchantillon == 0) throw new IllegalArgumentException("Echantillon nul, avez vous rentré au moins une range?");

        int heroRank = evaluateur.evaluate(comboHero, board);
        float equite = 0;
        for (int i = 0; i <= tailleEchantillon; i++) {
            int minVillainRank = LookupTable.MAX_HIGH_CARD;
            for (int indexVillain = 0; indexVillain <= nombreVillains; indexVillain++) {
                ComboReel comboVillain = combosVillains.get(indexVillain).get(i);
                int villainRank = evaluateur.evaluate(comboVillain, board);
                if (villainRank < minVillainRank) minVillainRank = villainRank;
            }

            if (heroRank < minVillainRank) equite += 1f;
            else if (heroRank == minVillainRank) equite += 0.5f;
        }
        return equite / tailleEchantillon;
    }

    public float equiteGlobaleMain(ComboReel comboHero, Board board, List<RangeReelle> rangesVillains) {
        return 0f;
    }
}
