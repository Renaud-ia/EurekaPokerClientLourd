package analyzor.modele.clustering.cluster;

import analyzor.modele.parties.Entree;

import java.util.*;

// contient les centroïdes
public class ClusterSPRB implements ClusterEntree {
    private float stackEffectif;
    private float pot;
    private float potBounty;
    // on regroupe les clusters par idNoeudAbstrait = action
    // comme ça pas besoin de le refaire ensuite
    private HashMap<Long, List<Entree>> entrees;

    public ClusterSPRB() {
        entrees = new HashMap<>();
    }

    public int getEffectif() {
        int effectif = 0;
        for (Long idNoeudAbstrait : entrees.keySet()) {
            effectif += entrees.get(idNoeudAbstrait).size();
        }
        return effectif;
    }

    public float getEffectiveStack() {
        return stackEffectif;
    }

    public float getPot() {
        return pot;
    }

    public float getPotBounty() {
        return potBounty;
    }

    public void ajouterEntree(Entree entree) {
        List<Entree> listeEntrees = entrees.computeIfAbsent(entree.getIdNoeudTheorique(), k -> new ArrayList<>());
        listeEntrees.add(entree);
    }

    public void setStackEffectif(float stackEffectif) {
        this.stackEffectif = stackEffectif;
    }

    public void setPot(float pot) {
        this.pot = pot;
    }

    public void setPotBounty(float potBounty) {
        this.potBounty = potBounty;
    }

    public Set<Long> noeudsPresents() {
        return entrees.keySet();
    }

    public List<Entree> obtenirEntrees(Long idNoeudTheorique) {
        return entrees.get(idNoeudTheorique);
    }
 }
