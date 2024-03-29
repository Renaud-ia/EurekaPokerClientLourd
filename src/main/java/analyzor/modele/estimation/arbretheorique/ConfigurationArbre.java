package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.parties.TourMain;

import java.util.HashMap;


public class ConfigurationArbre {
    private final HashMap<TourMain.Round, ConfigurationRound> configurationsParRound;
    public ConfigurationArbre() {
        configurationsParRound = new HashMap<>();

        ConfigurationRound configPreflop = new ConfigurationPreflop();
        configurationsParRound.put(TourMain.Round.PREFLOP, configPreflop);

        ConfigurationRound configFlop = new ConfigurationRound();
        configurationsParRound.put(TourMain.Round.FLOP, configFlop);

        ConfigurationRound configTurn = new ConfigurationRound();
        configurationsParRound.put(TourMain.Round.TURN, configTurn);

        ConfigurationRound configRiver = new ConfigurationRound();
        configurationsParRound.put(TourMain.Round.RIVER, configRiver);
    }

    public int getNombreReraises(TourMain.Round round) {
        ConfigurationRound configurationRound = obtenirConfiguration(round);
        return configurationRound.maxReraises;
    }

    public int getMaxBetSizes(TourMain.Round round) {
        ConfigurationRound configurationRound = obtenirConfiguration(round);
        return configurationRound.maxBetSizes;
    }

    public void setConfiguration(TourMain.Round round, int maxReraises, int maxBetSizes) {
        ConfigurationRound configurationRound = obtenirConfiguration(round);

        configurationRound.maxReraises = maxReraises;
        configurationRound.maxBetSizes = maxBetSizes;
    }

    public void setPreflopConfiguration(boolean headsUp, int maxActionsActives) {
        ConfigurationPreflop configurationRound = (ConfigurationPreflop) obtenirConfiguration(TourMain.Round.PREFLOP);

        configurationRound.headsUp = headsUp;
        configurationRound.maxActionsActives = maxActionsActives;
    }

    public boolean headsUpPreflop() {
        ConfigurationPreflop configurationPreflop = (ConfigurationPreflop) obtenirConfiguration(TourMain.Round.PREFLOP);
        return configurationPreflop.headsUp;
    }

    public int maxActionsPreflop() {
        ConfigurationPreflop configurationPreflop = (ConfigurationPreflop) obtenirConfiguration(TourMain.Round.PREFLOP);
        return configurationPreflop.maxActionsActives;
    }

    private ConfigurationRound obtenirConfiguration(TourMain.Round round) {
        ConfigurationRound configurationRound = configurationsParRound.get(round);
        if (configurationRound == null) throw new IllegalArgumentException("Le round modifié n'existe pas");
        return configurationRound;
    }

    private class ConfigurationRound {
        
        
        
        private int maxReraises;
        
        
        private int maxBetSizes;
        protected ConfigurationRound() {
        }
    }

    private class ConfigurationPreflop extends ConfigurationRound {
        private boolean headsUp;
        private int maxActionsActives;
    }
}
