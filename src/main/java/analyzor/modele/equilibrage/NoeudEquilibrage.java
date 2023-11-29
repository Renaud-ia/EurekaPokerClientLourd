package analyzor.modele.equilibrage;

import analyzor.modele.poker.evaluation.EquiteFuture;

import java.util.HashMap;

/**
 * noeud parent de l'arbre d'équilibrage
 * permet de tenir compte de la proximité d'autres combos pour l'équilibrage
 */
public class NoeudEquilibrage implements Enfant {
    private final int index;
    private final RegressionEquilibrage regression;
    private NoeudEquilibrage parent;
    private final int effectif;
    private int[] strategieActuelle;
    private EquiteFuture equiteMoyenne;
    private float ponderation;
    private final Enfant enfant1;
    private final Enfant enfant2;

    public NoeudEquilibrage(RegressionEquilibrage regression, Enfant enfant1, Enfant enfant2, int index) {
        this.index = index;
        this.regression = regression;
        this.enfant1 = enfant1;
        this.enfant2 = enfant2;

        // on répercute les effectif des enfants
        this.effectif = enfant1.getEffectif() + enfant2.getEffectif();
        if (enfant2.getEffectif() == 0) {
            throw new RuntimeException();
        }

        nouveauxCentroides();
        calculerDispersionEquite();
        nouvelleStrategie();
    }

    public void testerChangementStrategie(Enfant enfantAppelant, int[] nouvelleStrategie) {
        int[] prochaineStrategie;
        float dispersionStrategie;
        // on calcule la nouvelle dispersion avec le changement
        if (enfantAppelant == enfant1) {
            dispersionStrategie = calculerDispersionStrategie(nouvelleStrategie, enfant2.getStrategie());
            prochaineStrategie = centroideStrategie(nouvelleStrategie, enfant2.getStrategie());
        }
        else if (enfantAppelant == enfant2) {
            dispersionStrategie = calculerDispersionStrategie(enfant1.getStrategie(), nouvelleStrategie);
            prochaineStrategie = centroideStrategie(enfant1.getStrategie(), nouvelleStrategie);
        }

        else throw new IllegalArgumentException("L'enfant appelant ne correspond pas");

        // on teste cette valeur dans la régression
        regression.testerValeurDispersion(this, dispersionStrategie);

        // on répercute le changement de stratégie sur le parent s'il y a un parent (root n'en a pas)
        if (parent != null) parent.testerChangementStrategie(this, prochaineStrategie);

    }

    public void appliquerChangement() {
        this.nouveauxCentroides();
        this.nouvelleStrategie();
        parent.appliquerChangement();
    }

    private void nouvelleStrategie() {
        float dispersionStragie = calculerDispersionStrategie(enfant1.getStrategie(), enfant2.getStrategie());
        regression.setDispersionStrategie(this, dispersionStragie);
    }

    /**
     * seulement utilisé à la construction car ne change pas
     */
    private void calculerDispersionEquite() {
        float distanceEquite = enfant1.getEquiteFuture().distance(enfant2.getEquiteFuture());
        regression.setDispersionEquite(this, distanceEquite);
    }

    /**
     * juste la distance euclidienne entre les strategies des deux enfants
     * on ne pondère pas car on veut marquer les "gaps" de regroupement
     */
    private float calculerDispersionStrategie(int[] strategie1, int[] strategie2) {
        if (strategie1.length != strategie2.length)
            throw new IllegalArgumentException("Les stratégies ne font pas la même taille");

        float distanceCarree = 0;
        for (int i = 0; i < strategie1.length; i++) {
            distanceCarree += (float) Math.pow(strategie1[i] - strategie2[i], 2);
        }

        return (float) Math.sqrt(distanceCarree);
    }

    private void nouveauxCentroides() {
        // on calcule le nouveau centroide relativement au poids en effectif des enfants
        this.ponderation = (float) enfant1.getEffectif() / enfant2.getEffectif();
        this.equiteMoyenne = enfant1.getEquiteFuture().multiPonderee(enfant2, ponderation);

        strategieActuelle = centroideStrategie(enfant1.getStrategie(), enfant2.getStrategie());
    }

    private int[] centroideStrategie(int[] strategie1, int[] strategie2) {
        if (strategie1.length != strategie2.length)
            throw new IllegalArgumentException("Les stratégies ne font pas la même taille");

        int[] centroideStrategie = new int[strategie1.length];

        for (int i = 0; i < strategie1.length; i++) {
            centroideStrategie[i] = (int) (
                    (strategie1[i] + ponderation * strategie2[i]) / (1 + ponderation));
        }
        return centroideStrategie;
    }

    @Override
    public EquiteFuture getEquiteFuture() {
        return equiteMoyenne;
    }

    @Override
    public int[] getStrategie() {
        return strategieActuelle;
    }
    @Override
    public int getEffectif() {
        return effectif;
    }

    @Override
    public void setParent(NoeudEquilibrage noeudEquilibrage) {
        this.parent = noeudEquilibrage;
    }

    public int getIndex() {
        return index;
    }
}
