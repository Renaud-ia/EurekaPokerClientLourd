package analyzor.modele.estimation;

import analyzor.modele.arbre.NoeudDenombrable;
import analyzor.modele.arbre.classificateurs.Classificateur;
import analyzor.modele.arbre.classificateurs.ClassificateurFactory;
import analyzor.modele.denombrement.CompteurFactory;
import analyzor.modele.denombrement.CompteurIso;
import analyzor.modele.denombrement.CompteurRange;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.exceptions.NonImplemente;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.parties.RequetesBDD;
import analyzor.modele.parties.TourMain;
import analyzor.modele.showdown.EstimateurShowdown;
import analyzor.modele.showdown.ShowdownFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * coordonne l'ensemble des étapes du calcul des ranges
 * laisse le soin aux différentes étapes de gérer les accès à la BDD
 * crée le worker avec décompte de situations pour la progress bar
 * TODO : on crée un worker qui s'actualise chaque situation résolue
 * TODO : on reprend le travail là où il s'est arrêté
 */
public class Estimateur {
    public static void calculerRanges(FormatSolution formatSolution, TourMain.Round round, ProfilJoueur profilJoueur)
            throws NonImplemente {
        // on demande les situations
        LinkedHashMap<NoeudAbstrait, List<Entree>> situationsTriees =
                obtenirLesSituationsTriees(formatSolution, round, profilJoueur);


        int compte = 0;
        for (NoeudAbstrait noeudAbstrait : situationsTriees.keySet()) {
            // pour test
            if (compte++ == 2) break;

            // on demande au classificateur de créer les noeuds denombrables
            List<NoeudDenombrable> situationsIso = creerLesNoeuds(noeudAbstrait, formatSolution,
                    round, situationsTriees.get(noeudAbstrait));
            if (situationsIso.isEmpty()) continue;

            for (NoeudDenombrable noeudDenombrable : situationsIso) {
                CompteurRange compteurRange = new CompteurRange();
                compteurRange.remplirCombos(noeudDenombrable);

            }

        }

        // à la fin on met le round comme calculé
        formatSolution.setCalcule(round);
    }

    public static LinkedHashMap<NoeudAbstrait, List<Entree>> obtenirLesSituationsTriees(
            FormatSolution formatSolution, TourMain.Round round, ProfilJoueur profilJoueur) {
        List<Entree> toutesLesSituations = GestionnaireFormat.getEntrees(formatSolution, round, profilJoueur);
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);
        return arbreAbstrait.trierEntrees(toutesLesSituations);
    }

    public static List<NoeudDenombrable> creerLesNoeuds(NoeudAbstrait noeudAbstrait,
                                                        FormatSolution formatSolution, TourMain.Round round,
                                                        List<Entree> entreesSituation) throws NonImplemente {
        // ne devrait pas arriver
        if (noeudAbstrait == null) return new ArrayList<>();
        Classificateur classificateur =
                ClassificateurFactory.creeClassificateur(round, noeudAbstrait.getRang(), formatSolution);
        if (classificateur == null) return new ArrayList<>();

        // on récupère les entrées dans la HashMap et on les transmet au classificateur
        return classificateur.obtenirSituations(entreesSituation);
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
            Estimateur.calculerRanges(formatSolution, TourMain.Round.PREFLOP, null);
        }
        catch (NonImplemente nonImplemente) { }
    }
}
