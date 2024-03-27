package analyzor.modele.denombrement;

import analyzor.modele.arbre.noeuds.NoeudAction;
import analyzor.modele.denombrement.combos.ComboDenombrable;
import analyzor.modele.denombrement.combos.DenombrableIso;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.EquiteFuture;
import analyzor.modele.poker.evaluation.OppositionRange;

import java.util.*;

// outil pour dénombrer les ranges préflop
// todo refactoriser, réécrire la recherche de combo car on ne vuet pas de bug si combo mal enregistré
public class NoeudDenombrableIso extends NoeudDenombrable {
    private final HashMap<ComboIso, ComboDenombrable> tableCombo;
    private static HashMap<ComboIso, EquiteFuture> equitesCalculees;
    private float moyenneEquite;
    private final PriorityQueue<ComboDenombrable> combosTriesParEquite;
    public NoeudDenombrableIso(NoeudAbstrait noeudAbstrait) {
        super(noeudAbstrait.stringReduite());
        tableCombo = new HashMap<>();
        CalculEquitePreflop.getInstance().setNoeudAbstrait(noeudAbstrait);
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
        for (Entree entree : entreesCorrespondantes.get(noeudAction)) {
            if (entree.getCombo() == 0) continue;
            ComboReel comboObserve = new ComboReel(entree.getCombo());
            ComboIso equivalentIso = new ComboIso(comboObserve);
            ComboDenombrable comboDenombrable = tableCombo.get(equivalentIso);
            // cas où combo non présent dans Range
            if (comboDenombrable == null) continue;
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
        if (this.getNombreActionsSansFold() < 1) throw new RuntimeException("Moins de 1 actions dans la situation");

        constructionTerminee();
        if (!(oppositionRange.getRangeHero() instanceof RangeIso))
            throw new IllegalArgumentException("La range fournie n'est pas une range iso");

        RangeIso rangeHero;
        rangeHero = (RangeIso) oppositionRange.getRangeHero();

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

    private void calculerEquiteIso() {
        equitesCalculees = new HashMap<>();

        for (ComboIso comboIso : GenerateurCombos.combosIso) {
            EquiteFuture equiteFuture = CalculEquitePreflop.getInstance().getEquite(comboIso);
            equitesCalculees.put(comboIso, equiteFuture);
        }
    }

    // utilisé pour la range de hero, on va juste observer la stratégie sans équilibrage
    // on a besoin de eager sur tourMain et mainEnregistree
    public void decompterStrategieReelle() {
        // todo on pourrait optimiser en loopant une seule fois sur chaque situation
        for (ComboDenombrable combo : combosDenombrables) {
            if (!(combo instanceof DenombrableIso)) throw new RuntimeException("Ce n'est pas un combo iso");
            ComboIso comboIso = ((DenombrableIso) combo).getCombo();

            int[] decompteReel = new int[getNombreActions()];
            for (int i = 0; i < decompteReel.length - 1; i++) {
                NoeudAction noeudAction = getNoeudsActions()[i];
                List<Entree> entreesAction = getEntrees(noeudAction);
                decompteReel[i] = nombreObserves(entreesAction, comboIso) ;
            }

            NoeudAction noeudFold = getNoeudFold();
            List<Entree> entreesFold = getEntrees(noeudFold);

            // attention des fois on ne peut pas fold
            if (entreesFold != null) {
                decompteReel[decompteReel.length - 1]
                        = nombreObserves(entreesFold, comboIso);
            }

            float[] strategieReelle = new float[decompteReel.length];
            for (int i = 0; i < decompteReel.length; i++) {
                if (Arrays.stream(decompteReel).sum() == 0) {
                    strategieReelle[i] = 0;
                }
                else {
                    strategieReelle[i] = (float) decompteReel[i] / Arrays.stream(decompteReel).sum();
                }
            }

            combo.setStrategie(strategieReelle);
        }
    }

    /**
     * compte le % de combos joués
     */
    private int nombreObserves(List<Entree> entrees, ComboIso comboIso) {
        int nActions = 0;
        for (Entree entree : entrees) {
            try {
                int comboIntHero = entree.getTourMain().getMain().getComboHero();
                if (comboIntHero == 0) {
                    continue;
                }
                ComboReel comboMain = new ComboReel(comboIntHero);
                ComboIso isoComboMain = new ComboIso(comboMain);
                if (comboIso.equals(isoComboMain)) nActions++;
            }
            catch (Exception ignored) {
            }
        }

        return nActions;
    }
}
