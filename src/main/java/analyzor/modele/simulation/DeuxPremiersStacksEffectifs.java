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
    private final static int MAX_STACK_BB = 16384;
    // ne jamais changer cette valeur
    public final static int NUMERO_METHODE = 1;
    private float stackJoueur;
    private int premierStackEffectif;
    private int secondStackEffectif;

    DeuxPremiersStacksEffectifs(int stackJoueur) {
        super(NUMERO_METHODE);
        if (stackJoueur == 0) stackJoueur = 1;
        this.stackJoueur = stackJoueur;
        this.premierStackEffectif = 1;
        this.secondStackEffectif = 1;
    }

    DeuxPremiersStacksEffectifs(long idStocke) {
        super(NUMERO_METHODE);
        this.premierStackEffectif = (int) (idStocke >> Bits.bitsNecessaires(MAX_STACK_BB));
        long mask = Bits.creerMasque(Bits.bitsNecessaires(premierStackEffectif),
                Bits.bitsNecessaires(MAX_STACK_BB));
        this.secondStackEffectif = (int) (idStocke & mask);
    }

    public DeuxPremiersStacksEffectifs(float[] valeursStacksEffectifs) {
        super(NUMERO_METHODE);
        if (valeursStacksEffectifs.length != 2) throw new IllegalArgumentException("Plus de deux valeurs");

        premierStackEffectif = (int) valeursStacksEffectifs[0];
        secondStackEffectif = (int) valeursStacksEffectifs[1];
    }

    @Override
    void ajouterStackVillain(int stackVillainPrisEnCompte) {
        if (stackVillainPrisEnCompte > MAX_STACK_BB)
            throw new IllegalArgumentException("Stack dépasse le maximum autorisé");

        if (stackVillainPrisEnCompte > premierStackEffectif) {
            premierStackEffectif = (int) Math.min(stackJoueur, stackVillainPrisEnCompte);
        }
        else if (stackVillainPrisEnCompte > secondStackEffectif) {
            secondStackEffectif = (int) Math.min(stackJoueur, stackVillainPrisEnCompte);
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
        return (long) (((long) premierStackEffectif << Bits.bitsNecessaires(MAX_STACK_BB)) + secondStackEffectif);
    }

    @Override
    public String toString() {
        return "[STACK EFFECTIFS : 1 => " + premierStackEffectif + ", 2 => " + secondStackEffectif + "]";
    }
}
