package analyzor.modele.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPatterns {
    public static void main(String[] args) {
        Pattern patternInfos = Pattern.compile(
                "Winamax Poker - Tournament summary : (?<nomTournoi>.+?)\\((?<idTournoi>\\d+)\\)");
        Pattern patternHero = Pattern.compile("Player : (?<nomJoueur>.+)");
        Pattern patternBI = Pattern.compile("Buy-In : ([0-9]+(?:\\.[0-9]+)?)[€$] \\+ ([0-9]+(?:\\.[0-9]+)?)[€$]");
        Pattern patternFormat = Pattern.compile("Mode : (?<format>.+)");
        Pattern patternVitesse = Pattern.compile("Speed : (?<vitesse>.+)");
        Pattern patternAnte = Pattern.compile("^.*?\\[\\d+-(?<bb>\\d+):(?<ante>\\d+)");
        Pattern patternDate = Pattern.compile(
                "^Tournament started (\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}) UTC$");
        Pattern patternType = Pattern.compile("Type : (?<knockout>.+)");

        String line = "Winamax Poker - Tournament summary : Expresso(527507407)";

        Matcher matcher = patternInfos.matcher(line);

        System.out.println(matcher.find());
        System.out.println(matcher.group("nomTournoi"));
        System.out.println(matcher.group("idTournoi"));
    }
}
