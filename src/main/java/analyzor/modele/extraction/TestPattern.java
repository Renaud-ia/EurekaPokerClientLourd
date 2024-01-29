package analyzor.modele.extraction;

import analyzor.modele.poker.Carte;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPattern {
    public static void main(String[] args) {
        String ligne = "Winamax Poker - Tournament \"MINI WSOP THE REUNION - DAY 1A\" buyIn: 4.50€ + 0.50€ level: 1 - HandId: #2125256311774904392-1-1633111225 - Holdem no limit (12/50/100) - 2021/10/01 18:00:25 UTC";


        final Pattern patternPremiereLigne = Pattern.compile(
                "Winamax\\sPoker\\s-\\s" +
                        "(?<nomTournoi>(.(?!buyIn|-))+)\\s" +
                        "(buyIn:\\s(?<buyInMTT>[\\d+\\s\\u20AC€.]+))?" +
                        "((.(?!HandId))+\\s)" +
                        "(HandId:\\s#(?<numeroTournoi>[\\d-]+))" +
                        "(\\s-\\s(?<nomVariante>[\\sa-zA-Z]+))" +
                        "(\\((?<valeursBlindes>[\\d/\\u20AC€.]+)\\))\\s-\\s" +
                        "((?<dateTournoi>(\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2})) UTC$)"
        );

        Matcher matcher = patternPremiereLigne.matcher(ligne);
        System.out.println(matcher.find());
        System.out.println(matcher.group("nomTournoi"));
        System.out.println(matcher.group("buyInMTT"));
        System.out.println(matcher.group("numeroTournoi"));
        System.out.println(matcher.group("nomVariante"));
        System.out.println(matcher.group("valeursBlindes"));
        System.out.println(matcher.group("dateTournoi"));
    }
}
