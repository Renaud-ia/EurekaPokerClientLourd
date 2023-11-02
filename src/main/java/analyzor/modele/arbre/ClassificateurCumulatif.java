package analyzor.modele.arbre;

import analyzor.modele.parties.Entree;
import analyzor.modele.parties.RequetesBDD;
import analyzor.modele.poker.RangeDenombrable;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassificateurCumulatif extends Classificateur{
    //todo

    @Override
    public List<SituationIsoAvecRange> obtenirSituations(List<Entree> entreesSituation) {
        // si aucune situation on retourne une liste vide
        if (!super.situationValide(entreesSituation)) return new ArrayList<>();

        HashMap<Long, SituationIsoAvecRange> situationsDuRang = new HashMap<>();

        // s'il y a des actions de rang n+1, on va les labelliser
        this.labelliserProchainesActions(entreesSituation);

        //dans tous les cas on retourne les situations IsoAvecRange
        for (Entree entree : entreesSituation) {
            //todo est ce que c'est une bonne idée???
            // on a mis situationIso en EAGER donc on peut faire ça!
            Long idSituationIso = entree.getSituationIso().getId();
            if (situationsDuRang.get(idSituationIso) == null) {
                //todo récupérer Range
                RangeDenombrable range = recupererRange(entree);
                situationsDuRang.put(idSituationIso, new SituationIsoAvecRange(range));
            }
            situationsDuRang.get(idSituationIso).ajouterEntree(entree);
        }

        return (List<SituationIsoAvecRange>) situationsDuRang.values();
    }

    private void labelliserProchainesActions(List<Entree> entreesSituation) {
        List<List<Entree>> clustersSRPB = clusteriserSRPB(entreesSituation);

        Session session = RequetesBDD.getSession();
        Transaction transaction = session.beginTransaction();

        for (List<Entree> cluster : clustersSRPB) {
            List<List<Entree>> clustersAction = clusteriserActions(cluster);

            for (List<Entree> clusterFinal : clustersAction) {
                // on labellise les Entrées de rang +1
                // si elles existent
            }
        }

        transaction.commit();
        session.close();
    }


}
