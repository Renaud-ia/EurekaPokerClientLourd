package analyzor.modele.clustering;

import analyzor.modele.clustering.cluster.ClusterBetSize;
import analyzor.modele.parties.Entree;

import java.util.*;

/**
 * classe chargée de clusteriser les entrées par BetSize
 */
public class SpecialBetSize {
    private final static float SEUIL_SIGNIFICATIVITE = 0.1f;
    private final static float ECART_MINIMUM_PCT = 0.25f;
    private final int maxNombreBetSize;
    private List<Entree> donneesDepart;
    private int totalEffectif;
    public SpecialBetSize(int nBetSize) {
        this.maxNombreBetSize = nBetSize;
    }
    public void ajouterDonnees(List<Entree> donneesEntrees) {
        this.donneesDepart = donneesEntrees;
        this.totalEffectif = donneesEntrees.size();
    }

    public List<ClusterBetSize> construireClusters(int minEffectifBetSize) {
        HashMap<Float, List<Entree>> betSizesRegroupes = regrouperEntreesParBetSize(donneesDepart);

        List<Float> betSizesChoisis = choisirBetSizes(betSizesRegroupes);
        if (betSizesChoisis == null) return unSeulCluster(betSizesRegroupes);
        float[] bornes = creerBornes(betSizesChoisis);

        return regrouperParClusters(betSizesRegroupes, bornes);
    }


    private HashMap<Float, List<Entree>> regrouperEntreesParBetSize(List<Entree> donneesEntrees) {
        HashMap<Float, List<Entree>> betSizesRegroupes = new HashMap<>();

        for (Entree entree : donneesEntrees) {
            float betSize = entree.getBetSize();

            betSizesRegroupes.computeIfAbsent(betSize, k -> new ArrayList<>()).add(entree);
        }

        return betSizesRegroupes;
    }

    /**
     * return null si pas plus de 1 bet size retenu
     * @param betSizesRegroupes entrées regroupées par betSize
     * @return la liste des bet sizes retenus
     */
    private List<Float> choisirBetSizes(HashMap<Float, List<Entree>> betSizesRegroupes) {
        Float betSizePlusFrequent = betSizePlusFrequent(betSizesRegroupes);

        if (betSizePlusFrequent == null) {
            return null;
        }

        List<Float> betSizesChoisis = new ArrayList<>();
        betSizesChoisis.add(betSizePlusFrequent);

        Float prochainBetSize;
        while((prochainBetSize = trouverProchainBetSize(betSizesRegroupes, betSizesChoisis)) != null) {
            betSizesChoisis.add(prochainBetSize);
        }

        if (betSizesChoisis.size() <= 1) return null;

        return betSizesChoisis;
    }

    /**
     * trouve le prochain betsize qu'on va choisir en fonction de ceux qui ont déjà été choisis
     * 2 critères : assez loin des betsize précédent + significatif
     * si plusieurs remplissent les critères, renvoient le plus éloigné des deux autres
     * @param betSizesRegroupes entrées regroupés par bet size
     * @param betSizesChoisis betSize déjà choisis
     * @return le float correspond au bet size, null si aucun betsize choisi
     */
    private Float trouverProchainBetSize(HashMap<Float, List<Entree>> betSizesRegroupes, List<Float> betSizesChoisis) {
        if (betSizesChoisis.size() >= maxNombreBetSize) return null;

        Float prochainBetSize = null;
        float ecartMax = Float.MIN_VALUE;

        for (float betSizeTeste : betSizesRegroupes.keySet()) {
            if (betSizesChoisis.contains(betSizeTeste)) continue;
            if ((float) betSizesRegroupes.get(betSizeTeste).size() / totalEffectif < SEUIL_SIGNIFICATIVITE) continue;

            float distanceTotale = 0;
            boolean assezLoin = true;

            for (float betSizeDejaChoisi : betSizesChoisis) {
                float distance = (Math.abs(betSizeTeste - betSizeDejaChoisi) / betSizeDejaChoisi);
                if (distance < ECART_MINIMUM_PCT) {
                    assezLoin = false;
                    break;
                }
                distanceTotale += distance;
            }

            if (!assezLoin) continue;

            if (distanceTotale > ecartMax) {
                ecartMax = distanceTotale;
                prochainBetSize = betSizeTeste;
            }
        }

        return prochainBetSize;
    }

