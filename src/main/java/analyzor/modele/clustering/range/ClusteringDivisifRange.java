package analyzor.modele.clustering.range;

import analyzor.modele.clustering.objets.ComboPostClustering;
import analyzor.modele.clustering.objets.ComboPreClustering;
import analyzor.modele.denombrement.CalculEquitePreflop;
import analyzor.modele.denombrement.NoeudDenombrable;
import analyzor.modele.denombrement.combos.DenombrableIso;
import analyzor.modele.equilibrage.leafs.ClusterEquilibrage;
import analyzor.modele.equilibrage.leafs.ComboIsole;
import analyzor.modele.equilibrage.leafs.NoeudEquilibrage;
import analyzor.modele.estimation.CalculInterrompu;
import analyzor.modele.estimation.Estimateur;
import analyzor.modele.poker.ComboIso;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class ClusteringDivisifRange {
    private final OptimiseurHypothese optimiseurHypothese;
    private List<NoeudEquilibrage> noeudsInitiaux;
    private final List<ComboPostClustering> pointsIsoles;
    private final HashMap<ComboPostClustering, List<ComboPostClustering>> pointsAttribues;
    private final List<ClusterEquilibrage> clustersFinaux;

    public ClusteringDivisifRange(int nSituations) {
        optimiseurHypothese = new OptimiseurHypothese(nSituations);
        pointsIsoles = new ArrayList<>();
        pointsAttribues = new HashMap<>();
        clustersFinaux = new ArrayList<>();
    }

    public void ajouterDonnees(List<NoeudEquilibrage> noeuds) throws CalculInterrompu {
        this.noeudsInitiaux = noeuds;

        if (Estimateur.estInterrompu()) throw new CalculInterrompu();

        
        AcpRange acpRange = new AcpRange();
        acpRange.ajouterDonnees(noeuds);
        acpRange.transformer();
        
        List<ComboPreClustering> donneesTransformees = acpRange.getDonnesTransformees();

        
        optimiseurHypothese.creerHypotheses(donneesTransformees);
    }


    public List<ClusterEquilibrage> getResultats() throws CalculInterrompu {
        List<ComboPostClustering> meilleureHypothese = optimiseurHypothese.meilleureHypothese();


        return etendreLesCentres(meilleureHypothese);
    }

    
    private List<ClusterEquilibrage> etendreLesCentres(List<ComboPostClustering> centresGravite) {
        
        for (NoeudEquilibrage noeudEquilibrage : noeudsInitiaux) {
            boolean estCentre = false;
            for (ComboPostClustering centre : centresGravite) {
                if (centre.getNoeudEquilibrage() == noeudEquilibrage) {
                    estCentre = true;
                    break;
                }
            }

            if (!estCentre) {
                ComboPostClustering comboPostClustering = new ComboPostClustering(noeudEquilibrage);
                pointsIsoles.add(comboPostClustering);
            }
        }

        attribuerPointsIsoles(centresGravite);

        return clustersFinaux;
    }

    
    private void attribuerPointsIsoles(List<ComboPostClustering> centresGravites) {
        
        for (ComboPostClustering centreGravite : centresGravites) {
            pointsAttribues.put(centreGravite, new ArrayList<>());
        }

        
        for (ComboPostClustering pointIsole : pointsIsoles) {
            
            trouverCentrePlusProche(pointIsole, centresGravites);
        }

        creerClustersFinaux();
    }


    
    private void trouverCentrePlusProche(ComboPostClustering pointIsole, List<ComboPostClustering> centresGravites) {
        
        float minDistance = Float.MAX_VALUE;

        ComboPostClustering centrePlusProche = null;

        
        for (ComboPostClustering centreGravite : centresGravites) {
            float distance = centreGravite.distanceEquite(pointIsole);
            if (distance < minDistance) {
                minDistance = distance;
                centrePlusProche = centreGravite;
            }
        }

        if (centrePlusProche == null) throw new RuntimeException("Aucun centre plus proche trouvé");

        pointsAttribues.get(centrePlusProche).add(pointIsole);
    }

    
    private void creerClustersFinaux() {
        
        

        List<List<NoeudEquilibrage>> clustersFormes = new ArrayList<>();

        for (ComboPostClustering centreGravite : pointsAttribues.keySet()) {
            List<ComboPostClustering> points = pointsAttribues.get(centreGravite);

            List<NoeudEquilibrage> noeudsCluster = new ArrayList<>();

            noeudsCluster.add(centreGravite.getNoeudEquilibrage());
            for (ComboPostClustering pointCluster : points) {
                noeudsCluster.add(pointCluster.getNoeudEquilibrage());
            }

            clustersFormes.add(noeudsCluster);

        }

        reattribuerPetitesPp(clustersFormes);

        for (List<NoeudEquilibrage> cluster : clustersFormes) {
            ClusterEquilibrage clusterEquilibrage = new ClusterEquilibrage(cluster);
            clustersFinaux.add(clusterEquilibrage);
        }
    }

    
    private void reattribuerPetitesPp(List<List<NoeudEquilibrage>> clustersFormes) {
        
        List<NoeudEquilibrage> clusterAccueil = null;
        outerLoop:
        for (List<NoeudEquilibrage> cluster : clustersFormes) {
            for (NoeudEquilibrage noeudIsole : cluster) {
                ComboIso comboIsole = ((DenombrableIso) ((ComboIsole) noeudIsole).getComboDenombrable()).getCombo();
                if (comboIsole.equals(CalculEquitePreflop.comboReferent)) {
                    clusterAccueil = cluster;
                    break outerLoop;
                }
            }
        }

        if (clusterAccueil == null) throw new RuntimeException("Cluster accueil non trouvé");

        
        List<NoeudEquilibrage> noeudsChangesDeCluster = new ArrayList<>();
        for (List<NoeudEquilibrage> cluster : clustersFormes) {
            Iterator<NoeudEquilibrage> iterateur = cluster.iterator();
            while(iterateur.hasNext()) {
                NoeudEquilibrage noeudIsole = iterateur.next();
                ComboIso comboIsole = ((DenombrableIso) ((ComboIsole) noeudIsole).getComboDenombrable()).getCombo();
                if (CalculEquitePreflop.ppDistanceSpeciale.contains(comboIsole)) {
                    noeudsChangesDeCluster.add(noeudIsole);
                    iterateur.remove();
                }
            }
        }

        clusterAccueil.addAll(noeudsChangesDeCluster);
    }
}
