package analyzor.modele.equilibrage;

import analyzor.modele.poker.evaluation.EquiteFuture;

public interface Enfant {
    EquiteFuture getEquiteFuture();
    int[] getStrategie();
    int getEffectif();
}
