package analyzor.modele.equilibrage.leafs;

import analyzor.modele.poker.evaluation.EquiteFuture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClusterEquilibrage extends NoeudEquilibrage {
    private final List<ComboDansCluster> combos;
    private float[][] matriceDistance;
    private int[] valeursMinimumStrategie;
    private int[] valeursMaximumStrategie;
    private final float equiteMoyenne;

    /**
     * constructeur
     * @param cluster peut être un cluster ou bien un combo isolé
     */

    public ClusterEquilibrage(List<NoeudEquilibrage> cluster) {
        // todo probablement observations inutiles mais à vérifier
        super(calculerPCombo(cluster),
                calculerObservations(cluster),
                calculerShowdowns(cluster),
                calculerEquite(cluster));

        this.combos = new ArrayList<>();

        equiteMoyenne = calculerEquiteMoyenne(cluster);

        for (NoeudEquilibrage noeudEquilibrage : cluster) {
            if (noeudEquilibrage instanceof ClusterEquilibrage) {
                combos.addAll(((ClusterEquilibrage) noeudEquilibrage).getCombos());
            }
            else if (noeudEquilibrage instanceof ComboIsole) {
                ComboDansCluster combo =
                        new ComboDansCluster(((ComboIsole) noeudEquilibrage), this);
                combos.add(combo);
            }

            else throw new IllegalArgumentException("Type incompatible");
        }

        initialiserProbaFoldEquite(cluster);
    }

    // méthodes de construction

    /**
     * on va faire la moyenne des proba de fold liés à l'équite des combos qui composent le cluster
     * @param cluster
     */
    private void initialiserProbaFoldEquite(List<NoeudEquilibrage> cluster) {
        // gérer les cas ou pas initialisé
        if (cluster.getFirst().getProbaFoldEquite() == null) return;
        probaFoldEquite = new float[cluster.getFirst().getProbaFoldEquite().length];
        Arrays.fill(probaFoldEquite, 0);

        for (NoeudEquilibrage noeudEquilibrage : cluster) {
            for (int i = 0; i < probaFoldEquite.length; i++) {
                probaFoldEquite[i] += noeudEquilibrage.getProbaFoldEquite()[i] / cluster.size();
            }
        }
    }

    private float calculerEquiteMoyenne(List<NoeudEquilibrage> cluster) {
        float equiteTotale = 0f;
        for (NoeudEquilibrage noeudEquilibrage : cluster) {
            equiteTotale += noeudEquilibrage.getEquiteFuture().getEquite();
        }

        return equiteTotale / cluster.size();
    }

    /**
     * méthodes statiques pour initialiser le cluster
     * un peu répététif mais pas très lourd et ça permet de respecter "final"
      */

    private static EquiteFuture calculerEquite(List<NoeudEquilibrage> cluster) {
        List<EquiteFuture> equites = new ArrayList<>();
        List<Float> poidsCombo = new ArrayList<>();

        for (NoeudEquilibrage comboDenombrable : cluster) {
            equites.add(comboDenombrable.getEquiteFuture());
            poidsCombo.add(comboDenombrable.getPCombo());
        }


        return new EquiteFuture(equites, poidsCombo);
    }

    private static float[] calculerShowdowns(List<NoeudEquilibrage> cluster) {
        float sommePCombo = 0;
        int nActionsObservables = cluster.getFirst().nActionsSansFold();
        float[] showdownPondere = new float[nActionsObservables];

        for (NoeudEquilibrage comboDenombrable : cluster) {
            sommePCombo += comboDenombrable.getPCombo();
            for (int i = 0; i < showdownPondere.length; i++) {
                showdownPondere[i] += comboDenombrable.getShowdowns()[i] * comboDenombrable.getPCombo();
            }
        }

        for (int i = 0; i < showdownPondere.length; i++) {
            showdownPondere[i] /= sommePCombo;
        }

        return showdownPondere;
    }

    private static int[] calculerObservations(List<NoeudEquilibrage> cluster) {
        int nActionsObservables = cluster.getFirst().nActionsSansFold();
        int[] sommeObservations = new int[nActionsObservables];

        for (NoeudEquilibrage comboDenombrable : cluster) {
            for (int i = 0; i < sommeObservations.length; i++) {
                sommeObservations[i] += comboDenombrable.getObservations()[i];
            }
        }

        return sommeObservations;
    }

    private static float calculerPCombo(List<NoeudEquilibrage> cluster) {
        float sommePCombo = 0;
        for (NoeudEquilibrage comboDenombrable : cluster) {
            sommePCombo += comboDenombrable.getPCombo();
        }
        return sommePCombo;
    }

    public List<ComboDansCluster> getCombos() {
        return combos;
    }

    @Override
    public String toString() {
        StringBuilder nomCluster = new StringBuilder();
        nomCluster.append("[CLUSTER ( ").append(pCombo).append(") AVEC : ");
        for (ComboDansCluster combo : combos) {
            nomCluster.append(combo.getComboDenombrable());
            nomCluster.append(", ");
        }
        nomCluster.append("]");

        return nomCluster.toString();
    }

    public float equiteMoyenne() {
        return equiteMoyenne;
    }
}
