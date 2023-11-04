package analyzor.modele.arbre.classificateurs;

import analyzor.modele.arbre.classificateurs.Classificateur;
import analyzor.modele.arbre.noeuds.NoeudDenombrable;
import analyzor.modele.parties.Entree;

import java.util.ArrayList;
import java.util.List;

public class ClassificateurSubset extends Classificateur {
    @Override
    public List<NoeudDenombrable> obtenirSituations(List<Entree> entreesSituation) {
        if (!super.situationValide(entreesSituation)) return new ArrayList<>();
        List<NoeudDenombrable> situationsDuRang = new ArrayList<>();
        //todo

        // SI ON A DES SITUATIONS DE RANG2, on a déjà les SituationIso

        List<List<Entree>> clustersSubsets = clusteriserLeafEtSubset(entreesSituation);

        for(List<Entree> cluster : clustersSubsets) {
            List<List<Entree>> clustersActions = clusteriserActions(cluster);

            for (List<Entree> clusterFinal : clustersActions) {
                //on attribue ISO CODE, on le garde pour le retourner + la range
                // on attribue des ISO code à la situation RANG+1 si Subset 2e rank
            }
        }
        return situationsDuRang;
    }

    private List<List<Entree>> clusteriserLeafEtSubset(List<Entree> entreesSituation) {
        //todo
        return new ArrayList<>(new ArrayList<>());
    }
}
