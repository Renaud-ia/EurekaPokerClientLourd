package analyzor.modele.extraction;

import analyzor.modele.parties.Action;
import analyzor.modele.parties.Move;
import analyzor.modele.poker.Carte;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPattern {
    public static void main(String[] args) {
        String ligne = "bolchevik33 raises 0.15€ to 1.56€";


        final Pattern patternAction = Pattern.compile(
                "(?<playName>.+)\\s"+
                        "(?<action>bets|raises|calls|folds|checks)" +
                        "(\\s(?<bet>[\\d.]+))?[\\u20AC€]*"+
                        "(\\sto\\s(?<bet2>[\\d.]+))?[\\u20AC€]*"+
                        "(?<allIn>(.+all-in))?"
        );

        Matcher matcher = patternAction.matcher(ligne);
        matcher.find();
        String nomAction = matcher.group("action");
        Action action = null;
        boolean totalBet = true;
        boolean betComplet = true;


        if (Objects.equals(nomAction, "folds")) {
            action = new Action(Move.FOLD);
        }
        else if (Objects.equals(nomAction, "checks")) {
            action = new Action(Move.CALL);
        }
        else if (Objects.equals(nomAction, "calls")) {
            // call on a juste le montant de la complétion
            totalBet = false;
            action = new Action(Move.CALL, Float.parseFloat(matcher.group("bet")));
            if (matcher.group("allIn") != null) {
                action.setMove(Move.ALL_IN);
            }
        }
        else if (Objects.equals(nomAction, "bets")) {
            action = new Action(Move.RAISE, Float.parseFloat(matcher.group("bet")));
        }
        else if (Objects.equals(nomAction, "raises")) {
            // BUG WINAMAX, affiche parfois "raises to [bet2]" plutôt que "raises [bet1] to [bet2]"
            if (matcher.group("bet") == null) {
                betComplet = false;
            }
            action = new Action(Move.RAISE, Float.parseFloat(matcher.group("bet2")));
            if (matcher.group("allIn") != null) {
                action.setMove(Move.ALL_IN);
            }
        }

        System.out.println(action.getMove());
        System.out.println(action.getBetSize());

    }
}
