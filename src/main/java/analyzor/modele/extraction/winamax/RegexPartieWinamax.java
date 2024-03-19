package analyzor.modele.extraction.winamax;

import analyzor.modele.extraction.DTOLecteurTxt;
import analyzor.modele.extraction.exceptions.ErreurImportation;
import analyzor.modele.extraction.exceptions.ErreurMatch;
import analyzor.modele.extraction.exceptions.FormatNonPrisEnCharge;
import analyzor.modele.extraction.exceptions.InformationsIncorrectes;
import analyzor.modele.parties.Action;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.Variante;
import analyzor.modele.poker.Board;
import analyzor.modele.poker.Carte;
import analyzor.modele.poker.ComboReel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexPartieWinamax {
    // todo lever une nouvelle erreur spécifique aux regex

    // méthode de recherche avec leur regex associées

    private static final Pattern patternAction = Pattern.compile(
            "(?<playName>.+)\\s"+
                    "(?<action>bets|raises|calls|folds|checks)" +
                    "(\\s(?<bet>[\\d.]+))?[\\u20AC€]*"+
                    "(\\sto\\s(?<bet2>[\\d.]+))?[\\u20AC€]*"+
                    "(?<allIn>(.+all-in))?"
    );

    public DTOLecteurTxt.DetailAction trouverAction(String ligne) throws ErreurMatch {
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

    private static final Pattern patternBB = Pattern.compile("\\(([^/]+/[^/]+(?:/[^/]+)?)\\)");

    public float trouverMontantBB(String ligne) throws InformationsIncorrectes, ErreurMatch {
        Matcher matcher = matcherRegex(patternBB, ligne);

        String resultats = matcher.group(1);
        String[] nombres = resultats.split("/");

        if (nombres.length >= 2) {
            return Float.parseFloat(nombres[nombres.length - 1].replaceAll("[^\\d.]", ""));
        }
        else {
            throw new InformationsIncorrectes("Blindes non trouvées");
        }
    }

    private static final Pattern patternIdMain = Pattern.compile("#[0-9]+-(?<numberHand>[0-9]+)-(?<id>[0-9]+)");

    public long trouverIdMain(String ligne) throws ErreurMatch {
        Matcher matcher = matcherRegex(patternIdMain, ligne);
        return Long.parseLong(matcher.group("id"));
    }

    private static final Pattern patternInfoJoueur = Pattern.compile(
            "Seat\\s(?<seat>\\d+):" +
                    "\\s(?<playName>.+)\\s" +
                    "\\((?<stack>[\\d.]+)(\\u20AC)?" +
                    // si le bounty n'est pas en €, on n'en veut pas car pas exploitable
                    // donc il est hors du groupe de capture
                    "(,\\s((?<bounty>[\\d.]+)\\u20AC\\s(bounty)|[\\d.]+\\s(bounty))?)?\\)"
    );

    public DTOLecteurTxt.SituationJoueur trouverInfosJoueur(String ligne) throws ErreurMatch {

        Matcher matcher = matcherRegex(patternInfoJoueur, ligne);

        float bounty = (matcher.group("bounty") != null) ? Float.parseFloat(matcher.group("bounty")) : 0;

        return new DTOLecteurTxt.SituationJoueur(
                matcher.group("playName"),
                Integer.parseInt(matcher.group("seat")),
                Float.parseFloat(matcher.group("stack")),
                bounty
        );
    }

    private static final Pattern patternNomGain = Pattern.compile(
            "Seat\\s\\d:\\s(?<playName>(?:(?!\\sshowed\\s|\\swon\\s|[()]).)*)\\s.+");
    private static final Pattern patternGains = Pattern.compile("\\swon\\s(?<gains>[\\d.]+)");

    public DTOLecteurTxt.DetailGain trouverGain(String ligne) throws ErreurMatch {
        List <Carte> cartesJoueur = extraireCartes(ligne);
        ComboReel comboJoueur = null;
        if (cartesJoueur != null) {
            comboJoueur = new ComboReel(cartesJoueur);
        }

        // on a exclu les parenthèses car relou sinon pour capturer ce qu'on veut
        // apparemment Wina ne les accepte pas
        Matcher matcherNom = matcherRegex(patternNomGain, ligne);
        Matcher matcherGains = patternGains.matcher(ligne);

        float gains;
        if (!matcherGains.find()) {
            gains = 0;
        }
        else {
            gains = Float.parseFloat(matcherGains.group("gains"));
        }

        String nomJoueur = matcherNom.group("playName");

        return new DTOLecteurTxt.DetailGain(nomJoueur, gains, comboJoueur);
    }

    public ComboReel cartesHero(String ligne) throws InformationsIncorrectes {
        List<Carte> cartesTrouvees = extraireCartes(ligne);
        if (cartesTrouvees == null || cartesTrouvees.isEmpty())
            throw new InformationsIncorrectes("Aucune carte trouvée pour hero");

        return new ComboReel(cartesTrouvees);
    }

    private static final Pattern patternBlindesAntes = Pattern.compile(
            "(?<playName>.+)\\sposts\\s((?<blind>\\S*)\\s)(blind\\s)?(?<value>[\\d.]+)");

    @Deprecated
    public void trouverBlindesAntes(
            String ligne,
            DTOLecteurTxt.StructureBlinde structureBlinde,
            Map<String, Float> antesJoueur
    ) throws InformationsIncorrectes, ErreurMatch {

        Matcher matcher = matcherRegex(patternBlindesAntes, ligne);
        if (Objects.equals(matcher.group("blind"), "ante")) {
            antesJoueur.put(matcher.group("playName"), Float.parseFloat(matcher.group("value")));
        }
        else if (Objects.equals(matcher.group("blind"), "big")) {
            structureBlinde.setJoueurBB(matcher.group("playName"));
        }
        else if (Objects.equals(matcher.group("blind"), "small")) {
            structureBlinde.setJoueurSB(matcher.group("playName"));
        }
        else {
            throw new InformationsIncorrectes("Blindes non trouvées dans : " + ligne);
        }
    }

    public DTOLecteurTxt.BlindesAnte trouverBlindesAnte(String ligne) throws ErreurImportation {
        Matcher matcher = matcherRegex(patternBlindesAntes, ligne);
        DTOLecteurTxt.BlindesAnte.TypeTaxe typeTaxe;

        if (Objects.equals(matcher.group("blind"), "ante")) {
           typeTaxe = DTOLecteurTxt.BlindesAnte.TypeTaxe.ANTE;
        }
        else if (Objects.equals(matcher.group("blind"), "big")) {
            typeTaxe = DTOLecteurTxt.BlindesAnte.TypeTaxe.BLINDES;
        }
        else if (Objects.equals(matcher.group("blind"), "small")) {
            typeTaxe = DTOLecteurTxt.BlindesAnte.TypeTaxe.BLINDES;
        }
        else {
            throw new InformationsIncorrectes("Blindes non trouvées dans : " + ligne);
        }

        return new DTOLecteurTxt.BlindesAnte(matcher.group("playName"),
                typeTaxe,
                Float.parseFloat(matcher.group("value")));
    }

    private static final Pattern patternPremiereLigne = Pattern.compile(
            "Winamax\\sPoker\\s-\\s" +
                    "(?<nomTournoi>(.(?!buyIn|- HandId))+)\\s" +
                    "(buyIn:\\s(?<buyInMTT>[\\d+\\s\\u20AC€.]+))?" +
                    "((.(?!HandId))+\\s)" +
                    "(HandId:\\s#(?<numeroTournoi>[\\d-]+))" +
                    "(\\s-\\s(?<nomVariante>[\\sa-zA-Z]+))" +
                    "(\\((?<valeursBlindes>[\\d/\\u20AC€.]+)\\))\\s-\\s" +
                    "((?<dateTournoi>(\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2})) UTC$)"
    );

    public void infosPremiereLigne(String ligne, DTOLecteurWinamax.InfosPartie infosPartie)
            throws ErreurImportation {
        //todo refactoriser avec DTOLEcteurWinamax
        Matcher matcher = matcherRegex(patternPremiereLigne, ligne);

        // on trouve le format
        Variante.PokerFormat pokerFormat;
        String nomTournoi = matcher.group("nomTournoi");
        if (nomTournoi == null) throw new ErreurMatch("Aucun nom de tournoi trouvé");

        else if (nomTournoi.contains("CashGame")) {
            pokerFormat = Variante.PokerFormat.CASH_GAME;
        }
        // attention Expresso contient aussi Tournament
        else if (nomTournoi.contains("Expresso")) {
            pokerFormat = Variante.PokerFormat.SPIN;
        }

        else if (nomTournoi.contains("Tournament")) {
            pokerFormat = Variante.PokerFormat.MTT;
        }

        else {
            pokerFormat = Variante.PokerFormat.INCONNU;
        }

        // on trouve la variante
        Variante.VariantePoker variantePoker;
        String nomVariante = matcher.group("nomVariante");
        if (nomVariante == null) throw new ErreurMatch("Aucun nom de variante trouvé");
        if (nomVariante.contains("Holdem no limit")) {
            variantePoker = Variante.VariantePoker.HOLDEM_NO_LIMIT;
        }

        else {
            variantePoker = Variante.VariantePoker.INCONNU;
        }

        // on convertit le buy in et on récupère le rake
        // la procédure est différente selon MTT/SPIN ou Cash-Game
        // récupération manuelle ante/rake désactivé car relou
        float ante = 0;
        float rake = 0;
        float buyIn = 0;

        if (matcher.group("buyInMTT") != null) {
            String[] buyInParties = matcher.group("buyInMTT").split("\\+");
            for (String partieBuyIn : buyInParties) {
                buyIn += Float.parseFloat(partieBuyIn.replaceAll("[^\\d.]", ""));
            }


            if (pokerFormat == Variante.PokerFormat.MTT) {
                if (matcher.group("valeursBlindes") != null) {
                    String[] partiesBlindes = matcher.group("valeursBlindes").split("/");
                    // attention il y a des tournois sans Ante(starting block par ex)
                    if (partiesBlindes.length != 3) throw new FormatNonPrisEnCharge("Tournoi gratuit probablement");

                    ante = Float.parseFloat(partiesBlindes[2]) / Float.parseFloat(partiesBlindes[1]);
                }

                else throw new ErreurMatch("Pas de blindes trouvées");
            }

            // en Spin, on n'a pas d'ANTE avec WINAMAX
            else {
                ante = 0f;
            }
        }

        // si cash-game
        else if (matcher.group("valeursBlindes") != null)  {
            String[] partiesBlindes = matcher.group("valeursBlindes").split("/");
            String montantBB = partiesBlindes[partiesBlindes.length - 1].replaceAll("[^\\d.]", "");
            buyIn = Float.parseFloat(montantBB);
        }

        else throw new ErreurMatch("Le buy in n'a pas été trouvée");


        // on récupère le numéro de main et de table
        long numeroTable;
        long numeroMain;
        if (matcher.group("numeroTournoi") == null) throw new ErreurMatch("Numéro de tournoi non trouvé");
        String[] idTournois = matcher.group("numeroTournoi").split("-");
        if (idTournois.length != 3) throw new ErreurMatch("Format numéro tournoi non conforme");
        numeroTable = Long.parseLong(idTournois[0]);
        numeroMain = Long.parseLong(idTournois[2]);

        // on récupère la date
        String dateTimeStr = matcher.group("dateTournoi");
        if (dateTimeStr == null) throw new ErreurMatch("Date non trouvée");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime date = LocalDateTime.parse(dateTimeStr, formatter);


        infosPartie.setInfosFormat(new DTOLecteurWinamax.InfosFormat(
                variantePoker,
                pokerFormat,
                buyIn,
                date,
                numeroTable,
                numeroMain,
                ante,
                rake
        ));
    }

    private static final Pattern patternInfosTable = Pattern.compile(
            "Table:\\s'(?<nomTable>.+)'\\s" +
                    "(?<nombreJoueurs>[0-9]+)-max"
    );

    public void infosTable(String ligne, DTOLecteurWinamax.InfosPartie infosPartie) throws ErreurMatch {
        Matcher matcher = matcherRegex(patternInfosTable, ligne);

        infosPartie.setInfosTable(new DTOLecteurWinamax.InfosTable(
                matcher.group("nomTable"), Integer.parseInt(matcher.group("nombreJoueurs"))));
    }

    private static final Pattern patternNomHero = Pattern.compile(
            "Dealt to ((?<nomHero>.+)\\s\\[)"
    );

    public String nomHero(String ligne) throws ErreurMatch {
        Matcher matcher = matcherRegex(patternNomHero, ligne);
        return matcher.group("nomHero");
    }

    private static final Pattern patternRakeCG = Pattern.compile(
            "Total\\spot\\s" +
                    "(?<valeurPot>[\\w.]+)(\\u20AC)?\\s\\|\\s" +
                    "(No\\s)?(Rake|rake)(\\s(?<valeurRake>[\\w.]+)\\u20AC)?"
    );
    public float trouverRake(String ligne) throws ErreurMatch {
        Matcher matcher = matcherRegex(patternRakeCG, ligne);
        // parfois dans les MTT il y a marqué "No Rake" après Total pot
        if (matcher.group("valeurRake") == null) return 0f;
        return Float.parseFloat(matcher.group("valeurRake")) / Float.parseFloat(matcher.group("valeurPot"));
    }

    public float trouverRakeTotal(String ligne) throws ErreurMatch {
        Matcher matcher = matcherRegex(patternRakeCG, ligne);
        // parfois dans les MTT il y a marqué "No Rake" après Total pot
        if (matcher.group("valeurRake") == null) return 0f;
        return Float.parseFloat(matcher.group("valeurRake"));
    }


    // méthodes privées avec leur regex

    private Matcher matcherRegex(Pattern pattern, String ligne) throws ErreurMatch {
        Matcher matcher = pattern.matcher(ligne);
        if (!matcher.find()) {
            throw new ErreurMatch("Regex non trouvé dans : " + ligne);
        }
        return matcher;
    }

    private static final Pattern patternCartes = Pattern.compile(
            "\\[(?<cards>\\w{2}[\\s\\w{2}]*)](\\[(?<newCard>\\w{2})])?");

    /**
     * méthode privée pour générer des cartes
     * @param ligne une ligne avec les cartes sous cette forme [XX XX XX](optionnel : [XX]+)
     * @return la liste des cartes
     */
    private List<Carte> extraireCartes(String ligne) {
        Matcher matcher = patternCartes.matcher(ligne);
        if (!matcher.find()) {
            return null;
        }

        List<Carte> cartesTrouvees = new ArrayList<>();

        String[] cartesString = matcher.group("cards").split(" ");
        for (String carte : cartesString) {
            if (carte.length() != 2) throw new RuntimeException("Le format de la carte n'est pas bon");
            Carte objetCarte = new Carte(carte.charAt(0), carte.charAt(1));
            cartesTrouvees.add(objetCarte);
        }

        String nouvelleCarte = matcher.group("newCard");
        if (nouvelleCarte != null) cartesTrouvees.add(new Carte(nouvelleCarte.charAt(0), nouvelleCarte.charAt(1)));

        return cartesTrouvees;
    }


}
