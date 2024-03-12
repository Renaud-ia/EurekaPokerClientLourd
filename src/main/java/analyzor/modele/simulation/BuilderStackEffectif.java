package analyzor.modele.simulation;

import analyzor.modele.utils.Bits;

import java.util.List;

/**
 * assure l'équivalence entre la méthode qui a servi à construire l'identifiant
 * et son enregistremnt dans la la BDD
 * garde sur les deux derniers bits un code qui permet d'identifier
 * la méthode choisie pour assurer la compatibilité si évolution future
 */
public class BuilderStackEffectif {
    // occupe 3 bits
    // ne jamais toucher cette valeur!!!!
    private final static int MAX_N_METHODES = 6;
    public static StacksEffectifs getStacksEffectifs(long idStackEffectif) {
        int numeroMethode = (int) (idStackEffectif & Bits.bitsPleins(Bits.bitsNecessaires(MAX_N_METHODES + 1)));
        long idStackSansMethode = idStackEffectif >> (Bits.bitsNecessaires(MAX_N_METHODES + 1));

        if (numeroMethode == DeuxPremiersStacksEffectifs.NUMERO_METHODE) {
            return new DeuxPremiersStacksEffectifs(idStackSansMethode);
        }

        // ajouter les méthodes ICI en veuillant à avoir de numéros de méthode différents

        else throw new IllegalArgumentException("Méthode non implémentée");
    }

    public static long genererId(StacksEffectifs stacksEffectifs) {
        if (stacksEffectifs.getIdGenere() >
                (Long.MAX_VALUE - Bits.bitsPleins(Bits.bitsNecessaires(MAX_N_METHODES + 1)))) {
            throw new IllegalArgumentException("La valeur du long généré est trop longue : "
                    + stacksEffectifs.getIdGenere());
        }
        if (stacksEffectifs.getMethode() > MAX_N_METHODES) {
            throw new IllegalArgumentException("Le numéro de méthode dépasse la valeur max");
        }

        return (stacksEffectifs.getIdGenere() << Bits.bitsNecessaires(MAX_N_METHODES + 1))
                + stacksEffectifs.getMethode();
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
