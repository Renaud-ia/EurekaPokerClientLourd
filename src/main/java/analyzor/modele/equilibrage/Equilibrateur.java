package analyzor.modele.equilibrage;

import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * équilibre les ClusterEquilibrage selon les % d'actions globales observées et les proba observées par chaque cluster
 * apporte de la randomisation
 */
class Equilibrateur {
    private static final Logger logger = LogManager.getLogger(Equilibrateur.class);
    private float PCT_RANDOMISATION = 0.3f;
    private final float DIMINUTION_RANDOMISATION = 0.8f;
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

    void lancerEquilibrage( ) {
        logger.info("########DEBUT EQUILIBRAGE###########");
        logger.info("Stratégie à atteindre avec : " + Arrays.toString(pActionsReelle));
        loggerStrategies();
        calculerErreur();
        while (continuerEquilibrage()) {
            tourEquilibrage();
            calculerErreur();
        }
        logger.info("########EQUILIBRAGE TERMINE###########");
        loggerStrategies();
    }

    private boolean continuerEquilibrage() {
        // todo améliorer les critères d'arrêt ??
        if (valeursErreur.size() < 10) return true;
        else if (valeursErreur.size() > 500) {
            logger.warn("Pas réussi à équilibrer en 500 itérations");
            return false;
        }
        else return !(valeursErreur.getLast() < 0.01f);
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
                logger.error("Aucun combo à changer");
                return;
            }
            logger.trace("Combo à changer : " + comboChange);
            comboChange.appliquerChangementStrategie();
        }
    }

    private NoeudEquilibrage comboAChanger(int indexChangement, int sensChangement) {
        float probaPlusHaute = 0;
        NoeudEquilibrage comboChange = null;
        for (NoeudEquilibrage comboDenombrable : noeuds) {
            float probaChangement = comboDenombrable.testerChangementStrategie(indexChangement, sensChangement);
            //logger.trace("Proba pure changement [" + comboDenombrable + "] : " + probaChangement);
            // changement impossible
            if (probaChangement < 0) continue;

            if (probaChangement > probaPlusHaute) {
                comboChange = comboDenombrable;
                probaPlusHaute = probaChangement;
            }
        }

        return comboChange;
    }

    /**
     * détermine les changements à faire
     * sélectionne de manière random selon le poids relatif des erreurs
     * @return l'index du changement (-1 si fold) et le sens du changement (+1 pour augmenter, -1 pour diminuer)
     */
    private Pair<Integer, Integer> changementNecessaire() {
        Random rand = new Random();
        float totalPoids = 0;
        for (float erreur : erreursActuelles) {
            totalPoids += Math.abs(erreur);  // Calcul du total des poids
        }

        // Choix aléatoire en fonction des poids
        float valeurAleatoire = rand.nextFloat() * totalPoids;
        for (int indexAction = 0; indexAction < erreursActuelles.length; indexAction++) {
            valeurAleatoire -= Math.abs(erreursActuelles[indexAction]);
            if (valeurAleatoire <= 0) {
                int sensChangement = erreursActuelles[indexAction] > 0 ? -1 : 1;
                logger.trace("Changement nécessaire : " + indexAction + " ," + sensChangement);
                return new Pair<>(indexAction, sensChangement);
            }
        }

        // Gestion du cas où aucun choix n'est fait (devrait normalement ne pas arriver)
        throw new RuntimeException("Aucun choix n'a été fait");
    }

    /**
     * teste un changement au hasard
     * @return si le changement a pu être fait
     */
    private boolean changementRandom() {
        logger.trace("Changement random");
        int indexCombo = random.nextInt(noeuds.size());
        NoeudEquilibrage comboRandom = noeuds.get(indexCombo);
        int indexChangement = random.nextInt(erreursActuelles.length);

        int randomSens = random.nextInt(100);
        int sensChangement = randomSens > 50 ? +1 : -1;

        float changementPossible;
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
        erreursActuelles = new float[pActionsEstimees.length];

        if (pActionsEstimees.length != this.pActionsReelle.length)
            throw new RuntimeException("Pas le même nombre d'actions estimées et réelles");

        float moyenneErreur = 0;
        for (int i = 0; i < pActionsReelle.length; i++) {
            moyenneErreur += Math.abs(pActionsReelle[i] - pActionsEstimees[i]);
            erreursActuelles[i] = pActionsEstimees[i] - pActionsReelle[i];
        }
        // on divise par le nombre d'actions + fold
        moyenneErreur /= (pActionsReelle.length);

        this.valeursErreur.add(moyenneErreur);
        logger.trace("Strategie estimee avec fold : " + Arrays.toString(pActionsEstimees));
        logger.info("Erreur moyenne : " + moyenneErreur);
    }


    private float[] frequencesAction() {
        float[] pActions = new float[noeuds.get(0).getStrategieActuelle().length];
        for (NoeudEquilibrage comboDenombrable : noeuds) {
            for (int i = 0; i < pActions.length; i++) {
                pActions[i] += (comboDenombrable.getStrategieActuelle()[i] * comboDenombrable.getPCombo());
            }
        }
        return pActions;
    }

    // todo supprimer en prod
    private void loggerStrategies() {
        for (NoeudEquilibrage comboDenombrable : noeuds) {
            logger.trace("STRATEGIE de : " + comboDenombrable);
            logger.trace("OBSERVATIONS : " + Arrays.toString(comboDenombrable.getObservations()));
            logger.trace(Arrays.toString(comboDenombrable.getStrategieActuelle()));
        }
    }
}
