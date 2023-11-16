package analyzor.modele.extraction;

import analyzor.modele.poker.Carte;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPattern {
    public static void main(String[] args) {
        String ligne = "Seat 2: RendsL4rgent (button) showed [7s Ah] and won 9064 with One pair : Aces";
        Pattern patternNomGain = Pattern.compile(
                "\\[(?<cards>\\w{2}[\\s\\w{2}]*)\\](\\[(?<newCard>\\w{2})\\])?");
        Matcher matcher = patternNomGain.matcher(ligne);
        System.out.println(matcher.find());
        System.out.println(matcher.group("cards"));

        List<Carte> cartesTrouvees = new ArrayList<>();

        String[] cartesString = matcher.group("cards").split(" ");
        for (String carte : cartesString) {
            assert carte.length() == 2;
            Carte objetCarte = new Carte(carte.charAt(0), carte.charAt(1));
            cartesTrouvees.add(objetCarte);
        }

        String nouvelleCarte = matcher.group("newCard");
        if (nouvelleCarte != null) cartesTrouvees.add(new Carte(nouvelleCarte.charAt(0), nouvelleCarte.charAt(1)));

        System.out.println(cartesTrouvees);
        }
}
