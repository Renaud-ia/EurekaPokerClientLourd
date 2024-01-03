package analyzor.modele.estimation;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.config.ValeursConfig;
import analyzor.modele.denombrement.EnregistreurRange;
import analyzor.modele.denombrement.NoeudDenombrable;
import analyzor.modele.arbre.classificateurs.Classificateur;
import analyzor.modele.arbre.classificateurs.ClassificateurFactory;
import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.equilibrage.ArbreEquilibrage;
import analyzor.modele.estimation.arbretheorique.ArbreAbstrait;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.exceptions.NonImplemente;
import analyzor.modele.parties.*;
import analyzor.modele.utils.RequetesBDD;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * coordonne l'ensemble des étapes du calcul des ranges
 * laisse le soin aux différentes étapes de gérer les accès à la BDD
 * crée le worker avec décompte de situations pour la progress bar
 * TODO : on crée un worker qui s'actualise chaque situation résolue
 * TODO : on reprend le travail là où il s'est arrêté
 */
public class Estimateur {
    private static final int PAS_RANGE = 5;
    private static final Logger logger = LogManager.getLogger(Estimateur.class);

    /**
     * procédure qui ne prend pas en compte les profils
     * on calcule les ranges Villain et hero
     * on considère le format Solution calculé
     */
    public static void calculSansProfil(FormatSolution formatSolution, TourMain.Round round) throws NonImplemente {
        ProfilJoueur profilVillain = new ProfilJoueur(ValeursConfig.nomProfilVillain);
        ProfilJoueur profilHero = new ProfilJoueur(ValeursConfig.nomProfilHero);

        calculerRanges(formatSolution, round, profilVillain);
        calculerRanges(formatSolution, round, profilHero);

        formatSolution.setCalcule(round);
    }

    public static void calculerRanges(FormatSolution formatSolution, TourMain.Round round, ProfilJoueur profilJoueur)
            throws NonImplemente {
        logger.info("Calcul de range lancé : " + formatSolution + " (" + round + ") " + " (" + profilJoueur + ")");
        EnregistreurRange enregistreurRange = new EnregistreurRange(formatSolution, profilJoueur);
        // on demande les situations
        LinkedHashMap<NoeudAbstrait, List<NoeudAbstrait>> situationsTriees =
                obtenirLesSituationsTriees(formatSolution, round);

        int compte = 0;
        for (NoeudAbstrait noeudAbstrait : situationsTriees.keySet()) {
            // pour test
            // todo limiter la profondeur à 1 en version démo
            //if (compte++ == 4) break;
            // on vérifie qu'on a pas déjà calculé la range
            if (enregistreurRange.rangeExistante(noeudAbstrait.toLong())) {
                logger.debug("Range déjà calculée, on passe");
                continue;
            }

            logger.debug("Traitement du noeud : " + noeudAbstrait);
            List<NoeudDenombrable> situationsIso =
                    obtenirSituations(formatSolution, noeudAbstrait, round, situationsTriees, profilJoueur);
            if (situationsIso == null) continue;

            for (NoeudDenombrable noeudDenombrable : situationsIso) {
                logger.debug("Traitement d'un noeud dénombrable : " + noeudDenombrable);

                List<ComboDenombrable> combosEquilibres = obtenirCombosDenombrables(noeudDenombrable, profilJoueur);
                enregistreurRange.sauvegarderRanges(combosEquilibres, noeudDenombrable);
            }
        }

    }

    private static List<ComboDenombrable> obtenirCombosDenombrables(
            NoeudDenombrable noeudDenombrable, ProfilJoueur profilJoueur) {

        if (Objects.equals(profilJoueur.getNom(), ValeursConfig.nomProfilHero)) {
            noeudDenombrable.decompterStrategieReelle();
            return noeudDenombrable.getCombosDenombrables();
        }

        logger.debug("Décomptage des combos");
        noeudDenombrable.decompterCombos();
        List<ComboDenombrable> comboDenombrables = noeudDenombrable.getCombosDenombrables();
        ArbreEquilibrage arbreEquilibrage = new ArbreEquilibrage(comboDenombrables, PAS_RANGE,
                noeudDenombrable.totalEntrees(), noeudDenombrable.getPFold());
        loggerInfosNoeud(noeudDenombrable);
        logger.debug("Equilibrage");
        arbreEquilibrage.equilibrer(noeudDenombrable.getPActions());

        return arbreEquilibrage.getCombosEquilibres();
    }

    private static List<NoeudDenombrable> obtenirSituations(
            FormatSolution formatSolution, NoeudAbstrait noeudAbstrait,
            TourMain.Round round,
            LinkedHashMap<NoeudAbstrait, List<NoeudAbstrait>> situationsTriees,
            ProfilJoueur profilJoueur) throws NonImplemente {

        Classificateur classificateur = obtenirClassificateur(noeudAbstrait, formatSolution, round);
        List<Entree> entreesNoeudAbstrait = GestionnaireFormat.getEntrees(formatSolution,
                situationsTriees.get(noeudAbstrait), profilJoueur);
        // 2e rang flop => parfois pas de classificateur donc pas de traitement à faire
        if (classificateur == null) return null;
        logger.debug("Appel au classificateur");
        classificateur.creerSituations(entreesNoeudAbstrait);
        classificateur.construireCombosDenombrables();

        List<NoeudDenombrable> situationsIso = classificateur.obtenirSituations();
        if (situationsIso.isEmpty()) {
            logger.warn("Résultats vides renvoyés, on passe au noeud suivant");
            return null;
        }

        return situationsIso;
    }

    // todo : pour suivi valeurs à supprimer?
    private static void loggerInfosNoeud(NoeudDenombrable noeudDenombrable) {
        logger.trace("NOMBRE SITUATIONS : " + noeudDenombrable.totalEntrees());
        StringBuilder actionsString = new StringBuilder();
        actionsString.append("ACTIONS DU NOEUD : ");
        int index = 0;
        float[] pActions = noeudDenombrable.getPActions();
        for (NoeudAction noeudAction : noeudDenombrable.getNoeudSansFold()) {
            actionsString.append(noeudAction.getMove()).append(" ").append(noeudAction.getBetSize());
            actionsString.append("[").append(pActions[index] * 100).append("%]");
            actionsString.append(", ");
            index++;
        }
        logger.trace(actionsString);
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

    public static LinkedHashMap<NoeudAbstrait, List<NoeudAbstrait>> obtenirLesSituationsTriees(
            FormatSolution formatSolution, TourMain.Round round) {
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);
        return arbreAbstrait.obtenirNoeudsGroupes(round);
    }

    // todo pour test à supprilmer
    public static void main(String[] args) {
        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<FormatSolution> cq = criteriaBuilder.createQuery(FormatSolution.class);
        Root<FormatSolution> rootEntry = cq.from(FormatSolution.class);
        cq.select(rootEntry);

        Variante.PokerFormat pokerFormat = Variante.PokerFormat.SPIN;
        FormatSolution formatSolution =
                new FormatSolution(pokerFormat, false, false, 3, 0, 100);
        session.merge(formatSolution);

        RequetesBDD.fermerSession();

        try {
            Estimateur.calculSansProfil(formatSolution, TourMain.Round.PREFLOP);
        }
        catch (NonImplemente ignored) { }
    }
}
