package analyzor.modele.clustering;

import analyzor.modele.clustering.algos.DBScan;
import analyzor.modele.clustering.cluster.ClusterDBSCAN;
import analyzor.modele.clustering.cluster.ClusterFusionnable;
import analyzor.modele.clustering.objets.MinMaxCalcul;
import analyzor.modele.clustering.objets.ObjetIndexable;
import analyzor.modele.equilibrage.NoeudEquilibrage;
import analyzor.modele.equilibrage.leafs.ComboDenombrable;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

@Deprecated
// todo finir regroupement clusters isolés
public class DbScanEquilibrage extends DBScan<NoeudEquilibrage> {
    private final static float MIN_PCT_RANGE = 0.02f;
    private final static float SEUIL_EPSILON = 0.5f;
    private final PriorityQueue<Float> distances;

    public DbScanEquilibrage() {
        super();
        distances = new PriorityQueue<>();
    }

    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) {
        normaliserDonnees(noeuds);
        fixerEpsilon(noeuds);
        construireDonnees(noeuds);
    }

    public void lancerClustering() {
        this.clusteriserDonnees();
    }

    public List<NoeudEquilibrage> getResultats() {
        List<NoeudEquilibrage> resultats = new ArrayList<>();

        for (ClusterDBSCAN<NoeudEquilibrage> cluster : clusters) {
            logger.debug("###CLUSTER FORME###");
            List<ComboDenombrable> combosDansNoeud = new ArrayList<>();
            for (NoeudEquilibrage noeudEquilibrage : cluster.getObjets()) {
                logger.trace(noeudEquilibrage);
                logger.trace(Arrays.toString(noeudEquilibrage.getStrategie()));
                combosDansNoeud.addAll(noeudEquilibrage.getCombosDenombrables());
            }
            NoeudEquilibrage noeudParent = new NoeudEquilibrage(combosDansNoeud);
            resultats.add(noeudParent);
        }

        regrouperClustersIsoles();

        return resultats;
    }

    private void regrouperClustersIsoles() {
        for (ObjetIndexable<NoeudEquilibrage> point : pointsDepart) {
            if (pointParcouru.get(point)) continue;
            //todo regrouper les points par équité
        }
    }

    private void fixerEpsilon(List<NoeudEquilibrage> noeuds) {
        //on calcule toutes les distances
        float moyenneDistance = 0;
        int nombreDePaires = 0;
        for (int i = 0; i < noeuds.size(); i++) {
            NoeudEquilibrage noeud1 = noeuds.get(i);
            for (int j = i + 1; j < noeuds.size(); j++) {
                NoeudEquilibrage noeud2 = noeuds.get(j);
                this.distances.add(noeud1.distance(noeud2));
                moyenneDistance += noeud1.distance(noeud2);
                logger.trace("distance entre " + noeud1 + "et " + noeud2 + " : " + noeud1.distance(noeud2));
                nombreDePaires++;
            }
        }

        moyenneDistance /= nombreDePaires;

        logger.trace("Moyenne de la distance : " + moyenneDistance);

        int nombreValeurs = noeuds.size();

        if (distances.isEmpty()) throw new IllegalArgumentException("Noeuds vides");

        float epsilon = distances.poll();
        int compte = 1;
        while(!distances.isEmpty()) {
            epsilon = distances.poll();
            compte++;
            if (((float) compte / nombreValeurs) > SEUIL_EPSILON) break;
        }

        this.setEpsilon(moyenneDistance / 2);
    }

    // todo : fait doublon avec EquilibrageHiérarchique
    private void normaliserDonnees(List<NoeudEquilibrage> noeuds) {
        MinMaxCalcul<NoeudEquilibrage> minMaxCalcul = new MinMaxCalcul<>();
        minMaxCalcul.calculerMinMax(0, Float.MIN_VALUE, noeuds);

        // on calcule les valeurs min et max
        float[] minValeurs = minMaxCalcul.getMinValeurs();
        float[] maxValeurs = minMaxCalcul.getMaxValeurs();

        for (NoeudEquilibrage noeudEquilibrage : noeuds) {
            noeudEquilibrage.activerMinMaxNormalisation(minValeurs, maxValeurs);
        }
    }

    // méthode spéciale de minPoints on va prendre le % de range
    @Override
    protected boolean seuilMinimumAtteint(List<ObjetIndexable<NoeudEquilibrage>> autresVoisins) {
        float pctRange = 0;
        for (ObjetIndexable<NoeudEquilibrage> point : autresVoisins) {
            pctRange += point.getObjet().getPCombo();
        }

        logger.trace("Pourcentage de range du cluster : " + pctRange);
        logger.trace("Pourcentage requis : " + MIN_PCT_RANGE);

        return pctRange > MIN_PCT_RANGE;
    }

}
