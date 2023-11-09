package analyzor.modele.extraction.winamax;

import analyzor.modele.extraction.DTOLecteurTxt;
import analyzor.modele.parties.Action;
import analyzor.modele.parties.Move;
import analyzor.modele.poker.Board;
import analyzor.modele.poker.Carte;
import analyzor.modele.poker.ComboReel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexPartieWinamax {
    private static final Pattern patternAction = Pattern.compile(
            "(?<playName>.+)\\s(?<action>bets|raises|calls|folds|checks)(\\s(?<bet>\\d+))?(\\sto\\s(?<bet2>\\d+))?(?<allIn>.+all-in)?");
    private static final Pattern patternBB = Pattern.compile("\\(([^/]+/[^/]+(?:/[^/]+)?)\\)");
    private static final Pattern patternIdMain = Pattern.compile("#[0-9]+-(?<numberHand>[0-9]+)-(?<id>[0-9]+)");
    private static final Pattern patternInfoJoueur = Pattern.compile(
            "Seat\\s(?<seat>\\d+):\\s(?<playName>.+)\\s\\((?<stack>\\d+)(,\\s(?<bounty>.+)\\u20AC bounty)?\\)");

    private static final Pattern patternNomGain = Pattern.compile(
            "Seat\\s\\d:\\s(?<playName>(?:(?!showed|won|[\\(\\)]).)*)\\s.+");
    private static final Pattern patternGains = Pattern.compile("won\\s(?<gains>\\d+)");
    private static final Pattern patternBlindesAntes = Pattern.compile(
            "(?<playName>.+)\\sposts\\s((?<blind>\\S*)\\s)(blind\\s)?(?<value>\\d*)");
    private static final Pattern patternCartes = Pattern.compile(
            "\\[(?<cards>\\w{2}[\\s\\w{2}]*)\\](\\[(?<newCard>\\w{2})\\])?");

    public DTOLecteurTxt.DetailAction trouverAction(String ligne) {
        Matcher matcher = matcherRegex(patternAction, ligne);

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
            action = new Action(Move.CALL, Integer.parseInt(matcher.group("bet")));
            if (matcher.group("allIn") != null) {
                action.setMove(Move.ALL_IN);
            }
        }
        else if (Objects.equals(nomAction, "bets")) {
            action = new Action(Move.RAISE, Integer.parseInt(matcher.group("bet")));
        }
        else if (Objects.equals(nomAction, "raises")) {
            // BUG WINAMAX, affiche parfois "raises to [bet2]" plutôt que "raises [bet1] to [bet2]"
            if (matcher.group("bet") == null) {
                betComplet = false;
            }
            action = new Action(Move.RAISE, Integer.parseInt(matcher.group("bet2")));
            if (matcher.group("allIn") != null) {
                action.setMove(Move.ALL_IN);
            }
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
        Matcher matcher = matcherRegex(patternBB, ligne);

        String resultats = matcher.group(1);
        String[] nombres = resultats.split("/");

        if (nombres.length >= 2) {
            return Integer.parseInt(nombres[nombres.length - 1]);
        } else {
            throw new IllegalArgumentException("Blindes non trouvées");
        }
    }

    public long trouverIdMain(String ligne) {
        Matcher matcher = matcherRegex(patternIdMain, ligne);
        return Long.parseLong(matcher.group("id"));
    }

    public DTOLecteurTxt.SituationJoueur trouverInfosJoueur(String ligne) {

        Matcher matcher = matcherRegex(patternInfoJoueur, ligne);

        float bounty = (matcher.group("bounty") != null) ? Float.parseFloat(matcher.group("bounty")) : 0;

        return new DTOLecteurTxt.SituationJoueur(
                matcher.group("playName"),
                Integer.parseInt(matcher.group("seat")),
                Integer.parseInt(matcher.group("stack")),
                bounty
        );
    }

    public DTOLecteurTxt.DetailGain trouverGain(String ligne) {
        List <Carte> cartesJoueur = extraireCartes(ligne);
        ComboReel comboJoueur = null;
        if (cartesJoueur != null) {
            comboJoueur = new ComboReel(cartesJoueur);
        }

        // on a exclu les parenthèses car relou sinon pour capturer ce qu'on veut
        // apparemment Wina ne les accepte pas
        Matcher matcherNom = matcherRegex(patternNomGain, ligne);
        Matcher matcherGains = patternGains.matcher(ligne);

        int gains;
        if (!matcherGains.find()) {
            gains = 0;
        }
        else {
            gains = Integer.parseInt(matcherGains.group("gains"));
        }

        String nomJoueur = matcherNom.group("playName");

        return new DTOLecteurTxt.DetailGain(nomJoueur, gains, comboJoueur);
    }

    public void trouverBlindesAntes(
            String ligne,
            DTOLecteurTxt.StructureBlinde structureBlinde,
            Map<String, Integer> antesJoueur
    ) {
        Matcher matcher = matcherRegex(patternBlindesAntes, ligne);
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
        Matcher matcher = patternCartes.matcher(ligne);
        if (!matcher.find()) {
            return null;
        }

        List<Carte> cartesTrouvees = new ArrayList<>();

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
