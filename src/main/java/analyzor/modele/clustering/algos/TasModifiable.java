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
        indexValeurMaximum = pairesClusters.size();
        tasBinaire = new float[pairesClusters.size() + 1];

        // très important : si l'index est 0 alors 2 * 0 = 0 et le tri ne marche plus
        int indexTas = 1;
        for (DistanceCluster<T> distanceCluster : pairesClusters) {
            tasBinaire[indexTas] = distanceCluster.getDistance();
            positionPaire.put(distanceCluster.getIndex(), indexTas);
            paireStockee.put(indexTas, distanceCluster);
            indexTas++;
        }

        trierTas();
    }

    public DistanceCluster<T> pairePlusProche() {
        if (indexValeurMaximum < 1) return null;
        return paireStockee.get(1);
    }

    public void supprimer(long indexPaireSupprimee) {
        Integer indexCluster = positionPaire.get(indexPaireSupprimee);
        if (indexCluster == null) throw new IllegalArgumentException("La paire n'a pas été trouvée");

        // on récupère les valeurs stockées
        float valeurSupprimee = tasBinaire[indexCluster];
        float derniereValeur = tasBinaire[indexValeurMaximum];

        // on échange le premier et le dernier
        transfererValeur(indexValeurMaximum, indexCluster);

        // on décrémente de 1 la valeur de l'index max
        // le cluster supprimé devient inaccessible
        indexValeurMaximum--;

        // on réaffecte à la bonne place la paire déplacée
        if (valeurSupprimee < derniereValeur) {
            tasBinaire[indexCluster] = derniereValeur;
            deplacerEnBas(indexCluster);
        }
        else if (valeurSupprimee > derniereValeur) {
            tasBinaire[indexCluster] = derniereValeur;
            deplacerEnHaut(indexCluster);
        }
    }

    public void actualiser(DistanceCluster<T> nouvellePaire) {
        long idPaire = nouvellePaire.getIndex();
        if (positionPaire.get(idPaire) == null) throw new IllegalArgumentException("La paire n'a pas été trouvée");

        int indexCluster = positionPaire.get(idPaire);
        float nouvelleValeur = nouvellePaire.getDistance();

        float ancienneValeur = tasBinaire[indexCluster];
        tasBinaire[indexCluster] = nouvelleValeur;
        // si la nouvelle valeur est supérieure on le descend
        if (nouvelleValeur > ancienneValeur) {
            deplacerEnBas(indexCluster);
        }
        else if (nouvelleValeur < ancienneValeur) {
            deplacerEnHaut(indexCluster);
        }

        int nouvelIndex = positionPaire.get(idPaire);
        if (paireStockee.get(nouvelIndex) != nouvellePaire)
            throw new RuntimeException("La nouvelle paire ne pointe pas vers l'ancienne");

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

            // on passe j en i
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
        tasBinaire[i] = tasBinaire[j];
        DistanceCluster<T> clusterEnJ = paireStockee.get(j);
        positionPaire.put(clusterEnJ.getIndex(), i);
        paireStockee.put(i, clusterEnJ);
    }

}
