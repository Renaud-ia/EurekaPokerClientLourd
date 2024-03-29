package analyzor.modele.simulation;

import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.Arrays;


public class SituationStackPotBounty extends ObjetClusterisable {
    
    private final static float[] POIDS_VALEURS = {1, 1, 1};
    private final float[] valeursClusterisables;
    private final float[] poids;

    protected final StacksEffectifs stacksEffectifs;

    public SituationStackPotBounty(StacksEffectifs stacksEffectifs, float pot, float potBounty) {
        this.stacksEffectifs = stacksEffectifs;

        int nDimensionsStacksEffectifs = stacksEffectifs.getDimensions();

        valeursClusterisables = new float[nDimensionsStacksEffectifs + 2];
        poids = new float[nDimensionsStacksEffectifs + 2];

        for (int i = 0; i < nDimensionsStacksEffectifs; i++) {
            valeursClusterisables[i] = stacksEffectifs.getDonnees()[i];
            poids[i] = stacksEffectifs.getPoidsStacks()[i] * POIDS_VALEURS[0];
        }

        valeursClusterisables[nDimensionsStacksEffectifs] = pot;
        valeursClusterisables[nDimensionsStacksEffectifs + 1] = potBounty;

        poids[nDimensionsStacksEffectifs] = POIDS_VALEURS[1];
        poids[nDimensionsStacksEffectifs + 1] = POIDS_VALEURS[2];
    }

    @Override
    protected float[] valeursClusterisables() {
        return valeursClusterisables;
    }

    @Override
    public float[] getPoids() {
        return poids;
    }
}
