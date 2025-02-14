package analyzor.modele.clustering.cluster;

import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterBetSize {
    private float betSize;
    // on regroupe les clusters par idNoeudAbstrait = action
    // comme ça pas besoin de le refaire ensuite
    private List<Entree> entrees;

    public ClusterBetSize() {
        entrees = new ArrayList<>();
    }


    public void ajouterEntree(Entree entree) {
        this.entrees.add(entree);
    }

    public void setBetSize(float betSize) {
        this.betSize = betSize;
    }

    public int getEffectif() {
        return entrees.size();
    }

    public float getBetSize() {
        return betSize;
    }

    public List<Entree> getEntrees() {
        return entrees;
    }

    public void setBetSizePlusFrequent() {
        if (entrees.isEmpty()) throw new IllegalArgumentException("Aucune entrée dans le cluster");

        // on compte les fréquences
        HashMap<Float, Integer> frequencesBetSize = new HashMap<>();
        for (Entree entree : entrees) {
            System.out.println("ENTREE ID : "+entree.getId());
            System.out.println("BET SIZE :" + entree.getBetSize());
            float betSize = entree.getBetSize();
            Integer frequence = frequencesBetSize.get(betSize);
            if (frequence == null) {
                frequencesBetSize.put(betSize, 1);
            }
            else {
                frequencesBetSize.put(betSize, ++frequence);
            }
        }

        // on récupère le betsize le plus fréquent
        float betSizePlusFrequent = 0; // Initialisation à une valeur par défaut
        int frequenceMax = 0; // Initialisation à 0

        for (Map.Entry<Float, Integer> entry : frequencesBetSize.entrySet()) {
            float betSize = entry.getKey();
            int frequence = entry.getValue();

            if (frequence > frequenceMax) {
                betSizePlusFrequent = betSize; // Met à jour le betSize le plus fréquent
                frequenceMax = frequence; // Met à jour la fréquence maximale
            }
        }

        this.betSize = betSizePlusFrequent;
    }
}
