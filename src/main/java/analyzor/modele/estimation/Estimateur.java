package analyzor.modele.estimation;

import analyzor.modele.arbre.classificateurs.Classificateur;
import analyzor.modele.arbre.classificateurs.ClassificateurFactory;
import analyzor.modele.arbre.noeuds.NoeudDenombrable;
import analyzor.modele.exceptions.NonImplemente;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Situation;
import analyzor.modele.parties.TourMain;

import java.util.List;

/**
 * coordonne l'ensemble des étapes du calcul des ranges
 * laisse le soin aux différentes étapes de gérer les accès à la BDD
 * crée le worker avec décompte de situations pour la progress bar
 */
public class Estimateur {
    public static void calculerRanges(FormatSolution formatSolution, TourMain.Round round) throws NonImplemente {
        // on demande les situations
        GenerateurSituation generateurSituation = new GenerateurSituation(formatSolution);
        List<List<Entree>> toutesLesSituations = generateurSituation.getSituations(round);

        //TODO : on crée un worker qui s'actualise toutes les situations résolues
        // TODO reprend le travail là où il s'est arrêté
        for (List<Entree> entreesTriees : toutesLesSituations) {
            Situation situation = entreesTriees.get(0).getSituation();
            Classificateur classificateur = ClassificateurFactory.CreeClassificateur(situation);

            List<NoeudDenombrable> situationsIso = classificateur.obtenirSituations(entreesTriees);
            if (situationsIso.isEmpty()) continue;

            //worker par situations
        }

        // à la fin on met le round comme calculé
        formatSolution.setCalcule(round);
    }
}
