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
import java.util.List;

/**
 * calcul l'équité future d'un combo préflop
 * définit ses propres règles par rapport au noeud abstrait
 * todo convertir en classe abstraite
 */
public class CalculEquitePreflop {
    private final static Logger logger = LogManager.getLogger();
    private final EnregistrementEquite enregistrementEquite;
    private final CalculatriceEquite calculatriceEquite;
    private static final GenerateurRange generateurRange = new GenerateurRange();
    private final List<RangeReelle> rangesVillains;
    private final Board board;
    private Integer cleSituation;

    /**
     * utilisé pour génération
     */
    private CalculEquitePreflop() {
        ConfigCalculatrice configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modeExact();
        calculatriceEquite = new CalculatriceEquite(configCalculatrice);

        this.enregistrementEquite = new EnregistrementEquite();

        rangesVillains = new ArrayList<>();
        board = new Board();
    }


    public CalculEquitePreflop(NoeudAbstrait noeudAbstrait) {
        ConfigCalculatrice configCalculatrice = new ConfigCalculatrice();
        configCalculatrice.modePrecision();
        calculatriceEquite = new CalculatriceEquite(configCalculatrice);

        this.enregistrementEquite = new EnregistrementEquite();

        rangesVillains = new ArrayList<>();
        board = new Board();
        determinerSituation(noeudAbstrait);
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

    private EquiteFuture calculerEquite(ComboIso comboIso) throws IOException, DatabaseException {
        logger.trace("Calcul de l'équité pour : " + comboIso.codeReduit());
        ComboReel randomCombo = comboIso.toCombosReels().getFirst();
        EquiteFuture equiteFuture = calculatriceEquite.equiteFutureMain(randomCombo, board, rangesVillains);
        enregistrementEquite.enregistrerCombo(cleSituation, comboIso, equiteFuture);

        return equiteFuture;
    }

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

    private void appliquerCle(int cleSituation) {
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

    /**
     * classe utilitaire qui garde en mémoire la matrice de distance des équités entre tous les combos selon les situations
     */
    static class MatriceDistanceEquite {
        public static float distanceCombos(ComboIso combo1, ComboIso combo2) {
            //todo
            combo1.hashCode();
            return 0f;
        }
    }

    public static void main(String[] args) throws IOException, DatabaseException {
        ComboIso comboIso = new ComboIso("AA");
        CalculEquitePreflop calculEquitePreflop = new CalculEquitePreflop();

        calculEquitePreflop.appliquerCle(1);
        calculEquitePreflop.calculerEquite(comboIso);

        calculEquitePreflop.appliquerCle(2);
        calculEquitePreflop.calculerEquite(comboIso);

        calculEquitePreflop.appliquerCle(3);
        calculEquitePreflop.calculerEquite(comboIso);
    }
}
