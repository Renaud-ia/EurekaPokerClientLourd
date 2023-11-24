package analyzor.modele.estimation;

import analyzor.modele.denombrement.NoeudDenombrable;
import analyzor.modele.arbre.classificateurs.Classificateur;
import analyzor.modele.arbre.classificateurs.ClassificateurFactory;
import analyzor.modele.denombrement.elements.ComboDenombrable;
import analyzor.modele.equilibrage.Equilibrateur;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.exceptions.NonImplemente;
import analyzor.modele.parties.*;
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
            Classificateur classificateur = obtenirClassificateur(noeudAbstrait, formatSolution, round);
            // 2e rang flop => parfois pas de classificateur donc pas de traitement à faire
            if (classificateur == null) continue;
            classificateur.creerSituations(situationsTriees.get(noeudAbstrait));
            classificateur.construireCombosDenombrables();

            List<NoeudDenombrable> situationsIso = classificateur.obtenirSituations();
            if (situationsIso.isEmpty()) continue;

            for (NoeudDenombrable noeudDenombrable : situationsIso) {
                noeudDenombrable.decompterCombos();
                List<ComboDenombrable> comboDenombrables = noeudDenombrable.getCombosDenombrables();
                Equilibrateur equilibrateur = new Equilibrateur();
                equilibrateur.equilibrer(comboDenombrables);
            }

        }

        // à la fin on met le round comme calculé
        formatSolution.setCalcule(round);
    }

    private static Classificateur obtenirClassificateur(NoeudAbstrait noeudAbstrait,
                                                        FormatSolution formatSolution, TourMain.Round round)
            throws NonImplemente {
        if (noeudAbstrait == null) return null;
        Classificateur classificateur =
                ClassificateurFactory.creeClassificateur(round, noeudAbstrait.getRang(), formatSolution);
        if (classificateur == null) return null;

        return classificateur;
    }

    public static LinkedHashMap<NoeudAbstrait, List<Entree>> obtenirLesSituationsTriees(
            FormatSolution formatSolution, TourMain.Round round, ProfilJoueur profilJoueur) {
        List<Entree> toutesLesSituations = GestionnaireFormat.getEntrees(formatSolution, round, profilJoueur);
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);
        return arbreAbstrait.trierEntrees(toutesLesSituations);
    }

    public static void main(String[] args) {
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<FormatSolution> cq = criteriaBuilder.createQuery(FormatSolution.class);
        Root<FormatSolution> rootEntry = cq.from(FormatSolution.class);
        cq.select(rootEntry);

        Variante.PokerFormat pokerFormat = Variante.PokerFormat.SPIN;
        FormatSolution formatSolution = new FormatSolution(pokerFormat, false, false, 3, 0, 100);

        RequetesBDD.fermerSession();

        try {
            Estimateur.calculerRanges(formatSolution, TourMain.Round.PREFLOP, null);
        }
        catch (NonImplemente ignored) { }
    }
}
