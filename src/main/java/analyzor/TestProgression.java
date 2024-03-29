package analyzor;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Variante;
import org.apache.poi.ss.formula.functions.T;

public class TestProgression {
    private final float valeurAlpha;
    private final float MAX_VALEUR_MAPPAGE;
    private final int RATIO_GRANDE_PETITE_TACHE;
    private int nMaximumPonderee;
    private float pctFinal;
    private float iterationActuelle;
    private float pctInitial;
    private int nIterationsGrandeTache = 1;
    public TestProgression(Variante.PokerFormat pokerFormat) {
        iterationActuelle = 0;

        if (pokerFormat == Variante.PokerFormat.MTT) {
            valeurAlpha = 10;
            MAX_VALEUR_MAPPAGE = 5;
            RATIO_GRANDE_PETITE_TACHE = 38;
        }
        else if (pokerFormat == Variante.PokerFormat.SPIN) {
            valeurAlpha = 1.5f;
            MAX_VALEUR_MAPPAGE = 3f;
            RATIO_GRANDE_PETITE_TACHE = 30;
        }
        else if (pokerFormat == Variante.PokerFormat.CASH_GAME) {
            valeurAlpha = 10;
            MAX_VALEUR_MAPPAGE = 5;
            RATIO_GRANDE_PETITE_TACHE = 38;
        }
        else {
            valeurAlpha = 10;
            MAX_VALEUR_MAPPAGE = 5f;
            RATIO_GRANDE_PETITE_TACHE = 38;
        }
    }

    public void fixerIterationActuelle(int nSituationsResolues) {
        this.iterationActuelle = nSituationsResolues * (RATIO_GRANDE_PETITE_TACHE + 1);
        this.pctInitial = getPourcentageAjuste(iterationActuelle);
    }

    private void fixerNombreIterations(int nMaximumIterations) {
        this.nMaximumPonderee = nMaximumIterations * (RATIO_GRANDE_PETITE_TACHE + 1) + 1;
    }

    public void fixerIterationFinale(int size) {
        float nFinale = size * (RATIO_GRANDE_PETITE_TACHE + 1) + 1;
        this.pctFinal = getPourcentageAjuste(nFinale);
    }

    public float getPctFinal() {
        return pctFinal;
    }


    private int incrementerPetitAvancement() {
        iterationActuelle += 1;
        return (int) iterationActuelle;
    }


    private int incrementerGrandAvancement() {
        iterationActuelle += RATIO_GRANDE_PETITE_TACHE;
        return (int) iterationActuelle;
    }

    private int incrementerIterationGrandAvancement() {
        iterationActuelle += (float) RATIO_GRANDE_PETITE_TACHE / nIterationsGrandeTache;
        return (int) iterationActuelle;
    }

    public void fixerNombreIterationsGrandeTache(int size) {
        nIterationsGrandeTache = size;
    }


    private float getPourcentageAjuste(float iterationsCumulees) {
        float valeurMappee = mapperValeurEntreZeroEtCinq(iterationsCumulees);
        return (float) exponentielleInversee(valeurMappee);
    }


    private float mapperValeurEntreZeroEtCinq(float valeurIteration) {
        return (valeurIteration / nMaximumPonderee) * MAX_VALEUR_MAPPAGE;
    }

    private double exponentielleInversee(float x) {
        return (1 - Math.exp(- valeurAlpha * x));
    }

    public float getPourcentageInitial() {
        return pctInitial;
    }

    public static void main(String[] args) {
        TestProgression testProgression = new TestProgression(Variante.PokerFormat.MTT);
        testProgression.fixerNombreIterations(2000);
        testProgression.fixerIterationFinale(2000);

        for (int i = 0; i < 2000; i++) {
            testProgression.incrementerPetitAvancement();
            int avancement = testProgression.incrementerGrandAvancement();
            System.out.println(i);
            System.out.println(testProgression.getPourcentageAjuste(avancement));
        }
    }
}
