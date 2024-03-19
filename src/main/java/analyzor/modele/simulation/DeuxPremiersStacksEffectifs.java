package analyzor.modele.simulation;

import analyzor.modele.utils.Bits;

/**
 * implémentation concrète de stack effectif
 * les stacks doivent être founis en BB pour des raisons de place lors de la génération du long
 * genère une erreur si stacks trop grands
 */
public class DeuxPremiersStacksEffectifs extends StacksEffectifs {
    // todo trouver les bonnes valeurs
    private final static float[] POIDS_STACKS = {1, 0.5f};
    // ne jamais changer cette valeur
    public final static int NUMERO_METHODE = 1;
    private float stackJoueur;
    private float premierStackEffectif;
    private float secondStackEffectif;
    private boolean stackSuperieur = false;
    private int nJoueurs;

    DeuxPremiersStacksEffectifs(float stackJoueur, int nJoueurs) {
        super(NUMERO_METHODE);
        this.nJoueurs = nJoueurs;
        if (stackJoueur == 0) stackJoueur = 1;
        this.stackJoueur = stackJoueur;
        this.premierStackEffectif = stackJoueur;
        this.secondStackEffectif = stackJoueur;
    }

    DeuxPremiersStacksEffectifs(long idStocke) {
        super(NUMERO_METHODE);
        int f1Bits = (int) (idStocke >> 32);
        int f2Bits = (int) idStocke;
        this.premierStackEffectif = Float.intBitsToFloat(f1Bits);
        this.secondStackEffectif = Float.intBitsToFloat(f2Bits);
    }

    public DeuxPremiersStacksEffectifs(float[] valeursStacksEffectifs) {
        super(NUMERO_METHODE);
        if (valeursStacksEffectifs.length != 2) throw new IllegalArgumentException("Plus de deux valeurs");

        premierStackEffectif = valeursStacksEffectifs[0];
        secondStackEffectif = valeursStacksEffectifs[1];
    }

    @Override
    void ajouterStackVillain(float stackVillainPrisEnCompte) {
        if (stackVillainPrisEnCompte > stackJoueur) {
            if (!stackSuperieur) {
                if (secondStackEffectif == stackJoueur) secondStackEffectif = premierStackEffectif;
                premierStackEffectif = stackJoueur;
                stackSuperieur = true;
            }
            return;
        }

        if (nJoueurs == 2) {
            premierStackEffectif = stackVillainPrisEnCompte;
            secondStackEffectif = stackVillainPrisEnCompte;
            return;
        }

        if (secondStackEffectif == stackJoueur) {
            secondStackEffectif = stackVillainPrisEnCompte;
            return;
        }


        if (stackVillainPrisEnCompte < secondStackEffectif && premierStackEffectif == stackJoueur && !stackSuperieur) {
            premierStackEffectif = secondStackEffectif;
            secondStackEffectif = stackVillainPrisEnCompte;
            return;
        }

        if (stackVillainPrisEnCompte > premierStackEffectif) {
            secondStackEffectif = premierStackEffectif;
            premierStackEffectif = stackVillainPrisEnCompte;
            return;
        }

        if (stackVillainPrisEnCompte > secondStackEffectif) {
            secondStackEffectif = stackVillainPrisEnCompte;
        }

    }

    @Override
    public int getDimensions() {
        return getDonnees().length;
    }

    @Override
    public float[] getDonnees() {
        return new float[] {premierStackEffectif, secondStackEffectif};
    }

    @Override
    public float[] getPoidsStacks() {
        return POIDS_STACKS;
    }

    @Override
    public long getIdGenere() {
        int f1Bits = Float.floatToIntBits(premierStackEffectif);
        int f2Bits = Float.floatToIntBits(secondStackEffectif);
        // on s'assure que la deuxième valeur est traitée de manière non signée
        return ((long) f1Bits << 32) | (f2Bits & 0xFFFFFFFFL);
    }

    @Override
    public String toString() {
        return "[STACK EFFECTIFS : 1 => " + premierStackEffectif + ", 2 => " + secondStackEffectif + "]";
    }
}