    /**
     * on prend des bornes au milieu
     * @param betSizesChoisis betSizeRetenus
     * @return un tableau de bornes incluant minimum et maximum
     */
    private float[] creerBornes(List<Float> betSizesChoisis) {
        Float[] betSizesTries = betSizesChoisis.toArray(new Float[0]);

        Arrays.sort(betSizesTries);

        // Convertir le tableau trié en un tableau float[]
        float[] bornesFinalesAvecMinMax = new float[betSizesTries.length + 1];
        bornesFinalesAvecMinMax[0] = Float.MIN_VALUE;
        bornesFinalesAvecMinMax[bornesFinalesAvecMinMax.length - 1] = Float.MAX_VALUE;

        for (int i = 0; i < betSizesTries.length; i++) {
            if (i == betSizesTries.length - 1) break;

            bornesFinalesAvecMinMax[i + 1] = (betSizesTries[i] + betSizesTries[i + 1]) / 2;
        }

        return bornesFinalesAvecMinMax;
    }


    private Float betSizePlusFrequent(HashMap<Float, List<Entree>> betSizesRegroupes) {
        Float betSizePlusFrequent = null;
        int maxEffectif = Integer.MIN_VALUE;

        for (Float valeurBetSize : betSizesRegroupes.keySet()) {
            int effectifCeBetSize = betSizesRegroupes.get(valeurBetSize).size();
            if (((float) effectifCeBetSize / totalEffectif) < SEUIL_SIGNIFICATIVITE) continue;

            if (effectifCeBetSize > maxEffectif) {
                maxEffectif = effectifCeBetSize;
                betSizePlusFrequent = valeurBetSize;
            }
        }

        return betSizePlusFrequent;
    }

    private List<ClusterBetSize> regrouperParClusters(HashMap<Float, List<Entree>> betSizesRegroupes,
                                                      float[] bornesClustering) {
        List<ClusterBetSize> clusterBetSizes = new ArrayList<>();

        for (int i = 0; i < bornesClustering.length; i++) {
            ClusterBetSize nouveauCluster = new ClusterBetSize();
            if (i == bornesClustering.length -1) break;


            float borneInferieure = bornesClustering[i];
            float borneSuperieure = bornesClustering[i + 1];

            float betSizePlusFrequent = 0;
            int maxEffectif = Integer.MIN_VALUE;

            for (float betSize : betSizesRegroupes.keySet()) {
                if (betSize < borneInferieure || betSize >= borneSuperieure) continue;

                for (Entree entreeCorrespondante : betSizesRegroupes.get(betSize)) {
                    nouveauCluster.ajouterEntree(entreeCorrespondante);
                }

                // on prend le betSize le plus fréquent pour l'attribuer au groupe
                int effectif = betSizesRegroupes.get(betSize).size();
                if (effectif > maxEffectif) {
                    maxEffectif = effectif;
                    betSizePlusFrequent = betSize;
                }
            }

            nouveauCluster.setBetSize(betSizePlusFrequent);
            clusterBetSizes.add(nouveauCluster);
        }

        return clusterBetSizes;
    }

    /**
     * retourne un seul cluster avec un bet size fixé sur le plus fréquent
     * @param betSizesRegroupes entreés regroupées par betSize
     * @return un seul cluster avec un bet size fixé sur le plus fréquent
     */
    private List<ClusterBetSize> unSeulCluster(HashMap<Float, List<Entree>> betSizesRegroupes) {
        ClusterBetSize clusterBetSize = new ClusterBetSize();

        for (Entree entree : donneesDepart) {
            clusterBetSize.ajouterEntree(entree);
        }

        int maxEffectif = Integer.MIN_VALUE;
        float betSizePlusFrequent = 0;

        for (float betSize : betSizesRegroupes.keySet()) {
            int effectif = betSizesRegroupes.get(betSize).size();

            if (effectif > maxEffectif) {
                maxEffectif = effectif;
                betSizePlusFrequent = betSize;
            }
        }

        clusterBetSize.setBetSize(betSizePlusFrequent);

        List<ClusterBetSize> unSeulCluster = new ArrayList<>();
        unSeulCluster.add(clusterBetSize);


        return unSeulCluster;
    }
}
