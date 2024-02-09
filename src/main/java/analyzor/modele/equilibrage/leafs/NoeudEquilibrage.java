package analyzor.modele.equilibrage.leafs;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.poker.evaluation.EquiteFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * objet manipulé par ProbaEquilibrage et l'équilibrateur
 * remplit les fonctions de base liés à l'initialisation des stratégies
 * et aux probabilités de changement
 * peu importe les objets qui sont stockés
 */
public abstract class NoeudEquilibrage extends ObjetClusterisable {
    private final static Logger logger = LogManager.getLogger(NoeudEquilibrage.class);
    // pour clustering de la range
    private final static float POIDS_EQUITE = 3;
    private final static float POIDS_STRATEGIE = 1;
    protected final float pCombo;
    protected final int[] observations;
    protected final float[] pShowdowns;
    protected final EquiteFuture equiteFuture;
    protected Strategie strategieActuelle;
    protected Strategie ancienneStrategie;
    protected boolean notFolded;
    protected float[][] probaObservations;

    protected NoeudEquilibrage(float pCombo, int[] observations, float[] pShowdowns, EquiteFuture equiteFuture) {
        if (observations.length != pShowdowns.length)
            throw new IllegalArgumentException("Pas autant d'observations que de showdowns");

        this.pCombo = pCombo;
        this.observations = observations;
        this.pShowdowns = pShowdowns;
        this.equiteFuture = equiteFuture;
    }

    // méthodes abstraites

    /**
     * va créer l'objet stratégie
     * on laisse chaque objet hérité avoir sa propre logique
     * ex :
     * ComboIsole => prend en compte pFold calculé sur équité
     * ComboDansCluster => prend en compte les combos voisins les + proches
      */

    public abstract void initialiserStrategie(int pas);
    public abstract String toString();

    // initialisation des probas utilisés par probaObservations

    public void setStrategiePlusProbable() {
        strategieActuelle.setStrategiePlusProbable();
    }
    public void setProbabilitesObservations(float[][] probaDiscretisees) {
        this.probaObservations = probaDiscretisees;
    }


    // méthodes de contrôle de la stratégie utilisées par l'équilibrateur

    public float[] getStrategieActuelle() {
        float[] strategiePourcent = new float[strategieActuelle.getStrategie().length];

        for (int i = 0; i < strategiePourcent.length; i++) {
            strategiePourcent[i] = (float) strategieActuelle.getStrategie()[i] / 100;
        }
        return strategiePourcent;
    }

    public void appliquerChangementStrategie() {
        ancienneStrategie = strategieActuelle.copie();
        strategieActuelle.appliquerValeurTest();
    }

    /**
     * méthode publique pour estimer le changement le plus probable
     */
    public float testerChangementStrategie(int indexAction, int sensChangement) {
        // important il faut reset les tests précédents
        strategieActuelle.resetTest();

        return valeurChangementStrategie(indexAction, sensChangement);
    }

    protected float probabiliteChangement(int indexChangement, int sensChangement) {
        return strategieActuelle.probaInterne(indexChangement, sensChangement);
    }

    /**
     * calcul la probabilité d'un changement donné
     * le combo décide lui-même de l'action à ajuster pour rester équilibré
     * gère le cas où il boucle sur lui-même
     * @param indexAction - 1 pour fold
     * @return la probabilité POSITIVE du changement quant aux observations (nombre négatif si changement pas possible)
     * todo : procédure de détection d'un blocage (=boucle)
     */
    private float valeurChangementStrategie(int indexAction, int sensChangement) {
        logger.trace("Strategie actuelle [" + this + "] : " + strategieActuelle);
        logger.trace("Changement demande d'index : " + indexAction + "dans le sens de : " + sensChangement);
        if (sensChangement != 1 && sensChangement != -1)
            throw new IllegalArgumentException("Le changement doit être 1 ou -1");

        // sinon on crée une stratégie de test
        float probaPremierChangement = probabiliteChangement(indexAction, sensChangement);
        strategieActuelle.testerValeur(indexAction, sensChangement);

        if (probaPremierChangement == -1) return -1;

        // on cherche le changement inverse le plus probable pour équilibrer la stratégie
        int secondChangement = -sensChangement;
        float probaSecondChangement = trouverSecondChangement(indexAction, secondChangement);

        // cas possible (par ex si all in ou fold et que fold pas possible)
        if (probaSecondChangement == -1)
            return -1;

        logger.trace("ProbaChangement vaut : " + probaPremierChangement * probaSecondChangement);

        return probaPremierChangement * probaSecondChangement;
    }

