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


        GestionnaireFormat.setNombreSituations(formatSolution, situationsTriees.size() + 1);
        int compte = 1;
        int nSituationsResolues = formatSolution.getNombreSituationsResolues();

        fixerMaximumProgressBar(nSituationsResolues);

        for (NoeudAbstrait noeudAbstrait : situationsTriees.keySet()) {

            if (noeudAbstrait.nombreActions() >= 1 && LicenceManager.getInstance().modeDemo()) return null;

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

        progressionNonLineaire.fixerNombreIterations(situationsTriees.size());
        if (LicenceManager.getInstance().modeDemo()) {
            int nIterations = 0;
            for (NoeudAbstrait noeudAbstrait : situationsTriees.keySet()) {
                if (noeudAbstrait.nombreActions() >= 1) {
                    progressionNonLineaire.fixerIterationFinale(nIterations);
                    break;
                }
                nIterations += 1;
            }
        }
        else {
            progressionNonLineaire.fixerIterationFinale(situationsTriees.size());
        }


        progressionNonLineaire.fixerIterationActuelle(nSituationsResolues);

        progressBar.setIndeterminate(true);
        progressBar.setString("Calcul des ranges en cours");
    }



    private void calculerRangesSituation(NoeudAbstrait noeudAbstrait)
            throws NonImplemente, CalculInterrompu {

        if (interrompu) throw new CalculInterrompu();


        enregistreurRange.supprimerRange(noeudAbstrait.toLong());

        logger.info("Traitement du noeud : " + noeudAbstrait);

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

            List<ComboDenombrable> combosEquilibres = obtenirCombosDenombrables(noeudDenombrable);
            enregistreurRange.sauvegarderRanges(combosEquilibres, noeudDenombrable);
            publish(progressionNonLineaire.incrementerIterationGrandAvancement());
        }
    }

    private List<ComboDenombrable> obtenirCombosDenombrables(
            NoeudDenombrable noeudDenombrable) throws CalculInterrompu {


        if (profilJoueur.isHero()) {
            noeudDenombrable.decompterStrategieReelle();
            return noeudDenombrable.getCombosDenombrables();
        }

        noeudDenombrable.decompterCombos();
        List<ComboDenombrable> comboDenombrables = noeudDenombrable.getCombosDenombrables();
        ArbreEquilibrage arbreEquilibrage = new ArbreEquilibrage(comboDenombrables, PAS_RANGE,
                noeudDenombrable.totalEntrees(), noeudDenombrable.getPFold());
        arbreEquilibrage.equilibrer(noeudDenombrable.getPActions());

        return arbreEquilibrage.getCombosEquilibres();
    }

    private List<NoeudDenombrable> obtenirSituations(
            NoeudAbstrait noeudAbstrait) throws NonImplemente, CalculInterrompu {

        Classificateur classificateur = obtenirClassificateur(noeudAbstrait);
        List<Entree> entreesNoeudAbstrait = GestionnaireFormat.getEntrees(formatSolution,
                situationsTriees.get(noeudAbstrait), profilJoueur);

        if (entreesNoeudAbstrait.size() < ECHANTILLON_MINIMUM) return null;


        if (classificateur == null) return null;
        classificateur.creerSituations(entreesNoeudAbstrait);
        if (!(classificateur.construireCombosDenombrables())) return null;

        List<NoeudDenombrable> situationsIso = classificateur.obtenirSituations();
        if (situationsIso.isEmpty()) {
            logger.warn("Résultats vides renvoyés, on passe au noeud suivant");
            return null;
        }

        return situationsIso;
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


    public void annulerTache() {
        progressBar.setString("Arr\u00EAt en cours, patientez...");
        interrompu = true;
    }

    @Override
    protected void process(java.util.List<Integer> chunks) {
        int dernierIncrement = chunks.getLast();


        float pctAvancementRelatif =
                (progressionNonLineaire.getPourcentageAjuste(dernierIncrement)
                        - progressionNonLineaire.getPourcentageInitial()) /
                (progressionNonLineaire.getPctFinal() - progressionNonLineaire.getPourcentageInitial());


        float pctAvancementTotal =
                (progressionNonLineaire.getPourcentageAjuste(dernierIncrement)
                        - progressionNonLineaire.getPourcentageInitial()) /
                        (1 - progressionNonLineaire.getPourcentageInitial());
        pctAvancement = pctAvancementTotal + progressionNonLineaire.getPourcentageInitial();

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
                int minutesRestantes = Math.max((int) ((tempsRestant / (1000 * 60)) % 60), 1);
                tempsRestantAffiche = "(env. " + minutesRestantes + "min restantes)";
            }
        }

        progressBar.setString("Calcul des ranges en cours " +  tempsRestantAffiche);
    }

    public static boolean estInterrompu() {
        return interrompu;
    }



    private static class ProgressionNonLineaire {
        private final static float surchargePremiereIterationGrandeTache = 0.55f;
        private final float valeurAlpha;
        private final float MAX_VALEUR_MAPPAGE;
        private final int RATIO_GRANDE_PETITE_TACHE;
        private int nMaximumPonderee;
        private float pctFinal;
        private float iterationActuelle;
        private float pctInitial;
        private float nIterationsGrandeTache = 1;
        private int iterationActuelleGrandeTache;
        private ProgressionNonLineaire(FormatSolution formatSolution) {
            iterationActuelle = 0;

            Variante.PokerFormat pokerFormat = formatSolution.getPokerFormat();
            if (pokerFormat == Variante.PokerFormat.MTT) {
                valeurAlpha = 10;
                MAX_VALEUR_MAPPAGE = 5;
                RATIO_GRANDE_PETITE_TACHE = 40;
            }
            else if (pokerFormat == Variante.PokerFormat.SPIN) {
                valeurAlpha = 1.4f;
                MAX_VALEUR_MAPPAGE = 3f;
                RATIO_GRANDE_PETITE_TACHE = 30;
            }
            else if (pokerFormat == Variante.PokerFormat.CASH_GAME) {
                valeurAlpha = 10;
                MAX_VALEUR_MAPPAGE = 5;
                RATIO_GRANDE_PETITE_TACHE = 40;
            }
            else {
                valeurAlpha = 10;
                MAX_VALEUR_MAPPAGE = 5f;
                RATIO_GRANDE_PETITE_TACHE = 40;
            }
        }

        public void fixerIterationActuelle(int nSituationsResolues) {
            this.iterationActuelle = nSituationsResolues * (RATIO_GRANDE_PETITE_TACHE + 1);
            this.pctInitial = getPourcentageAjuste(iterationActuelle);
        }

        private void fixerNombreIterations(int nMaximumIterations) {
            this.nMaximumPonderee = nMaximumIterations * (RATIO_GRANDE_PETITE_TACHE + 1);
        }

        public void fixerIterationFinale(int size) {
            float nFinale = size * (RATIO_GRANDE_PETITE_TACHE + 1);
            this.pctFinal = getPourcentageAjuste(nFinale);
        }

        public float getPctFinal() {
            return pctFinal;
        }


        private int incrementerPetitAvancement() {
            iterationActuelle += 1;
            return (int) iterationActuelle;
        }


        private int incrementerGrandAvancement() {
            iterationActuelle += RATIO_GRANDE_PETITE_TACHE;
            return (int) iterationActuelle;
        }

        private int incrementerIterationGrandAvancement() {
            float valeurIncrement =
                    ((float) RATIO_GRANDE_PETITE_TACHE / nIterationsGrandeTache);
            if (iterationActuelleGrandeTache++ == 0) {
                valeurIncrement *= (1 + surchargePremiereIterationGrandeTache);
            }
            iterationActuelle += valeurIncrement;
            return (int) iterationActuelle;
        }

        public void fixerNombreIterationsGrandeTache(int size) {
            nIterationsGrandeTache = size + surchargePremiereIterationGrandeTache;
            iterationActuelleGrandeTache = 0;
        }


        private float getPourcentageAjuste(float iterationsCumulees) {
            float valeurMappee = mapperValeurEntreZeroEtCinq(iterationsCumulees);
            return (float) exponentielleInversee(valeurMappee);
        }


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
