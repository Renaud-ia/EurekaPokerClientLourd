package analyzor.modele.equilibrage.leafs;

import java.util.Arrays;


public class Strategie {
    private int[] indexStrategie;
    private int[] strategieTest;
    private final int maxIndex;
    private final float[][] probabilites;
    private final int pas;
    private boolean initialisee;
    private boolean notFolded;

    private Strategie(float[][] probabilites, int[] indexStrategie, int pas) {
        this.probabilites = probabilites;
        this.pas = pas;
        maxIndex = (100 / pas);
        this.indexStrategie = indexStrategie;
    }

    
    Strategie(float[][] probabilites, int pas, boolean notFolded) {
        this(probabilites, new int[probabilites.length], pas);
        this.notFolded = notFolded;
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

    

    private boolean changementPossible(int indexAction, int sensChangement) {
        int nouvelIndex = indexStrategie[indexAction] + sensChangement;
        return (nouvelIndex >= 0 && nouvelIndex <= maxIndex);
    }

    
    public float probaInterne(int indexAction, int sensChangement) {
        int indexActuel = indexStrategie[indexAction];
        return probaInterne(indexAction, indexActuel, sensChangement);
    }

    
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

    

    @Deprecated
    public void setStrategiePure() {
        
        initialisee = true;
    }

    public void setStrategiePlusProbable() {
        Arrays.fill(indexStrategie, 0);

        
        for (int i = 0; i < indexStrategie.length; i++) {
            float maxProba = 0;
            for (int j = 0; j < probabilites[i].length; j++) {
                if (probabilites[i][j] > maxProba) {
                    maxProba = probabilites[i][j];
                    indexStrategie[i] = j;
                }
            }
        }

        
        lisserStrategie();
        strategieTest = Arrays.copyOf(indexStrategie, indexStrategie.length);
        initialisee = true;
    }

    
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
        int maxIndexModifie;
        if (notFolded) {
            maxIndexModifie = indexStrategie.length - 1;
        }
        else maxIndexModifie = indexStrategie.length;

        int sommeIndex = 0;
        while (sommeIndex < maxIndex) {
            for (int i = 0; i < maxIndexModifie; i++) {
                indexStrategie[i]++;
                if (++sommeIndex >= maxIndex) break;
            }
        }
        strategieTest = Arrays.copyOf(indexStrategie, indexStrategie.length);
        initialisee = true;
    }

    

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
                pas);
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

    
    public int getValeur(int indexAction) {
        return indexStrategie[indexAction];
    }

    public int indexFold() {
        return indexStrategie.length - 1;
    }
}
