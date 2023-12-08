package analyzor.modele.equilibrage;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.equilibrage.leafs.ComboDenombrable;
import analyzor.modele.poker.evaluation.EquiteFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * objet manipulé par ProbaEquilibrage et l'équilibrateur
 * contient un ou plusieurs combos dénombrables
 */
public class NoeudEquilibrage extends ObjetClusterisable {
    private final static Logger logger = LogManager.getLogger(NoeudEquilibrage.class);
    // pour clustering de la range => plus le poids est elevé
    private final static float POIDS_EQUITE = 1;
    private final static float POIDS_STRATEGIE = 1;
    private float[] poidsClustering;
    private final List<ComboDenombrable> combos;
    private float pCombo;
    private final int[] observations;
    private final float[] pShowdowns;
    private final EquiteFuture equiteFuture;
    private NoeudEquilibrage parent;
    private Strategie strategieActuelle;
    private Strategie ancienneStrategie;
    private final float[][] probabilites;
    private int pas;
    private boolean notFolded;
    private int[] valeursMinimumStrategie;
    private int[] valeursMaximumStrategie;
    public NoeudEquilibrage(List<ComboDenombrable> cluster) {
        float sommePCombo = 0;
        int nActionsObservables = cluster.get(0).nObservations();
        int[] sommeObservations = new int[nActionsObservables];
        float[] showdownMoyen = new float[nActionsObservables];
        List<EquiteFuture> equites = new ArrayList<>();
        List<Float> poidsCombo = new ArrayList<>();

        for (ComboDenombrable comboDenombrable : cluster) {
            sommePCombo += comboDenombrable.getPCombo();
            for (int i = 0; i < sommeObservations.length; i++) {
                sommeObservations[i] += comboDenombrable.getObservations()[i];
                showdownMoyen[i] += comboDenombrable.getShowdowns()[i] * comboDenombrable.getPCombo();
                equites.add(comboDenombrable.getEquiteFuture());
                poidsCombo.add(comboDenombrable.getPCombo());
            }
        }

        for (int i = 0; i < showdownMoyen.length; i++) {
            showdownMoyen[i] /= sommePCombo;
        }

        this.pCombo = sommePCombo;
        this.observations = sommeObservations;
        this.pShowdowns = showdownMoyen;
        this.probabilites = new float[nActionsObservables + 1][];
        this.equiteFuture = new EquiteFuture(equites, poidsCombo);
        this.combos = cluster;
    }

    public NoeudEquilibrage(ComboDenombrable comboDenombrable) {
        this.pCombo = comboDenombrable.getPCombo();
        this.observations = comboDenombrable.getObservations();
        this.pShowdowns = comboDenombrable.getShowdowns();
        this.probabilites = new float[comboDenombrable.nObservations() + 1][];
        this.equiteFuture = comboDenombrable.getEquiteFuture();
        this.combos = new ArrayList<>();
        combos.add(comboDenombrable);
    }

    // méthode utilisée quand on rajoute des combos isolés à un cluster déjà formé
    // todo est ce qu'on recalcule les centres d'équité??
    public void ajouterCombo(ComboDenombrable comboDenombrable) {
        for (int i = 0; i < observations.length; i++) {
            // on ajoute les observations
            observations[i] += comboDenombrable.getObservations()[i];
            // on calcule showdown de manière pondérée
            pShowdowns[i] =
                    (comboDenombrable.getShowdowns()[i] * comboDenombrable.getPCombo() + pCombo * pShowdowns[i])
                            / (pCombo + comboDenombrable.getPCombo());

        }
        this.pCombo += comboDenombrable.getPCombo();
        this.combos.add(comboDenombrable);
    }


    public int[] getStrategie() {
        return strategieActuelle.strategieTotale();
    }

