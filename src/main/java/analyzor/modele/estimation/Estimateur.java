package analyzor.modele.estimation;

import analyzor.modele.arbre.classificateurs.Classificateur;
import analyzor.modele.arbre.classificateurs.ClassificateurFactory;
import analyzor.modele.arbre.noeuds.NoeudDenombrable;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.exceptions.NonImplemente;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Situation;
import analyzor.modele.parties.TourMain;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * coordonne l'ensemble des étapes du calcul des ranges
 * laisse le soin aux différentes étapes de gérer les accès à la BDD
 * crée le worker avec décompte de situations pour la progress bar
 */
public class Estimateur {
    public static void calculerRanges(FormatSolution formatSolution, TourMain.Round round) throws NonImplemente {
        // on demande les situations
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);
        LinkedHashMap<NoeudAbstrait, List<Entree>> toutesLesSituations = arbreAbstrait.obtenirEntrees(round);

        // TODO : on crée un worker qui s'actualise chaque situation résolue
        // TODO reprend le travail là où il s'est arrêté
        for (NoeudAbstrait noeudAbstrait : toutesLesSituations.keySet()) {
            // ne devrait pas arriver
            if (noeudAbstrait == null) continue;
            Classificateur classificateur =
                    ClassificateurFactory.creeClassificateur(round, noeudAbstrait.getRang());

            List<Entree> entreesSituation = toutesLesSituations.get(noeudAbstrait);
            List<NoeudDenombrable> situationsIso = classificateur.obtenirSituations(entreesSituation);
            if (situationsIso.isEmpty()) continue;

        }

        // à la fin on met le round comme calculé
        formatSolution.setCalcule(round);
    }
}