    /**
     * va trouver le second changement le plus probable et retourner sa probabilité
     * on peut override dans ComboDansCluster si on veut prendre en compte le voisinage
     */
    protected float trouverSecondChangement(int indexAction, int secondChangement) {
        int indexSecondChangement = 0;
        float probaSecondChangement = -1;

        for (int i = 0; i < strategieActuelle.nombreActions(); i++) {
            if (i == indexAction) continue;

            float probaChangement = probabiliteChangement(i, secondChangement);
            if (probaChangement > probaSecondChangement) {
                probaSecondChangement = probaChangement;
                indexSecondChangement = i;
            }
        }

        strategieActuelle.testerValeur(indexSecondChangement, secondChangement);

        return probaSecondChangement;
    }



    // pour clustering

    // todo à supprimer si on reste sur la nouvelle version de clustering range
    @Override
    protected float[] valeursClusterisables() {
        if (!(strategieActuelle.estInitialisee())) throw new RuntimeException("La stratégie n'est pas initialisée");
        // on met à plat les probabilités car écart de stratégie = 0 et ça déforme le clustering
        float[] strategieFloat = strategieActuelle.probabilitesAPlat();
        float[] equiteAPlat = equiteFuture.aPlat();
        int tailleTotale = strategieFloat.length + equiteAPlat.length;

        float[] valeursClusterisables = new float[tailleTotale];
        System.arraycopy(strategieFloat, 0, valeursClusterisables, 0, strategieFloat.length);
        System.arraycopy(equiteAPlat, 0, valeursClusterisables, strategieFloat.length, equiteAPlat.length);

        // on prend en compte que proba et strategie n'ont pas la même dimension
        float differenceDimension = (float) strategieFloat.length / equiteAPlat.length;

        this.poids = new float[tailleTotale];
        for (int i = 0; i < tailleTotale; i++) {
            if (i < strategieFloat.length) {
                poids[i] = POIDS_STRATEGIE;
            }
            else poids[i] = POIDS_EQUITE * differenceDimension;
        }

        return valeursClusterisables;
    }

    public float[] getProbabilites() {
        int largeurProba = probaObservations[0].length;
        float[] probasAPlat = new float[probaObservations.length * largeurProba];
        for (int i = 0; i < probaObservations.length; i++) {
            if (probaObservations[i].length != largeurProba) throw new RuntimeException("La matrice n'est pas carrée");
            int j = 0;
            while (j < probaObservations[i].length) {
                probasAPlat[i * largeurProba + j] = probaObservations[i][j];
                j++;
            }
        }
        return probasAPlat;
    }

    // getters

    public float getPCombo() {
        return pCombo;
    }

    public int[] getObservations() {
        return observations;
    }

    public float[] getShowdowns() {
        return pShowdowns;
    }

    public EquiteFuture getEquiteFuture() {
        return equiteFuture;
    }

    public boolean notFolded() {
        return notFolded;
    }

    public int nActionsSansFold() {
        return observations.length;
    }

    protected Strategie getStrategie() {
        return strategieActuelle;
    }

    // méthodes utilitaires

    /**
     * va normaliser les valeurs pour que la somme soit égale à 1
     */
    protected float[] normaliserProbabilites(float[] probas) {
        // Calcul de la somme des probabilités
        float sum = 0;
        for (float proba : probas) {
            sum += proba;
        }

        // Normalisation des probabilités
        for (int i = 0; i < probas.length; i++) {
            probas[i] /= sum;
        }

        return probas;
    }
}
