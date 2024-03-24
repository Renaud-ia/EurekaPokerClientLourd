package analyzor.modele.denombrement;

import analyzor.modele.berkeley.EnregistrementEquite;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.poker.*;
import analyzor.modele.poker.evaluation.CalculatriceEquite;
import analyzor.modele.poker.evaluation.ConfigCalculatrice;
import analyzor.modele.poker.evaluation.EquiteFuture;
import com.sleepycat.je.DatabaseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * calcul l'équité future d'un combo préflop
 * définit ses propres règles par rapport au noeud abstrait
 * pattern singleton => attention le jour où vuet multithreader!!!
 */
public final class CalculEquitePreflop {
    private static CalculEquitePreflop instance;
    private final static Logger logger = LogManager.getLogger(CalculEquitePreflop.class);
    private static EnregistrementEquite enregistrementEquite;
    private static CalculatriceEquite calculatriceEquite;
    private static final GenerateurRange generateurRange = new GenerateurRange();
    private static List<RangeReelle> rangesVillains;
    private static Board board;
    private static Integer cleSituation;
    private static final HashMap<Long, Float> mapsDistances = new HashMap<>();

    /**
     * utilisé pour génération
     */
    private CalculEquitePreflop() {
        logger.trace("Instance créée de CalculEquitePreflop");
        ConfigCalculatrice configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modeExact();
        calculatriceEquite = new CalculatriceEquite(configCalculatrice);

        enregistrementEquite = new EnregistrementEquite();

        rangesVillains = new ArrayList<>();
        board = new Board();
    }

    public static CalculEquitePreflop getInstance() {
        if (instance == null) {
            instance = new CalculEquitePreflop();
        }

        return instance;
    }

    // méthodes publiques

    public void setNoeudAbstrait(NoeudAbstrait noeudAbstrait) {
        determinerSituation(noeudAbstrait);

        // todo optimisation : on pourrait garder les résultats des autres situations pour ne pas avoir à recalculer
        mapsDistances.clear();
    }

    public EquiteFuture getEquite(ComboIso comboIso) {
        if (cleSituation == null) throw new RuntimeException("Clé situation non initialisée");

        EquiteFuture equiteFuture = null;
        try {
            equiteFuture = enregistrementEquite.recupererEquite(cleSituation, comboIso);
            if (equiteFuture == null) equiteFuture = calculerEquite(comboIso);
        } catch (Exception e) {
            logger.error("Problème de récupération de l'équité dans BDD", e);
        }
        //logger.trace("Equité combo récupéré dans BDD : " + comboIso.codeReduit());
        return equiteFuture;
    }

    public float distanceCombos(ComboIso combo1, ComboIso combo2) {
        long code1 = ((long) combo1.hashCode() << 32) + combo2.hashCode();
        long code2 = ((long) combo2.hashCode() << 32) + combo1.hashCode();

        Float distanceCode1 = mapsDistances.get(code1);
        if (distanceCode1 != null) return distanceCode1;

        Float distanceCode2 = mapsDistances.get(code2);
        if (distanceCode2 != null) return distanceCode2;

        float distanceCalculee = getEquite(combo1).distance(getEquite(combo2));
        long codeEquiteCalculee = ((long) combo1.hashCode() << 32) + combo2.hashCode();
        mapsDistances.put(codeEquiteCalculee, distanceCalculee);

        return distanceCalculee;
    }

    private EquiteFuture calculerEquite(ComboIso comboIso) throws IOException, DatabaseException {
        logger.trace("Calcul de l'équité pour : " + comboIso.codeReduit());
        ComboReel randomCombo = comboIso.toCombosReels().getFirst();
        EquiteFuture equiteFuture = calculatriceEquite.equiteFutureMain(randomCombo, board, rangesVillains);
        enregistrementEquite.enregistrerCombo(cleSituation, comboIso, equiteFuture);

        return equiteFuture;
    }

    // méthodes privées

    /**
     * on va déterminer les ranges des villain selon le noeud Abstrait
     * et attribuer une clé pour enregistrement dans la base
     * todo OPTIMISATION => on pourrait ajouter le nombre de joueurs ou plus de combinaisons
     */
    private void determinerSituation(NoeudAbstrait noeudAbstrait) {
        if (noeudAbstrait.nombreRaise() > 1) {
            cleSituation = 1;
        }

        else if (noeudAbstrait.nombreRaise() > 2 || noeudAbstrait.hasAllin()) {
            cleSituation = 2;
        }

        else {
            cleSituation = 3;
        }

        appliquerCle(cleSituation);
        logger.trace("Clé situation choisie : " + cleSituation);
    }

    // pour calcul

    private void appliquerCle(int cleSituation) {
        rangesVillains.clear();

        if (cleSituation == 1) {
            RangeReelle rangeVillain = generateurRange.topRange(0.37f);
            rangesVillains.add(rangeVillain);
        }

        else if (cleSituation == 2) {
            RangeReelle rangeVillain = generateurRange.topRange(0.12f);
            rangesVillains.add(rangeVillain);
        }

        else {
            RangeReelle rangeVillain = generateurRange.topRange(1f);
            rangesVillains.add(rangeVillain);
        }
    }


    public static void main(String[] args) throws IOException, DatabaseException {
        ComboIso comboIso = new ComboIso("AA");

        CalculEquitePreflop.getInstance().appliquerCle(1);
        CalculEquitePreflop.getInstance().calculerEquite(comboIso);

        CalculEquitePreflop.getInstance().appliquerCle(2);
        CalculEquitePreflop.getInstance().calculerEquite(comboIso);

        CalculEquitePreflop.getInstance().appliquerCle(3);
        CalculEquitePreflop.getInstance().calculerEquite(comboIso);
    }
}
