package analyzor.modele.estimation;

import analyzor.modele.arbre.Classificateur;
import analyzor.modele.arbre.ClassificateurFactory;
import analyzor.modele.exceptions.NonImplemente;
import analyzor.modele.parties.Situation;
import analyzor.modele.parties.SituationIso;
import analyzor.modele.parties.TourMain;

import java.util.List;

/**
 * coordonne l'ensemble des étapes du calcul des ranges
 */
public class Estimateur {
    public static void calculerRanges(FormatSolution formatSolution, TourMain.Round round) throws NonImplemente {
        GestionnaireFormat.ajouterFormat(formatSolution);

        // on demande les situations
        List<Situation> toutesLesSituations = GenerateurSituation.getSituations(round);

        //TODO : on crée un worker qui s'actualise toutes les situations résolues
        for (Situation situation : toutesLesSituations) {
            Classificateur classificateur = ClassificateurFactory.CreeClassificateur(situation);
            List<SituationIso> situationsIso = classificateur.obtenirSituations(situation);
        }

        //worker par situations


        // à la fin on met le round comme calculé
        formatSolution.setCalcule(round);
    }
}
