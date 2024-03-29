package analyzor.modele.equilibrage.leafs;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.poker.evaluation.EquiteFuture;
import analyzor.modele.utils.ManipulationTableaux;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public abstract class NoeudEquilibrage extends ObjetClusterisable {

    private final static float POIDS_EQUITE = 1;
    private final static float POIDS_STRATEGIE = 1;
    protected final float pCombo;
    protected final int[] observations;
    protected final float[] pShowdowns;
    protected final EquiteFuture equiteFuture;
    protected Strategie strategieActuelle;
    protected Strategie ancienneStrategie;
    protected float[][] probasStrategie;
    protected float[] probaFoldEquite;
    protected boolean isNotFolded;

    protected NoeudEquilibrage(float pCombo, int[] observations, float[] pShowdowns, EquiteFuture equiteFuture) {
        if (observations.length != pShowdowns.length)
            throw new IllegalArgumentException("Pas autant d'observations que de showdowns");

        this.pCombo = pCombo;
        this.observations = observations;
        this.pShowdowns = pShowdowns;
        this.equiteFuture = equiteFuture;
        this.isNotFolded = false;
    }




    public void initialiserStrategie(int pas) {
        if (probasStrategie == null)
            throw new RuntimeException("Les probabilités n'ont pas été correctement initialisées");

        int indexFold = probasStrategie.length - 1;

        float[] probaFoldObs = probasStrategie[indexFold];
        float[] probaFoldFinale = new float[probaFoldObs.length];


        if (probaFoldEquite != null) {
            if (probaFoldObs.length != probaFoldEquite.length)
                throw new RuntimeException("Proba fold observations et équité n'ont pas la même taille");

            for (int i = 0; i < probaFoldObs.length; i++) {
                probaFoldFinale[i] = probaFoldObs[i] * probaFoldEquite[i];
            }
        }

        else {
            System.arraycopy(probaFoldObs, 0, probaFoldFinale, 0, probaFoldObs.length);
        }

        normaliserProbabilites(probaFoldFinale);
        probasStrategie[indexFold] = probaFoldFinale;

        strategieActuelle = new Strategie(probasStrategie, pas, isNotFolded);
    }

    public abstract String toString();



    public void setStrategiePlusProbable() {
        strategieActuelle.setStrategiePlusProbable();
    }
    public void setProbabilitesObservations(float[][] probaDiscretisees) {
        this.probasStrategie = probaDiscretisees;
    }




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


    public float testerChangementStrategie(int indexAction, int sensChangement, int pasChangement) {

        strategieActuelle.resetTest();

        return valeurChangementStrategie(indexAction, sensChangement, pasChangement);
    }

    protected float probabiliteChangement(int indexChangement, int sensChangement) {
        return strategieActuelle.probaInterne(indexChangement, sensChangement);
    }


    private float valeurChangementStrategie(int indexAction, int sensChangement, int pasChangement) {
        if (sensChangement != 1 && sensChangement != -1)
            throw new IllegalArgumentException("Le changement doit être 1 ou -1");


        float probaPremierChangement = probabiliteChangement(indexAction, sensChangement);
        strategieActuelle.testerValeur(indexAction, sensChangement);

        if (probaPremierChangement == -1) return -1;


        int secondChangement = -sensChangement;
        float probaSecondChangement = trouverSecondChangement(indexAction, secondChangement);


        if (probaSecondChangement == -1)
            return -1;

        return probaPremierChangement * probaSecondChangement;
    }


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






    @Override
    protected float[] valeursClusterisables() {
        if (probasStrategie == null || probasStrategie.length == 0)
            throw new RuntimeException("Les probabilités d'observations ne sont pas initialisées : " + this);

        float[] strategieFloat = ManipulationTableaux.aplatir(probasStrategie);
        float[] equiteAPlat = equiteFuture.aPlat();
        int tailleTotale = strategieFloat.length + equiteAPlat.length;

        float[] valeursClusterisables = new float[tailleTotale];
        System.arraycopy(strategieFloat, 0, valeursClusterisables, 0, strategieFloat.length);
        System.arraycopy(equiteAPlat, 0, valeursClusterisables, strategieFloat.length, equiteAPlat.length);


        float differenceDimension = (float) strategieFloat.length / equiteAPlat.length;

        float[] poidsFixes = new float[tailleTotale];
        for (int i = 0; i < tailleTotale; i++) {
            if (i < strategieFloat.length) {
                poidsFixes[i] = POIDS_STRATEGIE;
            }
            else poidsFixes[i] = POIDS_EQUITE * differenceDimension;
        }
        setPoids(poidsFixes);

        return valeursClusterisables;
    }

    public float[] getProbabilites() {
        if (probasStrategie == null || probasStrategie.length == 0)
            throw new RuntimeException("Les probabilités d'observations ne sont pas initialisées : " + this);

        int largeurProba = probasStrategie[0].length;
        float[] probasAPlat = new float[probasStrategie.length * largeurProba];
        for (int i = 0; i < probasStrategie.length; i++) {
            if (probasStrategie[i].length != largeurProba) throw new RuntimeException("La matrice n'est pas carrée");
            int j = 0;
            while (j < probasStrategie[i].length) {
                probasAPlat[i * largeurProba + j] = probasStrategie[i][j];
                j++;
            }
        }
        return probasAPlat;
    }



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

    public int nActionsSansFold() {
        return observations.length;
    }

    protected Strategie getStrategie() {
        return strategieActuelle;
    }




    protected void normaliserProbabilites(float[] probas) {

        float sum = 0;
        for (float proba : probas) {
            sum += proba;
        }


        for (int i = 0; i < probas.length; i++) {
            probas[i] /= sum;
        }

    }

    protected float[] getProbaFoldEquite() {
        return probaFoldEquite;
    }

    public void setStrategieMediane() {
        strategieActuelle.setStrategieMediane();
    }
}
