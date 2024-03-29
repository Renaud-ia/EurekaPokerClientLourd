package analyzor.modele.clustering.objets;

import analyzor.modele.parties.Entree;
import analyzor.modele.simulation.BuilderStackEffectif;
import analyzor.modele.simulation.SituationStackPotBounty;
import analyzor.modele.simulation.StacksEffectifs;

public class EntreeSPRB extends SituationStackPotBounty {
    private final StacksEffectifs stacksEffectifs;
    private final Entree entree;
    
    public EntreeSPRB(Entree entree) {
        super(BuilderStackEffectif.getStacksEffectifs(entree.getCodeStackEffectif()),
                entree.getPotTotal(),
                entree.getPotBounty());
        this.stacksEffectifs = BuilderStackEffectif.getStacksEffectifs(entree.getCodeStackEffectif());
        this.entree = entree;
    }

    public Entree getEntree() {
        return entree;
    }

    public StacksEffectifs getStacksEffectifs() {
        return stacksEffectifs;
    }
}
