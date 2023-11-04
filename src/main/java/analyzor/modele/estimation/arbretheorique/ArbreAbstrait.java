package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.estimation.FormatSolution;

import java.util.HashMap;

/**
 * génère un arbre théorique à partir de séquences d'action (sans BETSIZE)
 * configurable (nombre de relances)
 * flop, turn et river
 */
public class ArbreAbstrait {
    private final ConfigurationArbre configurationArbre;
    private final FormatSolution formatSolution;
    private final HashMap<Long, NoeudTheorique> situationsPrecedentes;
    private final HashMap<Long, NoeudTheorique> situationsSuivantes;
    public ArbreAbstrait(ConfigurationArbre configurationArbre, FormatSolution formatSolution) {
        this.configurationArbre = configurationArbre;
        this.formatSolution = formatSolution;
        situationsPrecedentes = new HashMap<>();
        situationsSuivantes = new HashMap<>();
        genererArbre();
    }

    private void genererArbre() {

    }
}
