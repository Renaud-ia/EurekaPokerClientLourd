package analyzor.modele.clustering.range;

import analyzor.modele.clustering.objets.ComboPreClustering;

import java.util.List;

/**
 * hypothèse sur le clustering de range
 * une hypothèse est simplement un nombre de divisions des composantes de l'ACP
 * calcule une erreur lié à une fonction objectif
 */
class HypotheseClustering {
    private static List<ComboPreClustering> combos;
    private float[][] valeursAjustementsCourantes;
    private float coutActuel;

    HypotheseClustering(int[] hypotheses) {
        valeursAjustementsCourantes = new float[hypotheses.length][];
        for (int i = 0; i < hypotheses.length; i++) {
            // il y a une borne de moins que d'hypothèse
            int nBornes = hypotheses[i] - 1;
            valeursAjustementsCourantes[i] = new float[nBornes];
        }

        // todo initialiser les valeurs sur la plage max-min / nombre de bornes
    }

    // interface publique permettant à l'optimiseur de faire tourner les hypothèses

    /**
     * méthode statique pour initialiser les références vers les objets qu'on cherche à optimiser
     */
    static void ajouterCombos(List<ComboPreClustering> combosInitiaux) {
        combos = combosInitiaux;
    }

    /**
     * définit un pas d'ajustement des valeurs
     * @param pctPlageValeur exprimé en % de la plage de valeurs totales
     */
    void setPas(float pctPlageValeur) {

    }

    /**
     * ajuste une fois les valeurs selon le pas défini
     */
    void ajusterValeurs() {
        // on crée les couples de valeurs possibles
        // on calcule le cout de chaque ajustement
        // on fixe l'ajustement actuel
        // on fixe la valeur actuelle du coût
    }

    /**
     * @return le coût de l'ajustement actuel
     */
    float coutAjustementActuel() {
        return coutActuel;
    }

    /**
     * appelé pour récupérer le meilleur clustering quand on a fini d'équilibrer
     * @return les combos regroupés par cluster
     */
    List<List<ComboPreClustering>> clusteringActuel() {
        return null;
    }


    // méthodes privées d'ajustement interne

    /**
     * calcul le cout actuel d'une hypothèse donnée de valeurs de bornes
     */
    private float calculerCout(float[][] hypothèse) {
        // on regroupe les combos

        // on calcule la distance intercluster

        // on calcule la variance intracluster

        // on calcule les effectifs (en nombre de combos servis) de chaque catégorie

        // on renvoie une valeur combinée

        return 0f;
    }



}
