package analyzor.modele.denombrement;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.berkeley.EnregistrementEquiteIso;
import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.denombrement.combos.DenombrableIso;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;
import analyzor.modele.poker.evaluation.EquiteFuture;
import analyzor.modele.poker.evaluation.OppositionRange;

import java.util.*;

// outil pour dénombrer les ranges préflop
public class NoeudDenombrableIso extends NoeudDenombrable {
    private final HashMap<ComboIso, ComboDenombrable> tableCombo;
    private static HashMap<ComboIso, EquiteFuture> equitesCalculees;
    private float moyenneEquite;
    private final PriorityQueue<ComboDenombrable> combosTriesParEquite;
    public NoeudDenombrableIso(String nomNoeudAbstrait) {
        super(nomNoeudAbstrait);
        tableCombo = new HashMap<>();
        ConfigCalculatrice configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modePrecision();
        calculatriceEquite = new CalculatriceEquite(configCalculatrice);
        combosTriesParEquite = new PriorityQueue<>(Comparator.comparingDouble(ComboDenombrable::getEquite).reversed());
    }

    /**
     * on garantit que l'ordre de observations et showdowns est le même
     */
    @Override
    public void decompterCombos() {
        int indexAction = 0;
        for (NoeudAction noeudAction : this.getNoeudSansFold()) {
            denombrerCombos(noeudAction, indexAction);
            estimerShowdown(noeudAction, indexAction);
            indexAction++;
        }
    }

    private void denombrerCombos(NoeudAction noeudAction, int indexAction) {
        logger.info("Dénombrement des combos");
        for (Entree entree : entreesCorrespondantes.get(noeudAction)) {
            if (entree.getCombo() == 0) continue;
            ComboReel comboObserve = new ComboReel(entree.getCombo());
            ComboIso equivalentIso = new ComboIso(comboObserve);
            logger.trace("Combo trouvé dans Entree : " + equivalentIso.codeReduit());

            ComboDenombrable comboDenombrable = tableCombo.get(equivalentIso);
            if (comboDenombrable == null)
                throw new RuntimeException("Aucun équivalent dénombrable trouvé pour : " + comboObserve);

            comboDenombrable.incrementerObservation(indexAction);
        }
    }

    private void estimerShowdown(NoeudAction noeudAction, int indexAction) {
        float showdownAction = getShowdown(noeudAction);

        for (ComboDenombrable comboDenombrable : combosDenombrables) {
            float valeurShowdown;
            // si all-in, le % showdown ne dépend pas du combo
            if (noeudAction.getMove() == Move.ALL_IN) {
                valeurShowdown = showdownAction;
            }
            else {
                valeurShowdown = (comboDenombrable.getEquite() / moyenneEquite) * showdownAction;
            }
            if (valeurShowdown > 1) valeurShowdown = 1;
            comboDenombrable.setShowdown(indexAction, valeurShowdown);
        }
    }

    /**
     * appelé par le classificateur
     * doit être appelé AVANT dénombrement/showdown
     */
    public void construireCombosPreflop(OppositionRange oppositionRange) {
        constructionTerminee();
        if (!(oppositionRange.getRangeHero() instanceof RangeIso))
            throw new IllegalArgumentException("La range fournie n'est pas une range iso");

        RangeIso rangeHero = (RangeIso) oppositionRange.getRangeHero();
        List<RangeReelle> rangesVillains = oppositionRange.getRangesVillains();

        if (equitesCalculees == null) calculerEquiteIso();
        float nombreCombosRange = nombreCombosRange(rangeHero);

        for (ComboIso comboIso : rangeHero.getCombos()) {
            //todo est ce qu'on prend les combos nuls??
            if (comboIso.getValeur() == 0) continue;
            // on prend n'importe quel combo réel = même équité
            EquiteFuture equiteFuture = equitesCalculees.get(comboIso);
            moyenneEquite += equiteFuture.getEquite();

            float pCombo = comboIso.getValeur() * comboIso.getNombreCombos() / nombreCombosRange;

            DenombrableIso comboDenombrable = new DenombrableIso(
                    comboIso, pCombo, equiteFuture, this.getNombreActionsSansFold());
            // on les met d'abord dans la PQ pour trier par équité décroissante
            this.combosTriesParEquite.add(comboDenombrable);
            this.tableCombo.put(comboIso, comboDenombrable);
        }

        // on ajoute les combos dans l'ordre de l'équité dans la liste
        while(!combosTriesParEquite.isEmpty()) {
            this.combosDenombrables.add(combosTriesParEquite.poll());
        }

        moyenneEquite /= rangeHero.getCombos().size();
    }

    private float nombreCombosRange(RangeIso rangeHero) {
        float nombreCombos = 0;
        for (ComboIso comboIso : rangeHero.getCombos()) {
            nombreCombos += comboIso.getValeur() * comboIso.getNombreCombos();
        }
        return nombreCombos;
    }

    // todo : beaucoup trop long de calculer les équités au moment de ce code
    // en attendant on calcule les valeurs une seule fois
    // pas bon du tout pour % showdown probablement!!!!
    private void calculerEquiteIso() {
        // todo distinguer 2 et 3 joueurs ??
        logger.debug("Hashmap non trouvé, on calcule les équites des combos iso");
        equitesCalculees = new HashMap<>();
        RangeReelle rangeVillain = new RangeReelle();
        rangeVillain.remplir();
        List<RangeReelle> rangesVillains = new ArrayList<>();
        rangesVillains.add(rangeVillain);

        Board board = new Board();

        for (ComboIso comboIso : GenerateurCombos.combosIso) {
            EquiteFuture equiteFuture = recupererComboBDD(comboIso);
            if (equiteFuture == null) {
                logger.trace("Calcul de l'équité pour : " + comboIso.codeReduit());
                ComboReel randomCombo = comboIso.toCombosReels().get(0);
                equiteFuture = calculatriceEquite.equiteFutureMain(randomCombo, board, rangesVillains);
                enregistrerComboBDD(comboIso, equiteFuture);
            }

            equitesCalculees.put(comboIso, equiteFuture);
        }
    }

    private EquiteFuture recupererComboBDD(ComboIso comboIso) {
        EnregistrementEquiteIso enregistrementEquiteIso = new EnregistrementEquiteIso();
        EquiteFuture equiteFuture = null;
        try {
            equiteFuture = enregistrementEquiteIso.recupererEquite(comboIso);
            if (equiteFuture != null) logger.trace("Equité combo récupéré dans BDD : " + comboIso.codeReduit());
        } catch (Exception e) {
            logger.error("Problème de récupération de l'équité dans BDD", e);
        }
        return equiteFuture;
    }

    private void enregistrerComboBDD(ComboIso comboIso, EquiteFuture equiteFuture) {
        EnregistrementEquiteIso enregistrementEquiteIso = new EnregistrementEquiteIso();
        try {
            enregistrementEquiteIso.enregistrerCombo(comboIso, equiteFuture);
            logger.trace("Equité combo enregistré dans BDD : " + comboIso.codeReduit());
        }
        catch (Exception e) {
            logger.error("Impossible d'enregistrer l'équité dans la base", e);
        }
    }
}
