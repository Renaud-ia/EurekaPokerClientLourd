package analyzor.modele.estimation;

import analyzor.controleur.workers.WorkerAffichable;
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
    protected Void executerTache() {
        round = TourMain.Round.PREFLOP;
        logger.info("Calcul de range lancé : " + formatSolution + " (" + round + ") " + " (" + profilJoueur + ")");

        situationsTriees = obtenirLesSituationsTriees(formatSolution, round);
        GestionnaireFormat.setNombreSituations(formatSolution, situationsTriees.size());

        int compte = 0;
        int nSituationsResolues = formatSolution.getNombreSituationsResolues();
        logger.trace("Index situations résolues " + nSituationsResolues);

        fixerMaximumProgressBar(nSituationsResolues);

        for (NoeudAbstrait noeudAbstrait : situationsTriees.keySet()) {
            logger.trace("Noeud abstrait : " + noeudAbstrait + ", index : " + compte);

            if (compte >= nSituationsResolues) {
                try {
                    if (this.interrompu) {
                        this.cancel(true);
                        gestionInterruption();
                        break;
                    }

                    calculerRangesSituation(noeudAbstrait);
                    GestionnaireFormat.situationResolue(formatSolution, compte);
                }

                catch (Exception e) {
                    logger.error("Estimation interrompue", e);
                    gestionInterruption();
                    break;
                }

            }
            compte++;
        }

        GestionnaireFormat.roundResolu(formatSolution, round);

        return null;
    }

    private void fixerMaximumProgressBar(int nSituationsResolues) {
        int compte = 0;
        // une petite entrée pour incrémenter la barre au début
        int nBoucles = 1;
        for (NoeudAbstrait ignored : situationsTriees.keySet()) {
            if (compte++ > nSituationsResolues) {
                // à chaque noeud, on va avoir deux incréments
                nBoucles += 2;
            }
        }

        progressBar.setMaximum(nBoucles);
        this.nombreOperations = nBoucles;
        logger.trace("Maximum fixé : " + nBoucles);
        this.incrementerAvancement(1);
    }

    // méthodes privées des différentes étapes

    private void calculerRangesSituation(NoeudAbstrait noeudAbstrait)
            throws NonImplemente {

        if (this.interrompu) return;
        // on supprimes les ranges avant de calculer
        // si on interrompt, il y aura des ranges vides mais pas grave car on ne les affichera pas
        enregistreurRange.supprimerRange(noeudAbstrait.toLong());

        logger.debug("Traitement du noeud : " + noeudAbstrait);
        logger.debug("Actions théoriques possibles " + situationsTriees.get(noeudAbstrait));

        if (this.interrompu) return;

        List<NoeudDenombrable> situationsIso = obtenirSituations(noeudAbstrait);
        incrementerAvancement(1);
        if (situationsIso == null) {
            return;
        }

        for (NoeudDenombrable noeudDenombrable : situationsIso) {
            if (this.interrompu) return;
            logger.debug("Traitement d'un noeud dénombrable : " + noeudDenombrable);

            List<ComboDenombrable> combosEquilibres = obtenirCombosDenombrables(noeudDenombrable);
            enregistreurRange.sauvegarderRanges(combosEquilibres, noeudDenombrable);
        }

        incrementerAvancement(1);
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

        return ClassificateurFactory.creeClassificateur(round, noeudAbstrait.getRang(), formatSolution, profilJoueur);
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

        String valeurArrondie = String.valueOf((progressValue) * 100 / nombreOperations);
        progressBar.setString("Calcul en cours (" +  valeurArrondie + "%)");
    }

    private void incrementerAvancement(int nOperationSupp) {
        // on publie l'avancement régulier, il s'agit juste du nombre d'entrées traités
        avancement += nOperationSupp;
        this.publish(avancement);
    }


}
