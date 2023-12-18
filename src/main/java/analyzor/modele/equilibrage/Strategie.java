package analyzor.modele.equilibrage;

import java.util.Arrays;

/**
 * classe qui stocke les stratégies
 * la valeur contenue dans une stratégie correspond à l'index de sa proba
 * garde en mémoire les stratégies de test
 * renvoie les probabilités de changement dans un sens ou l'autre
 * initialise les stratégies
 * retourne les stratégies sous forme de % en multipliant par le pas
 */
public class Strategie {
    private int[] indexStrategie;
    private int[] strategieTest;
    private final int maxIndex;
    private final float[][] probabilites;
    private final int pas;
    private boolean initialisee;
    private final boolean notFolded;

    private Strategie(float[][] probabilites, int[] indexStrategie, int pas, boolean notFolded) {
        this.probabilites = probabilites;
        this.pas = pas;
        maxIndex = (100 / pas);
        this.indexStrategie = indexStrategie;
        this.notFolded = notFolded;
    }

    // constructeur utilisé par ProbaEquilibrage qui construit les Stratégies
    Strategie(float[][] probabilites, int pas, boolean notFolded) {
        this(probabilites, new int[probabilites.length], pas, notFolded);
    }

    public int[] getStrategie() {
        int[] strategie = new int[this.indexStrategie.length];
        for (int i = 0; i < indexStrategie.length; i++) {
            strategie[i] = indexStrategie[i] * pas;
        }

        return strategie;
    }

    public void resetTest() {
        this.strategieTest = Arrays.copyOf(indexStrategie, indexStrategie.length);
    }

    public void testerValeur(int indexAction, int sensChangement) {
        strategieTest[indexAction] += sensChangement;
    }

    public void appliquerValeurTest() {
        this.indexStrategie = Arrays.copyOf(strategieTest, strategieTest.length);
    }

    // calcul des probas internes

    private boolean changementPossible(int indexAction, int sensChangement) {
        // on empêche le fold de changer si notFolded
        if (notFolded && indexAction == indexStrategie.length - 1) return false;

        int nouvelIndex = indexStrategie[indexAction] + sensChangement;
        return (nouvelIndex >= 0 && nouvelIndex <= maxIndex);
    }

    /**
     * retourne la masse probabilité totale qui va dans le sens du changement
     * @param indexAction index actuel du tableau de probabilités
     * @param sensChangement sens du changement à tester
     */
    public float probaInterne(int indexAction, int sensChangement) {
        int indexActuel = indexStrategie[indexAction];
        return probaInterne(indexAction, indexActuel, sensChangement);
    }

    /**
     * retourne la proba à partir de n'importe quelle valeur (peut-être différente de la stratégie actuellement fixée)
     */
    public float probaInterne(int indexAction, int valeurTest, int sensChangement) {
        if (!(changementPossible(indexAction, sensChangement))) return -1;

        float probaChangement = 0;
        if (sensChangement == -1) {
            for (int i = valeurTest - 1; i >= 0; i--) {
                probaChangement += probabilites[indexAction][i];
            }
        }
        else if (sensChangement == 1) {
            for (int i = valeurTest + 1; i < probabilites[indexAction].length; i++) {
                probaChangement += probabilites[indexAction][i];
            }
        }
        else throw new IllegalArgumentException("Le changement doit être 1 ou -1");

        return probaChangement;
    }


    public int nombreActions() {
        return indexStrategie.length;
    }

    // méthodes pour initialiser des stratégies de différerents types

    public void setStrategiePure() {
        //todo
        initialisee = true;
    }

    public void setStrategiePlusProbable() {
        Arrays.fill(indexStrategie, 0);

        // on trouve l'indice de probabilité plus élevé
        for (int i = 0; i < indexStrategie.length; i++) {
            float maxProba = 0;
            for (int j = 0; j < probabilites[i].length; j++) {
                if (probabilites[i][j] > maxProba) {
                    maxProba = probabilites[i][j];
                    indexStrategie[i] = j;
                }
            }
        }

        //on lisse la stratégie
        lisserStrategie();
        strategieTest = Arrays.copyOf(indexStrategie, indexStrategie.length);
        initialisee = true;
    }

    /**
     * on fait en sorte que la somme de la stratégie soit ok
     */
    private void lisserStrategie() {
        while(Arrays.stream(indexStrategie).sum() != maxIndex) {
            int sensChangement = maxIndex > Arrays.stream(indexStrategie).sum() ? 1 : -1;
            float meilleureProba = 0;
            int indexChangement = 0;

            for (int indexAction = 0; indexAction < indexStrategie.length; indexAction++) {
                float proba = probaInterne(indexAction, sensChangement);
                if (proba > meilleureProba) {
                    meilleureProba = proba;
                    indexChangement = indexAction;
                }
            }

            indexStrategie[indexChangement] += sensChangement;
        }
    }

    public void setStrategieMediane() {
        Arrays.fill(indexStrategie, 0);
        int sommeIndex = 0;
        while (sommeIndex < maxIndex) {
            for (int i = 0; i < indexStrategie.length; i++) {
                indexStrategie[i]++;
                if (++sommeIndex >= maxIndex) break;
            }
        }
        strategieTest = Arrays.copyOf(indexStrategie, indexStrategie.length);
        initialisee = true;
    }

    // getters

    public boolean estInitialisee() {
        return initialisee;
    }

    public float[] probabilitesAPlat() {
        if (probabilites == null) throw new RuntimeException("Les probabilités n'ont pas été initialisées");

        int numRows = probabilites.length;
        int numCols = probabilites[0].length;
        float[] flatArray = new float[numRows * numCols];
        int index = 0;

        for (float[] probabilite : probabilites) {
            for (int j = 0; j < numCols; j++) {
                flatArray[index] = probabilite[j];
                index++;
            }
        }

        return flatArray;
    }

    public Strategie copie() {
        return new Strategie(
                Arrays.copyOf(probabilites, probabilites.length),
                Arrays.copyOf(indexStrategie, indexStrategie.length),
                pas, notFolded);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (float valeur : indexStrategie) {
            stringBuilder.append((int) (valeur * pas));
            stringBuilder.append(", ");
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    // fournit l'index actuel de la stratégie
    public int getValeur(int indexAction) {
        return indexStrategie[indexAction];
    }

    public int indexFold() {
        return indexStrategie.length - 1;
    }
}
