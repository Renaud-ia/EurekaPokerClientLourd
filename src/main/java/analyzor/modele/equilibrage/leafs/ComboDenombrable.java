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
    private Strategie strategieTest;
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

    public void incrementerObservation(int indexAction) {
        if (indexAction > (observations.length - 1)) throw new IllegalArgumentException("L'index dépasse la taille max");
        observations[indexAction]++;
        logger.trace("Incrémentation de :  " + this);
        logger.trace("Action d'index " + indexAction + " vaut " + observations[indexAction]);
    }

    public void setShowdown(int indexAction, float valeur) {
        if (indexAction > (pShowdowns.length - 1)) throw new IllegalArgumentException("L'index dépasse la taille max");
        pShowdowns[indexAction] = valeur;
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

        logger.trace("Ajout des probabilités pour : " + this + "(index : " + indexAction + ")");
        for (float probaDiscretisee : probaDiscretisees) {
            logger.trace(probaDiscretisee);
        }
    }

    public void setProbaFold(float[] probaDiscretisees) {
        probabilites[probabilites.length - 1] = probaDiscretisees;
    }

    public int getPFold() {
        return strategieActuelle.foldStrategie();
    }

    public int[] getStrategieSansFold() {
        return strategieActuelle.strategieSansFold();
    }

    public void initialiserStrategie() {
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
        strategieActuelle = new Strategie();
        strategieActuelle.initialiserStrategiePure(indexPureStrategie);
    }

    public void appliquerChangementStrategie() {
        ancienneStrategie = strategieActuelle.copie();
        strategieActuelle = strategieTest.copie();
    }

    public float testerChangementFold(int sensChangement) {
        return this.testerChangementStrategie(probabilites.length -1, sensChangement);
    }

    /**
     * le combo décide lui-même de l'action à ajuster pour rester équilibré
     * gère le cas où il boucle sur lui-même
     * @param indexAction - 1 pour fold
     * @return la probabilité POSITIVE du changement quant aux observations (-1 si changement pas possible)
     * todo : procédure de détection d'un blocage
     */
    public float testerChangementStrategie(int indexAction, int sensChangement) {
        logger.info("Strategie actuelle [" + this + "] : " + strategieActuelle);
        if (sensChangement != 1 && sensChangement != -1)
            throw new IllegalArgumentException("Le changement doit être 1 ou -1");

        // si changement impossible on retourne -1
        if (!(strategieActuelle.changementPossible(indexAction, sensChangement))) return -1;

        // sinon on crée une stratégie de test
        strategieTest = strategieActuelle.copie();
        strategieTest.changerValeur(indexAction, sensChangement);
        int valeurActuelle = strategieActuelle.valeurActuelle(indexAction);
        float probaPremierChangement = probabiliteChangement(indexAction, valeurActuelle, sensChangement);

        // on cherche le changement inverse le plus probable pour équilibrer la stratégie
        int secondChangement = -sensChangement;
        float probaSecondChangement = trouverSecondChangement(indexAction, secondChangement);

        //todo : pas de second changement, on genère une erreur??
        if (probaSecondChangement == 0) return -1;

        // on répercute sur le parent
        this.parent.testerChangementStrategie(this, strategieTest.strategieTotale());

        return probaPremierChangement * probaSecondChangement;
    }

    private float trouverSecondChangement(int indexAction, int secondChangement) {
        int indexSecondChangement = 0;
        float probaSecondChangement = 0;
        for (int i = 0; i < probabilites.length; i++) {
            if (i == indexAction) continue;
            if (!(strategieActuelle.changementPossible(i, secondChangement))) continue;
            int valeurCourante = strategieActuelle.valeurActuelle(i);
            float probaChangement = probabiliteChangement(i, valeurCourante, secondChangement);
            if (probaChangement > probaSecondChangement) {
                probaSecondChangement = probaChangement;
                indexSecondChangement = i;
            }
        }
        strategieTest.changerValeur(indexSecondChangement, secondChangement);
        return probaSecondChangement;
    }

    /**
     * retourne la masse probabilité totale qui va dans le sens du changement
     */
    private float probabiliteChangement(int indexAction, int valeurActuelle, int sensChangement) {
        float[] proba = probabilites[indexAction];
        float probaChangement = 0;
        if (sensChangement == -1) {
            for (int i = valeurActuelle - 1; i >= 0; i--) {
                probaChangement += proba[i];
            }
        }
        else if (sensChangement == 1) {
            for (int i = valeurActuelle + 1; i < proba.length; i++) {
                probaChangement += proba[i];
            }
        }

        else throw new IllegalArgumentException("Le changement doit être 1 ou -1");

        return probaChangement;
    }

    class Strategie {
        private final int[] indexStrategie;
        private final int maxIndex;
        // pour copie
        private Strategie(int[] indexStrategie) {
            int nValeursProbas = (100 / pas) + 1;
            maxIndex = nValeursProbas - 1;
            this.indexStrategie = indexStrategie;
        }
        Strategie() {
            int nValeursProbas = (100 / pas) + 1;
            maxIndex = nValeursProbas - 1;

            int nActions = observations.length + 1;
            indexStrategie = new int[nActions];
        }

        void initialiserStrategiePure(int indexAction) {
            Arrays.fill(indexStrategie, 0);
            indexStrategie[indexAction] = maxIndex;
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
            return (nouvelIndex > 0 && nouvelIndex <= maxIndex);
        }

        public int valeurActuelle(int indexAction) {
            return indexStrategie[indexAction];
        }

        public Strategie copie() {
            return new Strategie(Arrays.copyOf(indexStrategie, indexStrategie.length));
        }

        public void changerValeur(int indexAction, int sensChangement) {
            indexStrategie[indexAction] += sensChangement;
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
    }
}
