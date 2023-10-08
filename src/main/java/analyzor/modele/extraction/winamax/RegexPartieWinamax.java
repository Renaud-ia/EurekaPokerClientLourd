package analyzor.modele.extraction.winamax;

import analyzor.modele.extraction.DTOLecteurTxt;
import analyzor.modele.parties.Action;
import analyzor.modele.poker.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexPartieWinamax {
    public DTOLecteurTxt.DetailAction trouverAction(String ligne) {
        Pattern pattern = Pattern.compile(
                "(?<playName>.+)\\s(?<action>bets|raises|calls|folds|checks)(\\s(?<bet>\\d+))?(\\sto\\s(?<bet2>\\d+))?(?<allIn>.+all-in)?");
        Matcher matcher = matcherRegex(pattern, ligne);

        String nomAction = matcher.group("action");
        Action action = null;
        boolean totalBet = true;
        boolean betComplet = false;

        if (matcher.group("allIn") != null) {
            action = new Action(Action.Move.ALL_IN, Integer.parseInt(matcher.group("bet")));
        }
        else if (Objects.equals(nomAction, "folds")) {
            action = new Action(Action.Move.FOLD);
        }
        else if (Objects.equals(nomAction, "checks")) {
            action = new Action(Action.Move.CHECK);
        }
        else if (Objects.equals(nomAction, "calls")) {
            action = new Action(Action.Move.CALL, Integer.parseInt(matcher.group("bet")));
        }
        else if (Objects.equals(nomAction, "bets")) {
            action = new Action(Action.Move.RAISE, Integer.parseInt(matcher.group("bet")));
        }
        else if (Objects.equals(nomAction, "raises")) {
            // bug Winamax avec "raises to XXX" plutôt que "raises XXX to XXX"
            if (matcher.group("bet") == null) {
                totalBet = false;
            }
            action = new Action(Action.Move.RAISE, Integer.parseInt(matcher.group("bet2")));
        }
        
        return new DTOLecteurTxt.DetailAction(
                matcher.group("playName"),
                action,
                totalBet,
                betComplet
        );
    }

    public Board trouverBoard(String ligne) {
        List <Carte> cartesBoard = extraireCartes(ligne);
        return new Board(cartesBoard);
    }

    public int trouverMontantBB(String ligne) {
        Pattern pattern = Pattern.compile("\\(([^/]+/[^/]+(?:/[^/]+)?)\\)");
        Matcher matcher = matcherRegex(pattern, ligne);

        String resultats = matcher.group(1);
        String[] nombres = resultats.split("/");

        if (nombres.length >= 2) {
            return Integer.parseInt(nombres[nombres.length - 1]);
        } else {
            throw new IllegalArgumentException("Blindes non trouvées");
        }
    }

    public int trouverIdMain(String ligne) {
        Pattern pattern = Pattern.compile("#[0-9]+-(?<numberHand>[0-9]+)-(?<id>[0-9]+)");
        Matcher matcher = matcherRegex(pattern, ligne);
        return Integer.parseInt(matcher.group("id"));
    }

    public DTOLecteurTxt.SituationJoueur trouverInfosJoueur(String ligne) {
        Pattern pattern = Pattern.compile(
                "Seat\\s(?<seat>\\d+):\\s(?<playName>.+)\\s\\((?<stack>\\d+)(?:,\\s(?<bounty>.+)€ bounty)?\\)");
        Matcher matcher = matcherRegex(pattern, ligne);

        return new DTOLecteurTxt.SituationJoueur(
                matcher.group("playName"),
                Integer.parseInt(matcher.group("seat")),
                Integer.parseInt(matcher.group("stack")),
                Float.parseFloat(matcher.group("bounty"))
        );
    }

    public DTOLecteurTxt.DetailGain trouverGain(String ligne) {
        List <Carte> cartesJoueur = extraireCartes(ligne);
        ComboReel comboJoueur = null;
        if (cartesJoueur.size() > 0) {
            comboJoueur = new ComboReel(cartesJoueur);
        }

        Pattern pattern = Pattern.compile(
                "(?<playName>.+)\\s(\\(.+\\))?.+won\\s(?<gains>\\d+)");
        Matcher matcher = matcherRegex(pattern, ligne);
        String nomJoueur = matcher.group("playName");
        int gains = Integer.parseInt(matcher.group("gains"));

        return new DTOLecteurTxt.DetailGain(nomJoueur, gains, comboJoueur);
    }

    public void trouverBlindesAntes(
            String ligne,
            DTOLecteurTxt.StructureBlinde structureBlinde,
            Map<String, Integer> antesJoueur
    ) {
        Pattern pattern = Pattern.compile(
                "(?<playName>.+)\\sposts\\s((?<blind>\\S*)\\s)(blind\\s)?(?<value>\\d*)");
        Matcher matcher = matcherRegex(pattern, ligne);
        if (Objects.equals(matcher.group("blind"), "ante")) {
            antesJoueur.put(matcher.group("playName"), Integer.parseInt(matcher.group("value")));
        }
        else if (Objects.equals(matcher.group("blind"), "big")) {
            structureBlinde.setJoueurBB(matcher.group("playName"));
        }
        else if (Objects.equals(matcher.group("blind"), "small")) {
            structureBlinde.setJoueurSB(matcher.group("playName"));
        }
        else {
            throw new IllegalArgumentException("Blindes non trouvées dans : " + ligne);
        }
    }

    private Matcher matcherRegex(Pattern pattern, String ligne) {
        Matcher matcher = pattern.matcher(ligne);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Regex non trouvé dans : " + ligne);
        }
        return matcher;
    }

    private List<Carte> extraireCartes(String ligne) {
        Pattern pattern = Pattern.compile(
                "\\[(?<cards>\\w{2}[\\s\\w{2}]*)\\](\\[(?<newCard>\\w{2})\\])?");
        Matcher matcher = matcherRegex(pattern, ligne);

        List<Carte> cartesTrouvees = new ArrayList<>();

        if (matcher.group("cards") == null) return null;

        String[] cartesString = matcher.group("cards").split(" ");
        for (String carte : cartesString) {
            assert carte.length() == 2;
            Carte objetCarte = new Carte(carte.charAt(0), carte.charAt(1));
            cartesTrouvees.add(objetCarte);
        }

        String nouvelleCarte = matcher.group("newCard");
        if (nouvelleCarte != null) cartesTrouvees.add(new Carte(nouvelleCarte.charAt(0), nouvelleCarte.charAt(1)));

        return cartesTrouvees;
    }

}
