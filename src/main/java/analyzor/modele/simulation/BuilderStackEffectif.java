package analyzor.modele.simulation;

import analyzor.modele.utils.Bits;

import java.util.List;


public class BuilderStackEffectif {
    public static StacksEffectifs getStacksEffectifs(long idStackEffectif) {
        return new DeuxPremiersStacksEffectifs(idStackEffectif);
    }

    public static long genererId(StacksEffectifs stacksEffectifs) {
        return stacksEffectifs.getIdGenere();
    }

    
    public static StacksEffectifs getStacksEffectifs(float[] valeursStacksEffectifs, StacksEffectifs stacksEffectifs) {
        if (stacksEffectifs instanceof DeuxPremiersStacksEffectifs) {
            return new DeuxPremiersStacksEffectifs(valeursStacksEffectifs);
        }

        else throw new IllegalArgumentException("Méthode non implémentée");
    }
}
