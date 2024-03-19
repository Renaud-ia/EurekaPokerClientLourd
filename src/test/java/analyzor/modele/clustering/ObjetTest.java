package analyzor.modele.clustering;

import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.Random;

/**
 * objet de test qui génère des valeurs random
 */
public class ObjetTest extends ObjetClusterisable {
    private final float[] valeursClusterisables;
    public ObjetTest(int nDimensions, float minValue, float maxValue) {
        valeursClusterisables = new float[nDimensions];

        Random random = new Random();
        for (int i = 0; i < nDimensions; i++) {
            valeursClusterisables[i] = (random.nextFloat() * (maxValue - minValue)) + minValue;
        }
    }
    @Override
    protected float[] valeursClusterisables() {
        return valeursClusterisables;
    }
}
