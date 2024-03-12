package analyzor.modele.clustering.cluster;

import analyzor.modele.parties.Entree;
import analyzor.modele.simulation.BuilderStackEffectif;
import analyzor.modele.simulation.StacksEffectifs;

import java.util.*;

// contient les centroïdes
public class ClusterSPRB {
    private StacksEffectifs stackEffectifMoyen;
    private float potMoyen;
    private float potBountyMoyen;
    // on regroupe les clusters par idNoeudAbstrait = action
    // comme ça pas besoin de le refaire ensuite
    private final HashMap<Long, List<Entree>> entrees;

    public ClusterSPRB() {
        entrees = new HashMap<>();
    }

    public void ajouterEntree(Entree entree) {
        List<Entree> listeEntrees = entrees.computeIfAbsent(entree.getIdNoeudTheorique(), k -> new ArrayList<>());
        listeEntrees.add(entree);
    }

    /**
     * méthode appelée à la fin pour construire tous les objets
     * important, les données qu'on va garder sont des données normalisées
     * @param stacksEffectifs on fournit un exemplaire de stack effectif pour reconstruire
     */
    public void clusteringTermine(StacksEffectifs stacksEffectifs, float[] centroideCluster) {
        // on crée un nouvel objet stack effectif
        int borneSuperieureStacks = centroideCluster.length - 2;
        float[] valeursStacksEffectifs = Arrays.copyOfRange(centroideCluster, 0, borneSuperieureStacks);
        stackEffectifMoyen = BuilderStackEffectif.getStacksEffectifs(valeursStacksEffectifs, stacksEffectifs);

        // on récupère potMoyen et potBountyMoyen
        potMoyen = centroideCluster[borneSuperieureStacks];
        potBountyMoyen = centroideCluster[borneSuperieureStacks + 1];
    }


    // getters pour obtenir les données

    public Set<Long> noeudsPresents() {
        return entrees.keySet();
    }

    public List<Entree> obtenirEntrees(Long idNoeudTheorique) {
        return entrees.get(idNoeudTheorique);
    }

    public long getIdPremierNoeud() {
        return entrees.keySet().iterator().next();
    }

    public StacksEffectifs getStackEffectif() {
        return stackEffectifMoyen;
    }

    public float getPot() {
        return potMoyen;
    }

    public float getPotBounty() {
        return potBountyMoyen;
    }

    public int getEffectif() {
        int effectif = 0;
        for (Long idNoeudAbstrait : entrees.keySet()) {
            effectif += entrees.get(idNoeudAbstrait).size();
        }
        return effectif;
    }
}
