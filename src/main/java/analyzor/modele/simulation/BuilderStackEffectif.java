package analyzor.modele.simulation;

import analyzor.modele.utils.Bits;

import java.util.List;

/**
 * assure l'équivalence entre la méthode qui a servi à construire l'identifiant
 * et son enregistremnt dans la la BDD
 * garde sur les deux derniers bits un code qui permet d'identifier
 * la méthode choisie pour assurer la compatibilité si évolution future
 * todo ne sert plus à rien car finalement on encode plus la méthode => trop compliqué et aucun intérêt
 */
public class BuilderStackEffectif {
    public static StacksEffectifs getStacksEffectifs(long idStackEffectif) {
        return new DeuxPremiersStacksEffectifs(idStackEffectif);
    }

    public static long genererId(StacksEffectifs stacksEffectifs) {
        return stacksEffectifs.getIdGenere();
    }

    /**
     * méthode utilisée après le clustering pour recréer un objet de type StackEffectif
     * @param valeursStacksEffectifs valeurs du nouvel objet
     * @param stacksEffectifs on passe un objet pour créer un objet de même insntace
     * @return un nouvel objet stacks effectifs avec les valeurs moyennes
     */
    public static StacksEffectifs getStacksEffectifs(float[] valeursStacksEffectifs, StacksEffectifs stacksEffectifs) {
        if (stacksEffectifs instanceof DeuxPremiersStacksEffectifs) {
            return new DeuxPremiersStacksEffectifs(valeursStacksEffectifs);
        }

        else throw new IllegalArgumentException("Méthode non implémentée");
    }
}