    public void setParent(NoeudEquilibrage noeudEquilibrage) {
        this.parent = noeudEquilibrage;
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

    public void setProbaAction(int indexAction, float[] probaDiscretisees) {
        if (indexAction >= probabilites.length - 1)
            throw new IllegalArgumentException("L'index de l'action dépasse");
        probabilites[indexAction] = probaDiscretisees;
    }

    public void setProbaFold(float[] probaDiscretisees) {
        logger.trace("Ajout des probabilités pour : " + this + "(fold)");
        probabilites[probabilites.length - 1] = probaDiscretisees;
    }

    public int getPFold() {
        return strategieActuelle.foldStrategie();
    }

    public int[] getStrategieSansFold() {
        return strategieActuelle.strategieSansFold();
    }

    public void initialiserStrategie() {
        // todo on peut tester différents algo d'initialisation (pure, médiane etc)
        setStrategiePlusProbable();
    }

    public void setStrategiePlusProbable() {
        int nActions = observations.length + 1;
        strategieActuelle = new Strategie(nActions);
        strategiePlusProbable(strategieActuelle);
    }

    public void appliquerChangementStrategie() {
        ancienneStrategie = strategieActuelle.copie();
        strategieActuelle.appliquerValeurTest();
    }

    public float testerChangementFold(int sensChangement) {
        return this.testerChangementStrategie(probabilites.length -1, sensChangement);
    }

    /**
     * méthode publique pour estimer le changement le plus probable
     * transmet la stratégie test la plus probable au parent
     */
    public float testerChangementStrategie(int indexAction, int sensChangement) {
        // important il faut reset les tests précédents
        strategieActuelle.resetTest();
        float probaChangement = testerChangementStrategie(strategieActuelle, indexAction, sensChangement);
        if (probaChangement == -1) return -1;
        return probaChangement;
    }

    /**
     * calcul la probabilité d'un changement donné
     * le combo décide lui-même de l'action à ajuster pour rester équilibré
     * gère le cas où il boucle sur lui-même
     * @param indexAction - 1 pour fold
     * @return la probabilité POSITIVE du changement quant aux observations (-1 si changement pas possible)
     * todo : procédure de détection d'un blocage (=boucle)
     */
    private float testerChangementStrategie(Strategie strategieChangee, int indexAction, int sensChangement) {
        logger.trace("Strategie actuelle [" + this + "] : " + strategieChangee);
        logger.trace("Changement demande d'index : " + indexAction + "dans le sens de : " + sensChangement);
        if (sensChangement != 1 && sensChangement != -1)
            throw new IllegalArgumentException("Le changement doit être 1 ou -1");

        // si changement impossible on retourne -1
        if (!(strategieChangee.changementPossible(indexAction, sensChangement))) return -1;

        // sinon on crée une stratégie de test
        strategieChangee.testerValeur(indexAction, sensChangement);
        int valeurActuelle = strategieChangee.valeurActuelle(indexAction);
        float[] probaStrategie = probabilites[indexAction];
        float probaPremierChangement = probabiliteChangement(probaStrategie, valeurActuelle, sensChangement);

        // on cherche le changement inverse le plus probable pour équilibrer la stratégie
        int secondChangement = -sensChangement;
        float probaSecondChangement = trouverSecondChangement(strategieChangee, indexAction, secondChangement);

        if (probaSecondChangement == -1)
            throw new RuntimeException("Second changement impossible, probleme probable de stratégie");

        logger.trace("ProbaChangement vaut : " + probaPremierChangement * probaSecondChangement);

        return probaPremierChangement * probaSecondChangement;
    }

    private float trouverSecondChangement(Strategie strategieChangee, int indexAction, int secondChangement) {
        int indexSecondChangement = 0;
        float probaSecondChangement = -1;
        for (int i = 0; i < strategieChangee.nombreActions(); i++) {
            if (i == indexAction) continue;
            if (!(strategieChangee.changementPossible(i, secondChangement))) continue;
            int valeurCourante = strategieChangee.valeurActuelle(i);
            float[] probaStrategie = probabilites[i];
            float probaChangement = probabiliteChangement(probaStrategie, valeurCourante, secondChangement);
            if (probaChangement > probaSecondChangement) {
                probaSecondChangement = probaChangement;
                indexSecondChangement = i;
            }
        }
        strategieChangee.testerValeur(indexSecondChangement, secondChangement);
        return probaSecondChangement;
    }

    /**
     * retourne la masse probabilité totale qui va dans le sens du changement
     * @param probaStrategie tableau des probabilités
     * @param indexActuel index actuel du tableau de probabilités
     * @param sensChangement sens du changement à tester
     */
    private float probabiliteChangement(float[] probaStrategie, int indexActuel, int sensChangement) {
        float probaChangement = 0;
        if (sensChangement == -1) {
            for (int i = indexActuel - 1; i >= 0; i--) {
                probaChangement += probaStrategie[i];
            }
        }
        else if (sensChangement == 1) {
            for (int i = indexActuel + 1; i < probaStrategie.length; i++) {
                probaChangement += probaStrategie[i];
            }
        }
        else throw new IllegalArgumentException("Le changement doit être 1 ou -1");

        // cas où stratégie est ne peut pas fold
        if (probaChangement == 0) probaChangement = -1;

        return probaChangement;
    }

    /**
     * méthode intermédiaire utilisée par le calcul de probas
     * On calcule la stratégie optimale sans fold pour estimer le taux de fold le plus important
     */
    public int[] strategiePlusProbableSansFold() {
        int nActions = observations.length;
        Strategie strategieSansFold = new Strategie(nActions);

        // puis on change la stratégie tant que ça améliore le chmilblik
        strategiePlusProbable(strategieSansFold);

        return strategieSansFold.strategieTotale();
    }

    /**
     * va déterminer la stratégie la plus probable une fois les proba calculées
     */
    private void strategiePlusProbable(Strategie strategie) {
        strategie.setStrategiePlusProbable();
    }

    private void strategiePure(Strategie strategie) {
        // on fait une stratégie pure sur le mouvement le plus probable
        float plusHauteProba = 0;
        int indexPureStrategie = 0;
        int indexAction = 0;
        for (float[] probasAction : probabilites) {
            float pPureStrategie = probasAction[probasAction.length -1];
            if (pPureStrategie > plusHauteProba) {
                plusHauteProba = pPureStrategie;
                indexPureStrategie = indexAction;
            }
            indexAction++;
        }
        strategieActuelle.initialiserStrategiePure(indexPureStrategie);
    }

    public void setPas(int pas) {
        this.pas = pas;
    }


    // pour clustering

    @Override
    public float[] valeursClusterisables() {
        // attention si modifié modifier aussi les poids

        // on met à plat les probabilités car écart de stratégie = 0 et ça déforme le clustering
        float[] strategieFloat = probabilitesAPlat();
        float[] equiteAPlat = equiteFuture.aPlat();
        int tailleTotale = strategieFloat.length + equiteAPlat.length;

        float[] valeursClusterisables = new float[tailleTotale];
        System.arraycopy(strategieFloat, 0, valeursClusterisables, 0, strategieFloat.length);
        System.arraycopy(equiteAPlat, 0, valeursClusterisables, strategieFloat.length, equiteAPlat.length);

        // on prend en compte que proba a plus de poids car plus de data
        float poidsDonneesRelatif = (float) strategieFloat.length / equiteAPlat.length;

        poidsClustering = new float[tailleTotale];
        for (int i = 0; i < tailleTotale; i++) {
            if (i < strategieFloat.length) {
                poidsClustering[i] = POIDS_STRATEGIE;
            }
            else poidsClustering[i] = POIDS_EQUITE * poidsDonneesRelatif;
        }

        return valeursClusterisables;
    }

    private float[] probabilitesAPlat() {
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

    public float[] getPoids() {
        return poidsClustering;
    }

    public List<ComboDenombrable> getCombosDenombrables() {
        return combos;
    }

    public String toString() {
        StringBuilder nomNoeud = new StringBuilder();
        nomNoeud.append("NOEUD EQUILIBRAGE AVEC COMBOS : [");
        for (ComboDenombrable comboDenombrable : combos) {
            nomNoeud.append(comboDenombrable);
            nomNoeud.append(", ");
        }
        nomNoeud.append("]");
        return nomNoeud.toString();
    }

    public ObjetClusterisable getEquiteFuture() {
        return equiteFuture;
    }

    public void setNotFolded(boolean notFolded) {
        this.notFolded = notFolded;
    }

    public boolean notFolded() {
        return notFolded;
    }


    /**
     * classe qui stocke les stratégies
     * la valeur contenue dans une stratégie correspond à l'index de sa proba
     * garde en mémoire les stratégies de test
     * retourne les stratégies sous forme de % en multipliant par le pas
     */
    class Strategie {
        private int[] indexStrategie;
        private int[] strategieTest;
        private final int maxIndex;
        // pour copie
        private Strategie(int[] indexStrategie) {
            // l'index commence à zéro donc pas de - 1
            maxIndex = (100 / pas);
            this.indexStrategie = indexStrategie;
        }
        Strategie(int nActions) {
            maxIndex = (100 / pas);

            indexStrategie = new int[nActions];
        }

        void initialiserStrategiePure(int indexAction) {
            Arrays.fill(indexStrategie, 0);
            indexStrategie[indexAction] = maxIndex;
            strategieTest = Arrays.copyOf(indexStrategie, indexStrategie.length);
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
        }

        public void setStrategiePlusProbable() {
            Arrays.fill(indexStrategie, 0);

            // on trouve l'indice de probabilité plus élevé
            int[] indexPlusProbables = Arrays.copyOf(indexStrategie, indexStrategie.length);
            for (int i = 0; i < indexPlusProbables.length; i++) {
                float maxProba = 0;
                for (int j = 0; j < probabilites[i].length; j++) {
                    if (probabilites[i][j] > maxProba) {
                        maxProba = probabilites[i][j];
                        indexPlusProbables[i] = j;
                    }
                }
            }

            // on incrémente au fur et à mesure les index qui ont le plus besoin d'augmenter
            int sommeIndex = 0;
            while (sommeIndex < maxIndex) {
                int indexPlusEloigne = 0;
                int valeurPlusEloigne = -1000;

                for (int i = 0; i < indexPlusProbables.length; i++) {
                    int distanceIndex = indexPlusProbables[i] - indexStrategie[i];
                    if (distanceIndex > valeurPlusEloigne) {
                        indexPlusEloigne = i;
                        valeurPlusEloigne = distanceIndex;
                    }
                }
                indexStrategie[indexPlusEloigne]++;
                sommeIndex++;
            }
            strategieTest = Arrays.copyOf(indexStrategie, indexStrategie.length);
        }

        int[] strategieSansFold() {
            int[] strategie = new int[this.indexStrategie.length - 1];
            for (int i = 0; i < indexStrategie.length -1; i++) {
                strategie[i] = indexStrategie[i] * pas;
            }

            return strategie;
        }

        int foldStrategie() {
            return this.indexStrategie[indexStrategie.length - 1] * pas;
        }

        public int[] strategieTotale() {
            int[] strategie = new int[this.indexStrategie.length];
            for (int i = 0; i < indexStrategie.length; i++) {
                strategie[i] = indexStrategie[i] * pas;
            }

            return strategie;
        }

        public boolean changementPossible(int indexAction, int sensChangement) {
            int nouvelIndex = indexStrategie[indexAction] + sensChangement;
            return (nouvelIndex >= 0 && nouvelIndex <= maxIndex);
        }

        public int valeurActuelle(int indexAction) {
            return indexStrategie[indexAction];
        }

        public Strategie copie() {
            return new Strategie(Arrays.copyOf(indexStrategie, indexStrategie.length));
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

        public int[] getStrategieTest() {
            int[] strategie = new int[this.strategieTest.length];
            for (int i = 0; i < strategieTest.length; i++) {
                strategie[i] = strategieTest[i] * pas;
            }

            return strategie;
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


        public int nombreActions() {
            return indexStrategie.length;
        }
    }
}
