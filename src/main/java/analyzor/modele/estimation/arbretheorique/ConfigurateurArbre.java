package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.parties.TourMain;

/**
 * crée des configurations standard de l'arbre abstrait
 * maxReraises = 1 => 3BET ALL IN
 * maxReraires = 2 => 4BET ALL IN
 * maxReraires = 3 => 5BET ALL IN
 */
public class ConfigurateurArbre {
    public static ConfigurationArbre SPIN() {
        ConfigurationArbre configurationSPIN = new ConfigurationArbre();

        configurationSPIN.setConfiguration(TourMain.Round.PREFLOP, 1, 3);
        configurationSPIN.setPreflopConfiguration(true, 3);
        configurationSPIN.setConfiguration(TourMain.Round.FLOP, 1, 3);
        configurationSPIN.setConfiguration(TourMain.Round.TURN, 1, 2);
        configurationSPIN.setConfiguration(TourMain.Round.RIVER, 1, 2);

        return configurationSPIN;
    }

    public static ConfigurationArbre CASH() {
        ConfigurationArbre configurationCASH = new ConfigurationArbre();

        configurationCASH.setConfiguration(TourMain.Round.PREFLOP, 3, 3);
        configurationCASH.setPreflopConfiguration(false, 3);
        configurationCASH.setConfiguration(TourMain.Round.FLOP, 2, 3);
        configurationCASH.setConfiguration(TourMain.Round.TURN, 1, 2);
        configurationCASH.setConfiguration(TourMain.Round.RIVER, 1, 2);

        return configurationCASH;
    }
    public static ConfigurationArbre MTT() {
        ConfigurationArbre configurationMTT = new ConfigurationArbre();

        configurationMTT.setConfiguration(TourMain.Round.PREFLOP, 2, 3);
        configurationMTT.setPreflopConfiguration(false, 3);
        configurationMTT.setConfiguration(TourMain.Round.FLOP, 1, 3);
        configurationMTT.setConfiguration(TourMain.Round.TURN, 1, 2);
        configurationMTT.setConfiguration(TourMain.Round.RIVER, 1, 2);

        return configurationMTT;
    }

    public static ConfigurationArbre DEFAUT() {
        ConfigurationArbre configurationMTT = new ConfigurationArbre();

        configurationMTT.setConfiguration(TourMain.Round.PREFLOP, 2, 3);
        configurationMTT.setPreflopConfiguration(false, 3);
        configurationMTT.setConfiguration(TourMain.Round.FLOP, 1, 3);
        configurationMTT.setConfiguration(TourMain.Round.TURN, 1, 2);
        configurationMTT.setConfiguration(TourMain.Round.RIVER, 1, 2);

        return configurationMTT;
    }
}
