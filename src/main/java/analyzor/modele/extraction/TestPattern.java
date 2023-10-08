package analyzor.modele.extraction;

import analyzor.modele.poker.Carte;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPattern {
    public static void main(String[] args) {
        String ligne = "Seat 2: LaMissDu33 (big blind) (button) showed [7h Jh] and won 180 with Trips of Jacks";

        Pattern patternNom = Pattern.compile(
                "Seat\\s\\d:\\s(?<playName>.[^()]+)\\s(\\(.+\\)\\s)?(showed|won)");
        Matcher matcherNom = patternNom.matcher(ligne);


        Pattern patternGains = Pattern.compile("won\\s(?<gain>\\d+)");
        Matcher matcherGains = patternGains.matcher(ligne);

        System.out.println(matcherNom.find());
        System.out.println(matcherGains.find());
        System.out.println(matcherNom.group("playName"));
        System.out.println(matcherGains.group("gain"));
        }
}
