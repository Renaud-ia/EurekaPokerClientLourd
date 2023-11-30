package analyzor.modele.equilibrage;

import analyzor.modele.equilibrage.leafs.ComboDenombrable;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Equilibrateur {
    private static final Logger logger = LogManager.getLogger(Equilibrateur.class);
    private float PCT_RANDOMISATION = 0.3f;
    private final float DIMINUTION_RANDOMISATION = 0.8f;
    private final RegressionEquilibrage regressionEquilibrage;
    private final List<ComboDenombrable> leafs;
    private final float[] pActionsReelle;
    private final float pFoldReelle;
    private float[] erreursActuelles;
    private final List<Float> valeursErreur;
    private final Random random;

    Equilibrateur(RegressionEquilibrage regressionEquilibrage, List<ComboDenombrable> leafs,
                  float[] pActionsReelle, float pFoldReelle) {
        this.regressionEquilibrage = regressionEquilibrage;
        this.leafs = leafs;
        this.pActionsReelle = pActionsReelle;
        this.pFoldReelle = pFoldReelle;
        valeursErreur = new ArrayList<>();
        random = new Random();
    }

    void lancerEquilibrage( ) {
        calculerErreur();
        while (continuerEquilibrage()) {
            tourEquilibrage();
            calculerErreur();
        }
    }

    private boolean continuerEquilibrage() {
        if (valeursErreur.size() < 10) return true;
        else if (valeursErreur.size() > 500) return false;
        else if (valeursErreur.get(valeursErreur.size() - 1) < 0.01f) return true;
        return true;
    }

    private void tourEquilibrage() {
        if (randomisation()) {
            while (!changementRandom());
        }
        else {
            Pair<Integer, Integer> changement = changementNecessaire();
            int indexChangement = changement.getFirst();
            int sensChangement = changement.getSecond();
            ComboDenombrable comboChange = comboAChanger(indexChangement, sensChangement);
            comboChange.appliquerChangementStrategie();
        }
    }

    private ComboDenombrable comboAChanger(int indexChangement, int sensChangement) {
        regressionEquilibrage.resetTest();
        float probaPlusHaute = 0;
        ComboDenombrable comboChange = null;
        for (ComboDenombrable comboDenombrable : leafs) {
            float probaChangement;
            if (indexChangement == -1) {
                probaChangement =comboDenombrable.testerChangementFold(sensChangement);
            }
            else {
                probaChangement = comboDenombrable.testerChangementStrategie(indexChangement, sensChangement);
            }
            logger.info("Proba changement [" + comboDenombrable + "] : " + probaChangement);
            if (probaChangement < 0) continue;
            probaChangement *= regressionEquilibrage.getAmelioration();
            if (probaChangement > probaPlusHaute) {
                comboChange = comboDenombrable;
                probaPlusHaute = probaChangement;
            }
            regressionEquilibrage.resetTest();
        }
        if (comboChange == null) throw new RuntimeException("Aucun combo à changer");
        return comboChange;
    }

    /**
     * détermine les changements à faire
     * @return l'index du changement (-1 si fold) et le sens du changement (+1 pour augmenter, -1 pour diminuer)
     */
    private Pair<Integer, Integer> changementNecessaire() {
        int indexChangement = 0;
        int sensChangement = 0;

        float erreurMin = 0;
        for (int i = 0; i < erreursActuelles.length; i++) {
            if (Math.abs(erreursActuelles[i]) > erreurMin) {
                if (i == erreursActuelles.length - 1) {
                    indexChangement = -1;
                }
                else indexChangement = i;
                sensChangement = erreursActuelles[i] > 0 ? -1 : +1 ;
            }
        }
        if (sensChangement == 0) throw new RuntimeException("Aucun changement à faire");

        return new Pair<>(indexChangement, sensChangement);
    }

    /**
     * teste un changement au hasard
     * @return si le changement a pu être fait
     */
    private boolean changementRandom() {
        int indexCombo = random.nextInt(leafs.size());
        ComboDenombrable comboRandom = leafs.get(indexCombo);
        int indexChangement = random.nextInt(erreursActuelles.length);

        int randomSens = random.nextInt(100);
        int sensChangement = randomSens > 50 ? +1 : -1;

        float changementPossible;
        if (indexChangement == erreursActuelles.length - 1)
            changementPossible = comboRandom.testerChangementFold(sensChangement);
        else
            changementPossible = comboRandom.testerChangementStrategie(indexChangement, sensChangement);
        if (changementPossible > 0) {
            comboRandom.appliquerChangementStrategie();
            return true;
        }
        else return false;
    }

    /**
     * randomisation de moins en moins probable avec le temps
     */
    private boolean randomisation() {
        this.PCT_RANDOMISATION *= DIMINUTION_RANDOMISATION;
        float randomFloat = (float) random.nextInt(100) / 100;
        return (randomFloat < PCT_RANDOMISATION);
    }

    private void calculerErreur() {
        float[] pActionsEstimees = frequencesAction();
        float pFoldEstimee = frequenceFold();
        erreursActuelles = new float[pActionsEstimees.length];

        if (pActionsEstimees.length != this.pActionsReelle.length)
            throw new RuntimeException("Pas le même nombre d'actions estimées et réelles");

        float moyenneErreur = 0;
        for (int i = 0; i < pActionsReelle.length; i++) {
            moyenneErreur += Math.abs(pActionsReelle[i] - pActionsEstimees[i]);
            erreursActuelles[i] = pActionsEstimees[i] - pActionsReelle[i];
        }
        moyenneErreur += Math.abs(this.pFoldReelle - pFoldEstimee);
        // on divise par le nombre d'actions + fold
        moyenneErreur /= pActionsReelle.length;

        this.valeursErreur.add(moyenneErreur);
        logger.info("Erreur moyenne : " + moyenneErreur);
    }

    private float frequenceFold() {
        float pFold = 0;
        for (ComboDenombrable comboDenombrable : leafs) {
            // il faut diviser par 100 car exprimé en entier dans combo dénombrable
            pFold += comboDenombrable.getPFold() * comboDenombrable.getPCombo() / 100;
        }
        return pFold;
    }

    private float[] frequencesAction() {
        float[] pActions = new float[leafs.get(0).getStrategieSansFold().length];
        for (ComboDenombrable comboDenombrable : leafs) {
            for (int i = 0; i < pActions.length; i++) {
                // il faut diviser par 100 car exprimé en entier dans combo dénombrable
                pActions[i] += comboDenombrable.getStrategieSansFold()[i] * comboDenombrable.getPCombo() / 100;
            }
        }
        return pActions;
    }
}
