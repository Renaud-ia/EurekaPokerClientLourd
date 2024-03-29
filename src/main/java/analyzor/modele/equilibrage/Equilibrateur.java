package analyzor.modele.equilibrage;

import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import analyzor.modele.estimation.CalculInterrompu;
import analyzor.modele.estimation.Estimateur;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


class Equilibrateur {
    private float PCT_RANDOMISATION = 0.3f;
    private final static float DIMINUTION_RANDOMISATION = 0.8f;
    private final static int MIN_ITERATIONS = 200;
    private final static int MAX_ITERATIONS = 10000;
    private final static float CRITERE_ARRET_ERREUR = 0.01f;
    private final List<? extends NoeudEquilibrage> noeuds;
    private final float[] pActionsReelle;
    private float[] erreursActuelles;
    private final List<Float> valeursErreur;
    private final Random random;

    Equilibrateur(List<? extends NoeudEquilibrage> noeuds,
                  float[] pActionsReelle) {
        this.noeuds = noeuds;
        this.pActionsReelle = pActionsReelle;
        valeursErreur = new ArrayList<>();
        random = new Random();
    }

    void lancerEquilibrage( ) throws CalculInterrompu {
        calculerErreur();
        while (continuerEquilibrage()) {
            tourEquilibrage();
            calculerErreur();
        }
    }

    private boolean continuerEquilibrage() throws CalculInterrompu {
        if (Estimateur.estInterrompu()) throw new CalculInterrompu();


        if (valeursErreur.size() < MIN_ITERATIONS) return true;
        else if (valeursErreur.size() > MAX_ITERATIONS) {
            return false;
        }
        else return !(valeursErreur.getLast() < CRITERE_ARRET_ERREUR);
    }

    private void tourEquilibrage() {
        if (randomisation()) {
            changementRandom();
        }
        else {
            Pair<Integer, Integer> changement = changementNecessaire();
            int indexChangement = changement.getFirst();
            int sensChangement = changement.getSecond();
            NoeudEquilibrage comboChange = comboAChanger(indexChangement, sensChangement);
            if (comboChange == null) {
                return;
            }
            comboChange.appliquerChangementStrategie();
        }
    }

    private NoeudEquilibrage comboAChanger(int indexChangement, int sensChangement) {
        int pasChangement = determinerPasChangement();
        float probaPlusHaute = 0;
        NoeudEquilibrage comboChange = null;
        for (NoeudEquilibrage comboDenombrable : noeuds) {
            float probaChangement = comboDenombrable.testerChangementStrategie(indexChangement, sensChangement, pasChangement);

            if (probaChangement < 0) continue;

            if (probaChangement > probaPlusHaute) {
                comboChange = comboDenombrable;
                probaPlusHaute = probaChangement;
            }
        }

        return comboChange;
    }

    private int determinerPasChangement() {
        if (valeursErreur.isEmpty()) return 5;
        if (valeursErreur.getLast() > 0.05f) return 5;
        return 1;
    }


    private Pair<Integer, Integer> changementNecessaire() {
        Random rand = new Random();
        float totalPoids = 0;
        for (float erreur : erreursActuelles) {
            totalPoids += Math.abs(erreur);
        }


        float valeurAleatoire = rand.nextFloat() * totalPoids;
        for (int indexAction = 0; indexAction < erreursActuelles.length; indexAction++) {
            valeurAleatoire -= Math.abs(erreursActuelles[indexAction]);
            if (valeurAleatoire <= 0) {
                int sensChangement = erreursActuelles[indexAction] > 0 ? -1 : 1;
                return new Pair<>(indexAction, sensChangement);
            }
        }


        throw new RuntimeException("Aucun choix n'a été fait");
    }


    private boolean changementRandom() {
        int indexCombo = random.nextInt(noeuds.size());
        NoeudEquilibrage comboRandom = noeuds.get(indexCombo);
        int indexChangement = random.nextInt(erreursActuelles.length);

        int randomSens = random.nextInt(100);
        int sensChangement = randomSens > 50 ? +1 : -1;

        float changementPossible;
        changementPossible = comboRandom.testerChangementStrategie(indexChangement, sensChangement, determinerPasChangement());
        if (changementPossible > 0) {
            comboRandom.appliquerChangementStrategie();
            return true;
        }
        else return false;
    }


    private boolean randomisation() {
        this.PCT_RANDOMISATION *= DIMINUTION_RANDOMISATION;
        float randomFloat = random.nextFloat();
        return (randomFloat < PCT_RANDOMISATION);
    }

    private void calculerErreur() {
        float[] pActionsEstimees = frequencesAction();
        erreursActuelles = new float[pActionsEstimees.length];

        if (pActionsEstimees.length != this.pActionsReelle.length)
            throw new RuntimeException("Pas le même nombre d'actions estimées et réelles");

        float moyenneErreur = 0;
        for (int i = 0; i < pActionsReelle.length; i++) {
            moyenneErreur += Math.abs(pActionsReelle[i] - pActionsEstimees[i]);
            erreursActuelles[i] = pActionsEstimees[i] - pActionsReelle[i];
        }

        moyenneErreur /= (pActionsReelle.length);

        this.valeursErreur.add(moyenneErreur);
    }


    private float[] frequencesAction() {
        float[] pActions = new float[noeuds.getFirst().getStrategieActuelle().length];
        for (NoeudEquilibrage comboDenombrable : noeuds) {
            for (int i = 0; i < pActions.length; i++) {
                pActions[i] += (comboDenombrable.getStrategieActuelle()[i] * comboDenombrable.getPCombo());
            }
        }
        return pActions;
    }
}
