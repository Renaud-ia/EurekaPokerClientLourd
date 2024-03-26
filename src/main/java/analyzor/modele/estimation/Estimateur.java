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
import analyzor.modele.licence.LicenceManager;
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
    private final static int ECHANTILLON_MINIMUM = 200;
    private static final int PAS_RANGE = 1;
    private final Logger logger = LogManager.getLogger(Estimateur.class);
    private final FormatSolution formatSolution;
    private final ProfilJoueur profilJoueur;
    private final EnregistreurRange enregistreurRange;
    private LinkedHashMap<NoeudAbstrait, List<NoeudAbstrait>> situationsTriees;
    private TourMain.Round round;
    private static boolean interrompu;
    private final ProgressionNonLineaire progressionNonLineaire;
    private final long heureLancement;
    private float pctAvancement;

    public Estimateur(FormatSolution formatSolution) {
        super("Calcul");
        this.formatSolution = formatSolution;
        this.profilJoueur = ObjetUnique.selectionnerVillain();
        this.enregistreurRange = new EnregistreurRange(formatSolution, profilJoueur);
        interrompu = false;

        progressionNonLineaire = new ProgressionNonLineaire(formatSolution);
        heureLancement = System.currentTimeMillis();
        pctAvancement = 0;
    }

    @Override
    protected Void executerTache() {
        round = TourMain.Round.PREFLOP;
        logger.info("Calcul de range lancé : " + formatSolution + " (" + round + ") " + " (" + profilJoueur + ")");

        situationsTriees = obtenirLesSituationsTriees(formatSolution, round);

        // il faut commencer à 1 pour distinguer non résolu et 1 situation résolue en démo
        GestionnaireFormat.setNombreSituations(formatSolution, situationsTriees.size() + 1);
        int compte = 1;
        int nSituationsResolues = formatSolution.getNombreSituationsResolues();
        logger.trace("Index situations résolues " + nSituationsResolues);

        fixerMaximumProgressBar(nSituationsResolues);

        for (NoeudAbstrait noeudAbstrait : situationsTriees.keySet()) {
            // limitation du calcul en mode démo
            if (noeudAbstrait.nombreActions() >= 1 && LicenceManager.getInstance().modeDemo()) return null;
            logger.trace("Noeud abstrait : " + noeudAbstrait + ", index : " + compte);

            if (compte <= nSituationsResolues) {
                compte++;
                continue;
            }
            
            try {
                calculerRangesSituation(noeudAbstrait);
                GestionnaireFormat.situationResolue(formatSolution, compte, pctAvancement);
            }

            catch (CalculInterrompu interrompu) {
                this.cancel(true);
                gestionInterruption();
                return null;
            }

            catch (Exception e) {
                // todo PRODUCTION log à encrypter
                logger.fatal("Estimation interrompue", e);
                gestionInterruption();
                return null;
            }

            compte++;
        }

        GestionnaireFormat.roundResolu(formatSolution, round);

        return null;
    }

    private void fixerMaximumProgressBar(int nSituationsResolues) {
        // une petite entrée pour incrémenter la barre au début
        int nIterations = 0;
        for (NoeudAbstrait noeudAbstrait : situationsTriees.keySet()) {
            if (noeudAbstrait.nombreActions() >= 1 && LicenceManager.getInstance().modeDemo()) break;
            // à chaque noeud, on va avoir deux incréments
            nIterations += 1;
        }

        progressionNonLineaire.fixerNombreIterations(nIterations);
        progressionNonLineaire.fixerIterationActuelle(nSituationsResolues);
        logger.debug("Nombre d'itérations à faire : " + nIterations);

        progressBar.setIndeterminate(true);
        progressBar.setString("Calcul en cours");
    }

    // méthodes privées des différentes étapes

    private void calculerRangesSituation(NoeudAbstrait noeudAbstrait)
            throws NonImplemente, CalculInterrompu {

        if (interrompu) throw new CalculInterrompu();
        // on supprimes les ranges avant de calculer
        // si on interrompt, il y aura des ranges vides mais pas grave car on ne les affichera pas
        enregistreurRange.supprimerRange(noeudAbstrait.toLong());

        logger.debug("Traitement du noeud : " + noeudAbstrait);
        logger.debug("Actions théoriques possibles " + situationsTriees.get(noeudAbstrait));

        if (interrompu) throw new CalculInterrompu();

        List<NoeudDenombrable> situationsIso = obtenirSituations(noeudAbstrait);
        publish(progressionNonLineaire.incrementerPetitAvancement());

        if (situationsIso == null) {
            publish(progressionNonLineaire.incrementerGrandAvancement());
            return;
        }

        progressionNonLineaire.fixerNombreIterationsGrandeTache(situationsIso.size());
        for (NoeudDenombrable noeudDenombrable : situationsIso) {
            if (interrompu) throw new CalculInterrompu();
            logger.debug("Traitement d'un noeud dénombrable : " + noeudDenombrable);

            List<ComboDenombrable> combosEquilibres = obtenirCombosDenombrables(noeudDenombrable);
            enregistreurRange.sauvegarderRanges(combosEquilibres, noeudDenombrable);
            publish(progressionNonLineaire.incrementerIterationGrandAvancement());
        }

        publish(progressionNonLineaire.incrementerGrandAvancement());
    }

    private List<ComboDenombrable> obtenirCombosDenombrables(
            NoeudDenombrable noeudDenombrable) throws CalculInterrompu {


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
            NoeudAbstrait noeudAbstrait) throws NonImplemente, CalculInterrompu {

        Classificateur classificateur = obtenirClassificateur(noeudAbstrait);
        List<Entree> entreesNoeudAbstrait = GestionnaireFormat.getEntrees(formatSolution,
                situationsTriees.get(noeudAbstrait), profilJoueur);

        if (entreesNoeudAbstrait.size() < ECHANTILLON_MINIMUM) return null;

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

    // todo PRODUCTION : pour suivi valeurs à supprimer
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
        progressBar.setString("Arr\u00EAt en cours, patientez...");
        interrompu = true;
    }

    @Override
    protected void process(java.util.List<Integer> chunks) {
        int dernierIncrement = chunks.getLast();

        // on tient compte du fait que la tâche commence pas forcément à zéro
        float pctAvancementRelatif =
                (progressionNonLineaire.getPourcentageAjuste(dernierIncrement)
                        - progressionNonLineaire.getPourcentageInitial()) /
                (1 - progressionNonLineaire.getPourcentageInitial());

        pctAvancement = pctAvancementRelatif + progressionNonLineaire.getPourcentageInitial();

        if (pctAvancementRelatif <= 0) {
            return;
        }

        String tempsRestantAffiche = "";
        if (!chunks.isEmpty()) {
            long heureActuelle = System.currentTimeMillis();
            long tempsEcoule = heureActuelle - heureLancement;
            long tempsRestant = (long) (tempsEcoule / pctAvancementRelatif) - tempsEcoule;

            int heuresRestantes = Math.round((float) tempsRestant / (1000 * 60 * 60));
            if (heuresRestantes > 0) {
                tempsRestantAffiche = "(env." + heuresRestantes + "h restantes)";
            }
            else {
                int minutesRestantes = (int) ((tempsRestant / (1000 * 60)) % 60);
                tempsRestantAffiche = "(env. " + minutesRestantes + "min restantes)";
            }

            logger.debug("Pourcentage de la tâche effectuée : " + pctAvancementRelatif);
            logger.debug("Temps restant estimé : " + tempsRestantAffiche);
        }

        progressBar.setString("Calcul en cours " +  tempsRestantAffiche);
    }

    public static boolean estInterrompu() {
        return interrompu;
    }


    /**
     * classe privée qui permet de visualiser un temps d'avancement non linéaire
     */
    private static class ProgressionNonLineaire {
        // plus alpha est élevé plus les premières tâches auront du poids
        private final float valeurAlpha;
        private final float MAX_VALEUR_MAPPAGE;
        private final int RATIO_GRANDE_PETITE_TACHE;
        private int nMaximumPonderee;
        private float iterationActuelle;
        private float pctInitial;
        private int nIterationsGrandeTache = 1;
        private ProgressionNonLineaire(FormatSolution formatSolution) {
            iterationActuelle = 0;

            Variante.PokerFormat pokerFormat = formatSolution.getPokerFormat();
            if (pokerFormat == Variante.PokerFormat.MTT) {
                valeurAlpha = 2.2f;
                MAX_VALEUR_MAPPAGE = 7;
                RATIO_GRANDE_PETITE_TACHE = 7;
            }
            else if (pokerFormat == Variante.PokerFormat.SPIN) {
                valeurAlpha = 0.9f;
                MAX_VALEUR_MAPPAGE = 1.8f;
                RATIO_GRANDE_PETITE_TACHE = 8;
            }
            else if (pokerFormat == Variante.PokerFormat.CASH_GAME) {
                valeurAlpha = 2.2f;
                MAX_VALEUR_MAPPAGE = 7;
                RATIO_GRANDE_PETITE_TACHE = 7;
            }
            else {
                valeurAlpha = 2.2f;
                MAX_VALEUR_MAPPAGE = 7;
                RATIO_GRANDE_PETITE_TACHE = 7;
            }
        }

        public void fixerIterationActuelle(int nSituationsResolues) {
            this.iterationActuelle = nSituationsResolues * (RATIO_GRANDE_PETITE_TACHE + 1);
            this.pctInitial = getPourcentageAjuste(iterationActuelle);
        }

        private void fixerNombreIterations(int nMaximumIterations) {
            this.nMaximumPonderee = nMaximumIterations * (RATIO_GRANDE_PETITE_TACHE + 1);
        }

        /**
         * publier un petit incrément = tâche légère
         * @return la valeur cumulée de l'avancement
         */
        private int incrementerPetitAvancement() {
            iterationActuelle += 1;
            return (int) iterationActuelle;
        }

        /**
         * publier un gros incrément = tâche lourde
         * @return la valeur cumulée de l'avancement
         */
        private int incrementerGrandAvancement() {
            iterationActuelle += RATIO_GRANDE_PETITE_TACHE;
            return (int) iterationActuelle;
        }

        private int incrementerIterationGrandAvancement() {
            iterationActuelle += (float) RATIO_GRANDE_PETITE_TACHE / nIterationsGrandeTache;
            return (int) iterationActuelle;
        }

        public void fixerNombreIterationsGrandeTache(int size) {
            nIterationsGrandeTache = 1;
        }

        /**
         * @return valeur totale de l'avancement mappé
         */
        private float getPourcentageAjuste(float iterationsCumulees) {
            float valeurMappee = mapperValeurEntreZeroEtCinq(iterationsCumulees);
            return (float) exponentielleInversee(valeurMappee);
        }

        /**
         * on mappe la valeur entre 0 et 5 car exp inverse de 0 vaut 1 et exp inverse de 5 vaut presque 0
         */
        private float mapperValeurEntreZeroEtCinq(float valeurIteration) {
            return (valeurIteration / nMaximumPonderee) * MAX_VALEUR_MAPPAGE;
        }

        private double exponentielleInversee(float x) {
            return (1 - Math.exp(- valeurAlpha * x));
        }

        public float getPourcentageInitial() {
            return pctInitial;
        }
    }
}
