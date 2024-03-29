package analyzor.modele.clustering.algos;

import analyzor.modele.clustering.cluster.DistanceCluster;
import analyzor.modele.clustering.objets.ObjetClusterisable;

import java.util.HashMap;
import java.util.List;


public class TasModifiable<T extends ObjetClusterisable> {
    private int indexValeurMaximum;
    private float[] tasBinaire;
    
    private final HashMap<Long, Integer> positionPaire;
    
    private final HashMap<Integer, DistanceCluster<T>> paireStockee;

    public TasModifiable() {
        positionPaire = new HashMap<>();
        paireStockee = new HashMap<>();
    }

    public void initialiser(List<DistanceCluster<T>> pairesClusters) {
        indexValeurMaximum = pairesClusters.size();
        tasBinaire = new float[pairesClusters.size() + 1];

        
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

        
        float valeurSupprimee = tasBinaire[indexCluster];
        float derniereValeur = tasBinaire[indexValeurMaximum];

        
        transfererValeur(indexValeurMaximum, indexCluster);

        
        
        indexValeurMaximum--;

        
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
        
        while (j <= e) {
            if (j < e) {
                
                if (tasBinaire[j] > tasBinaire[j+1]) j++;
            }
            
            if (valeur <= tasBinaire[j]) break;

            
            transfererValeur(j, i);

            
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
            
            if (tasBinaire[j] <= valeur) break;

            
            transfererValeur(j, i);

            
            i = j;
            j = i / 2;
        }
        tasBinaire[i] = valeur;
        positionPaire.put(paireModifiee.getIndex(), i);
        paireStockee.put(i, paireModifiee);
    }

    
    private void transfererValeur(int j, int i) {
        tasBinaire[i] = tasBinaire[j];
        DistanceCluster<T> clusterEnJ = paireStockee.get(j);
        positionPaire.put(clusterEnJ.getIndex(), i);
        paireStockee.put(i, clusterEnJ);
    }

}
