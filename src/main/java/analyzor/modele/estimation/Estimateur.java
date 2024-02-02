package analyzor.modele.estimation;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.bdd.ObjetUnique;
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
import analyzor.modele.exceptions.TacheInterrompue;
import analyzor.modele.parties.*;
import analyzor.modele.bdd.ConnexionBDD;
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
 */
public class Estimateur {
    private static final int PAS_RANGE = 5;
    private final Logger logger = LogManager.getLogger(Estimateur.class);

    private final FormatSolution formatSolution;
    private final ProfilJoueur profilJoueur;
    private final EnregistreurRange enregistreurRange;
    private LinkedHashMap<NoeudAbstrait, List<NoeudAbstrait>> situationsTriees;
    private TourMain.Round round;
    private int compteur;

    public Estimateur(FormatSolution formatSolution, ProfilJoueur profilJoueur) {
        this.formatSolution = formatSolution;
        this.profilJoueur = profilJoueur;
        this.enregistreurRange = new EnregistreurRange(formatSolution, profilJoueur);
    }

    // méthodes appelées par le worker

    /**
     * initialisation de l'estimateur pour un round donné
     * @return le nombre de situations qu'il faudra calculer
     */
    public Integer setRound(TourMain.Round round) {
        logger.info("Calcul de range lancé : " + formatSolution + " (" + round + ") " + " (" + profilJoueur + ")");

        situationsTriees = obtenirLesSituationsTriees(formatSolution, round);

        // si déjà résolu on ne fait rien
        if (round == TourMain.Round.PREFLOP && formatSolution.getPreflopCalcule()) return null;
        GestionnaireFormat.setNombreSituations(formatSolution, situationsTriees.size());

        // sinon on récupère l'endroit où le calcul s'est arrêté
        compteur = formatSolution.getNombreSituationsResolues();
        if (compteur < 0) compteur = 0;
        this.round = round;

        return situationsTriees.size();
    }

    /**
     * calcule la range de la situation suivante
     * @throws NonImplemente round non implémenté
     * @return true, si il y a une situation suivante, false sinon
     */
    public boolean calculerRangeSuivante()
            throws NonImplemente {

        if (situationsTriees == null || situationsTriees.isEmpty() || round == null) {
            throw new RuntimeException("Situation n'a pas été initialisée");
        }

        logger.trace("Compteur vaut : " + compteur);

        // si on a calculé toutes les situations
        if (compteur >= situationsTriees.size()) {
            GestionnaireFormat.roundResolu(formatSolution, round);
            situationsTriees = null;
            compteur = 0;
            return false;
        }

        int compte = 0;
        for (NoeudAbstrait noeudAbstrait : situationsTriees.keySet()) {
            logger.trace("Noeud abstrait : " + noeudAbstrait + ", index : " + compte);
            if (compteur == compte++) {
                try {
                    calculerRangesSituation(noeudAbstrait);
                    compteur++;
                    return true;
                }
                catch (TacheInterrompue tacheInterrompue) {
                    return false;
                }
            }

        }
        throw new RuntimeException("Situation non trouvée : " + compteur);
    }

    public void calculerRangesSituation(NoeudAbstrait noeudAbstrait)
            throws NonImplemente, TacheInterrompue {

        // on supprime les ranges si elles existent
        // car le calcul a été interrompu à cet endroit
        enregistreurRange.supprimerRange(noeudAbstrait.toLong());

        logger.debug("Traitement du noeud : " + noeudAbstrait);
        logger.debug("Actions théoriques possibles " + situationsTriees.get(noeudAbstrait));

        List<NoeudDenombrable> situationsIso = obtenirSituations(noeudAbstrait);
        // on met quand la même la situation comme résolue
        if (situationsIso == null) {
            GestionnaireFormat.situationResolue(formatSolution);
            return;
        }

        for (NoeudDenombrable noeudDenombrable : situationsIso) {
            logger.debug("Traitement d'un noeud dénombrable : " + noeudDenombrable);

            List<ComboDenombrable> combosEquilibres = obtenirCombosDenombrables(noeudDenombrable);
            enregistreurRange.sauvegarderRanges(combosEquilibres, noeudDenombrable);
        }

        GestionnaireFormat.situationResolue(formatSolution);
    }

    private List<ComboDenombrable> obtenirCombosDenombrables(
            NoeudDenombrable noeudDenombrable) throws TacheInterrompue {

        if (profilJoueur.isHero()) {
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

    private List<NoeudDenombrable> obtenirSituations(
            NoeudAbstrait noeudAbstrait) throws NonImplemente, TacheInterrompue {

        Classificateur classificateur = obtenirClassificateur(noeudAbstrait);
        List<Entree> entreesNoeudAbstrait = GestionnaireFormat.getEntrees(formatSolution,
                situationsTriees.get(noeudAbstrait), profilJoueur);
        // 2e rang flop => parfois pas de classificateur donc pas de traitement à faire
        if (classificateur == null) return null;
        logger.debug("Appel au classificateur");
        classificateur.creerSituations(entreesNoeudAbstrait);
        if (!(classificateur.construireCombosDenombrables())) return null;

        List<NoeudDenombrable> situationsIso = classificateur.obtenirSituations();
        if (situationsIso.isEmpty()) {
            logger.warn("Résultats vides renvoyés, on passe au noeud suivant");
            return null;
        }

        return situationsIso;
    }

    // todo : pour suivi valeurs à supprimer?
    private void loggerInfosNoeud(NoeudDenombrable noeudDenombrable) {
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

    private Classificateur obtenirClassificateur(NoeudAbstrait noeudAbstrait)
            throws NonImplemente, TacheInterrompue {
        if (noeudAbstrait == null) return null;
        Classificateur classificateur =
                ClassificateurFactory.creeClassificateur(round, noeudAbstrait.getRang(), formatSolution, profilJoueur);
        if (classificateur == null) return null;

        return classificateur;
    }

    private LinkedHashMap<NoeudAbstrait, List<NoeudAbstrait>> obtenirLesSituationsTriees(
            FormatSolution formatSolution, TourMain.Round round) {
        ArbreAbstrait arbreAbstrait = new ArbreAbstrait(formatSolution);
        return arbreAbstrait.obtenirNoeudsGroupes(round);
    }

}
