package analyzor.modele.estimation;

import analyzor.modele.arbre.NoeudDenombrable;
import analyzor.modele.arbre.classificateurs.Classificateur;
import analyzor.modele.arbre.classificateurs.ClassificateurFactory;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.exceptions.NonImplemente;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.RequetesBDD;
import analyzor.modele.parties.TourMain;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;

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
        List<Entree> toutesLesSituations = GestionnaireFormat.getEntrees(formatSolution, round);
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);
        LinkedHashMap<NoeudAbstrait, List<Entree>> situationsTriees = arbreAbstrait.trierEntrees(toutesLesSituations);

        // TODO : on crée un worker qui s'actualise chaque situation résolue
        // TODO reprend le travail là où il s'est arrêté
        int compte = 0;
        for (NoeudAbstrait noeudAbstrait : situationsTriees.keySet()) {
            // pour test
            if (compte++ == 1) break;
            // ne devrait pas arriver
            if (noeudAbstrait == null) continue;
            Classificateur classificateur =
                    ClassificateurFactory.creeClassificateur(round, noeudAbstrait.getRang(), formatSolution);
            if (classificateur == null) continue;

            // on récupère les entrées dans la HashMap et on les transmet au classificateur
            List<Entree> entreesSituation = situationsTriees.get(noeudAbstrait);
            List<NoeudDenombrable> situationsIso =
                    classificateur.obtenirSituations(entreesSituation);
            if (situationsIso.isEmpty()) continue;

            for (NoeudDenombrable noeudDenombrable : situationsIso) {
                //EstimateurShowdown estimateurShowdown =
                        //ShowdownFactory.creeEstimateur(noeudDenombrable.getComboDenombrable());

                //CompteurRange compteurRange = CompteurFactory.creeCompteur(noeudDenombrable.getComboDenombrable());

            }

        }

        // à la fin on met le round comme calculé
        formatSolution.setCalcule(round);
    }

    public static void main(String[] args) {
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<FormatSolution> cq = criteriaBuilder.createQuery(FormatSolution.class);
        Root<FormatSolution> rootEntry = cq.from(FormatSolution.class);
        cq.select(rootEntry);

        FormatSolution formatSolution = session.createQuery(cq).getResultList().get(0);

        RequetesBDD.fermerSession();

        try {
            Estimateur.calculerRanges(formatSolution, TourMain.Round.PREFLOP);
        }
        catch (NonImplemente nonImplemente) { }
    }
}
