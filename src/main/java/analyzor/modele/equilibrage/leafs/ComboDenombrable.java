package analyzor.modele.equilibrage.leafs;

import analyzor.modele.clustering.objets.ObjetClusterisable;
import analyzor.modele.equilibrage.Enfant;
import analyzor.modele.equilibrage.NoeudEquilibrage;
import analyzor.modele.poker.evaluation.EquiteFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public abstract class ComboDenombrable extends ObjetClusterisable implements Enfant {
    private final static Logger logger = LogManager.getLogger(ComboDenombrable.class);
    float pCombo;
    private final int[] observations;
    private final float[] pShowdowns;
    private final EquiteFuture equiteFuture;
    private final float equite;
    private NoeudEquilibrage parent;
    // on stocke l'index de la proba
    private Strategie strategieActuelle;
    private Strategie ancienneStrategie;
    private final float[][] probabilites;
    private int pas;

    // important : le fold ne doit pas être compris dans nombre d'actions
    protected ComboDenombrable(float pCombo, EquiteFuture equiteFuture, int nombreActions) {
        this.pCombo = pCombo;
        this.equiteFuture = equiteFuture;
        this.equite = equiteFuture.getEquite();
        this.observations = new int[nombreActions];
        this.pShowdowns = new float[nombreActions];
        // on rajoute le fold
        this.probabilites = new float[nombreActions + 1][];
    }

    public void setPas(int pas) {
        this.pas = pas;
    }

    // utilise pour test, à voir si utile sinon
    public void setObservation(int indexAction, int valeur) {
        if (indexAction > (observations.length - 1)) throw new IllegalArgumentException("L'index dépasse la taille max");
        observations[indexAction] = valeur;
    }

    public void incrementerObservation(int indexAction) {
        if (indexAction > (observations.length - 1)) throw new IllegalArgumentException("L'index dépasse la taille max");
        observations[indexAction]++;
        logger.trace("Incrémentation de :  " + this);
        logger.trace("Action d'index " + indexAction + " vaut " + observations[indexAction]);
    }

    public void setShowdown(int indexAction, float valeur) {
        if (indexAction > (pShowdowns.length - 1)) throw new IllegalArgumentException("L'index dépasse la taille max");
        pShowdowns[indexAction] = valeur;
        logger.trace("%SHOWDOWN fixé (" + this + ") pour action d'index " + indexAction + " : " + valeur);
    }

    @Override
    public float[] valeursClusterisables() {
        return equiteFuture.valeursClusterisables();
    }

    public float getEquite() {
        return equite;
    }

    @Override
    public EquiteFuture getEquiteFuture() {
        return equiteFuture;
    }

    @Override
    public int[] getStrategie() {
        return strategieActuelle.strategieTotale();
    }

    @Override
    public int getEffectif() {
        return 1;
    }
    @Override
    public void setParent(NoeudEquilibrage noeudEquilibrage) {
        this.parent = noeudEquilibrage;
    }

    public float getPCombo() {
        return pCombo;
    }

    int[] getObservations() {
        return observations;
    }

    float[] getShowdowns() {
        return pShowdowns;
    }

    void setProbaAction(int indexAction, float[] probaDiscretisees) {
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
        int nActions = observations.length + 1;
        strategieActuelle = new Strategie(nActions);
        strategiePlusProbable(strategieActuelle);
    }

    public void appliquerChangementStrategie() {
        ancienneStrategie = strategieActuelle.copie();
        strategieActuelle.appliquerValeurTest();
        parent.appliquerChangement();
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
        // on répercute sur le parent
        this.parent.testerChangementStrategie(this, strategieActuelle.getStrategieTest());
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

        //todo : pas de second changement, on genère une erreur??
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
        // on fixe une stratégie médiane
        strategie.setStrategieMediane();

        // puis on change tant que ça s'améliore
        float ancienneProba = 1;
        for (int compte = 0; compte < 20; compte++) {
            int meilleurIndex = 0;
            int meilleurChangement = 0;
            float meilleurProba = 0;
            for (int i = 0; i < observations.length; i++) {
                for (int changement = -1; changement <= 1; changement += 2) {
                    float probaChangement = testerChangementStrategie(strategie, i, changement);
                    if (probaChangement > meilleurProba) {
                        meilleurProba = probaChangement;
                        meilleurIndex = i;
                        meilleurChangement = changement;
                    }
                    strategie.resetTest();
                }
            }
            testerChangementStrategie(strategie, meilleurIndex, meilleurChangement);
            strategie.appliquerValeurTest();
            // si les proba remontent on a atteint l'optimum donc on compte
            if (meilleurProba > ancienneProba) break;
            ancienneProba = meilleurProba;

        }
    }

    private void strategiePure(Strategie strategie) {
        // todo faire une stratégie plus probable??
        //  on peut juste diminuer proportionnellement au % fold la stratégie plus probable sans fold
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
