package analyzor.modele.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPattern {
    public static void main(String[] args) {
        String ligne = "PleinAuAs2 raises to 56";

        Pattern pattern = Pattern.compile(
                "(?<playName>.+)\\s(?<action>bets|raises|calls|folds|checks)(\\s(?<bet>\\d+))?(\\sto\\s(?<bet2>\\d+))?(?<allIn>.+all-in)?");

        Matcher matcher = pattern.matcher(ligne);
        System.out.println(matcher.find());

        System.out.println(matcher.group("playName"));
        System.out.println(matcher.group("action"));
        System.out.println(matcher.group("bet"));
        System.out.println(matcher.group("bet2"));
        System.out.println(matcher.group("allIn") != null);

    }
}
