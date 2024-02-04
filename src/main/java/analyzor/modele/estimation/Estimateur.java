package analyzor.modele.estimation;

import analyzor.controleur.WorkerAffichable;
import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.bdd.ObjetUnique;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * coordonne l'ensemble des étapes du calcul des ranges
 * laisse le soin aux différentes étapes de gérer les accès à la BDD
 */
public class Estimateur extends WorkerAffichable {
    private static final int PAS_RANGE = 5;
    private final Logger logger = LogManager.getLogger(Estimateur.class);
    private final FormatSolution formatSolution;
    private final ProfilJoueur profilJoueur;
    private final EnregistreurRange enregistreurRange;
    private LinkedHashMap<NoeudAbstrait, List<NoeudAbstrait>> situationsTriees;
    private TourMain.Round round;
    private int avancement;
    private boolean interrompu;

    public Estimateur(FormatSolution formatSolution) {
        super("Calcul");
        this.formatSolution = formatSolution;
        this.profilJoueur = ObjetUnique.selectionnerVillain();
        this.enregistreurRange = new EnregistreurRange(formatSolution, profilJoueur);
        avancement = 0;
        interrompu = false;
    }

    @Override
    protected Void executerTache() throws Exception {
        round = TourMain.Round.PREFLOP;
        logger.info("Calcul de range lancé : " + formatSolution + " (" + round + ") " + " (" + profilJoueur + ")");

        situationsTriees = obtenirLesSituationsTriees(formatSolution, round);

        fixerMaximumProgressBar();

        int compte = 0;
        int nSituationsResolues = formatSolution.getNombreSituationsResolues();

        for (NoeudAbstrait noeudAbstrait : situationsTriees.keySet()) {
            if (compte++ >= nSituationsResolues) {
                try {
                    if (this.interrompu) {
                        this.cancel(true);
                        gestionInterruption();
                        break;
                    }
                    logger.trace("Noeud abstrait : " + noeudAbstrait + ", index : " + compte);

                    calculerRangesSituation(noeudAbstrait);
                } catch (Exception e) {
                    gestionInterruption();
                    break;
                }

            }
        }

        return null;
    }

    private void fixerMaximumProgressBar() {
        int nEntreesTotales = 0;
        for (NoeudAbstrait noeudAbstrait : situationsTriees.keySet()) {
            // on met deux situations de plus comme ça on peut incrémenter régulièrement
            nEntreesTotales += situationsTriees.get(noeudAbstrait).size() + 2;
        }

        progressBar.setMaximum(nEntreesTotales);
        this.nombreOperations = nEntreesTotales;
        logger.trace("Maximum fixé : " + nEntreesTotales);
    }

    // méthodes privées des différentes étapes

    private void calculerRangesSituation(NoeudAbstrait noeudAbstrait)
            throws NonImplemente {

        if (this.interrompu) return;

        // on supprime les ranges si elles existent
        // car le calcul a été interrompu à cet endroit
        enregistreurRange.supprimerRange(noeudAbstrait.toLong());

        logger.debug("Traitement du noeud : " + noeudAbstrait);
        logger.debug("Actions théoriques possibles " + situationsTriees.get(noeudAbstrait));

        if (this.interrompu) return;

        List<NoeudDenombrable> situationsIso = obtenirSituations(noeudAbstrait);
        incrementerAvancement(1);
        // on met quand la même la situation comme résolue
        if (situationsIso == null) {
            GestionnaireFormat.situationResolue(formatSolution);
            return;
        }

        for (NoeudDenombrable noeudDenombrable : situationsIso) {
            if (this.interrompu) return;
            incrementerAvancement(1);
            logger.debug("Traitement d'un noeud dénombrable : " + noeudDenombrable);

            List<ComboDenombrable> combosEquilibres = obtenirCombosDenombrables(noeudDenombrable);
            enregistreurRange.sauvegarderRanges(combosEquilibres, noeudDenombrable);

            incrementerAvancement(1);
        }

        GestionnaireFormat.situationResolue(formatSolution);
    }

    private List<ComboDenombrable> obtenirCombosDenombrables(
            NoeudDenombrable noeudDenombrable) {

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
            NoeudAbstrait noeudAbstrait) throws NonImplemente {

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
            throws NonImplemente {
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

    /**
     * surcharge de la méthode cancel du worker
     * indispensable car si on interrompt le processus,
     */
    public void annulerTache() {
        progressBar.setString("Veuillez patienter...");
        interrompu = true;
    }

    @Override
    protected void process(java.util.List<Integer> chunks) {
        int progressValue = chunks.get(chunks.size() - 1);
        progressBar.setValue(progressValue);
        progressBar.setString("Calcul en cours (" + (progressValue / nombreOperations) + "%)");
        logger.trace("Avancement publié : " + progressValue);
    }

    private void incrementerAvancement(int nOperationSupp) {
        // on publie l'avancement régulier, il s'agit juste du nombre d'entrées traités
        avancement += nOperationSupp;
        this.publish(avancement);
    }


}
