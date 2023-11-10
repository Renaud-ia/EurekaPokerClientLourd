package analyzor.modele.clustering.algos;

import analyzor.modele.clustering.cluster.DistanceCluster;
import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.HashMap;
import java.util.List;

/**
 * tas (heap) qui trie les éléments du plus petit au plus grand
 * supporte la modification et la suppression des éléments
 */
public class TasModifiable<T extends ObjetClusterisable> {
    private int indexValeurMinimum;
    private int indexValeurMaximum;
    private float[] tasBinaire;
    // Where
    private final HashMap<Long, Integer> positionPaire;
    // Pair
    private final HashMap<Integer, DistanceCluster<T>> paireStockee;

    public TasModifiable() {
        positionPaire = new HashMap<>();
        paireStockee = new HashMap<>();
    }

    public void initialiser(List<DistanceCluster<T>> pairesClusters) {
        indexValeurMinimum = 0;
        indexValeurMaximum = pairesClusters.size();
        tasBinaire = new float[pairesClusters.size()];

        int indexTas = 0;
        for (DistanceCluster<T> distanceCluster : pairesClusters) {
            tasBinaire[indexTas] = distanceCluster.getDistance();
            positionPaire.put(distanceCluster.getIndex(), indexTas);
            paireStockee.put(indexTas, distanceCluster);
            indexTas++;
        }

        trierTas();
    }

    public DistanceCluster<T> pairePlusProche() {
        return paireStockee.get(indexValeurMinimum++);
    }

    public void supprimer(DistanceCluster<T> clusterSupprime) {
        // on échange le premier et le dernier
        int indexCluster = positionPaire.get(clusterSupprime.getIndex());
        float valeurSupprimee = tasBinaire[indexCluster];

        float valeurMax = tasBinaire[indexValeurMaximum];
        tasBinaire[indexCluster] = valeurMax;

        transfererValeur(indexValeurMaximum, indexCluster);

        // on décrémente de 1 la valeur de l'index max
        indexValeurMaximum--;

        // on réaffecte à la bonne place la paire déplacée
        if (valeurSupprimee > valeurMax) {
            deplacerEnBas(indexCluster);
        }
        else if (valeurSupprimee < valeurMax) {
            deplacerEnHaut(indexCluster);
        }
    }

    public void actualiser(DistanceCluster<T> clusterModifie) {
        int indexCluster = positionPaire.get(clusterModifie.getIndex());
        // si la nouvelle valeur est supérieure on le descend
        if (clusterModifie.getDistance() > tasBinaire[indexCluster]) {
            deplacerEnBas(indexCluster);
        }
        else if (clusterModifie.getDistance() < tasBinaire[indexCluster]) {
            deplacerEnHaut(indexCluster);
        }
    }

    private void trierTas() {
        int indexModifie = (indexValeurMaximum / 2) + 1;
        while (indexModifie > 1) {
            indexModifie--;
            deplacerEnBas(indexModifie);
        }
    }

    private void deplacerEnBas(int indexElement) {
        int i = indexElement;
        int j = 2 * i;
        float valeur = tasBinaire[i];
        DistanceCluster<T> paireModifiee = paireStockee.get(i);

        int e = indexValeurMaximum;
        // on parcout les étages inférieurs
        while (j <= e) {
            if (j < e) {
                // on prend la valeur la plus haute des deux branches
                if (tasBinaire[j] > tasBinaire[j+1]) j++;
            }
            // s'il n'y a pas de valeur inférieure, on ne descend pas plus bas
            if (valeur <= tasBinaire[j]) break;

            // valeur inférieure on passe j en i
            tasBinaire[i] = tasBinaire[j];
            // on actualise ses références
            transfererValeur(j, i);

            // on passe au noeud inférieur
            i = j;
            j = 2 * i;
        }

        tasBinaire[i] = valeur;
        positionPaire.put(paireModifiee.getIndex(), i);
        paireStockee.put(i, paireModifiee);
    }

    private void deplacerEnHaut(int indexElement) {
        int i = indexElement;
        int j = i / 2;
        float valeur = tasBinaire[indexElement];
        DistanceCluster<T> paireModifiee = paireStockee.get(i);

        while(j >= 1) {
            // si le noeud inférieur a une valeur inférieure on s'arrête là
            if (tasBinaire[j] <= valeur) break;
            tasBinaire[i] = tasBinaire[j];

            // on actualise ses références
            transfererValeur(j, i);

            // on passe au noeud supérieur
            i = j;
            j = i / 2;
        }
        tasBinaire[i] = valeur;
        positionPaire.put(paireModifiee.getIndex(), i);
        paireStockee.put(i, paireModifiee);
    }

    // la valeur j sera affectée en i
    private void transfererValeur(int j, int i) {
        DistanceCluster<T> clusterEnJ = paireStockee.get(j);
        positionPaire.put(clusterEnJ.getIndex(), i);
        paireStockee.put(i, clusterEnJ);
    }

}
