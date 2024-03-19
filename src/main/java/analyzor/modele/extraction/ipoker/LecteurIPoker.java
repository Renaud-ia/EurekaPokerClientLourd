package analyzor.modele.extraction.ipoker;

import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.extraction.DTOLecteurTxt;
import analyzor.modele.extraction.EnregistreurMain;
import analyzor.modele.extraction.LecteurPartie;
import analyzor.modele.extraction.exceptions.ErreurImportation;
import analyzor.modele.extraction.exceptions.FichierCorrompu;
import analyzor.modele.extraction.exceptions.FormatNonPrisEnCharge;
import analyzor.modele.extraction.exceptions.InformationsIncorrectes;
import analyzor.modele.parties.*;
import analyzor.modele.poker.Board;
import analyzor.modele.poker.Carte;
import analyzor.modele.poker.ComboReel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LecteurIPoker extends LecteurPartie {
    // todo déterminer le bon ante/rake de Betclic et prévoir Parions SPort....
    private static final float ANTE_BETCLIC = 10f;
    private static final float RAKE_BETCLIC = 5f;
    // todo à refactoriser
    private Document document;
    public LecteurIPoker(Path cheminDuFichier) {
        super(cheminDuFichier, PokerRoom.IPOKER);
    }

    @Override
    public Integer sauvegarderPartie() {
        super.ouvrirTransaction();
        logger.debug("Sauvegarde en cours dans la BDD pour : " + cheminDuFichier.toString());
        Exception exceptionApparue = null;
        int compteMains = 0;

        try {
            Partie partie = getPartie();
            if (partie == null) return null;

            Element generalElement = (Element) document.getElementsByTagName("general").item(0);
            String nomHero = generalElement.getElementsByTagName("nickname").item(0).getTextContent();

            NodeList gameElements = document.getElementsByTagName("game");
            for (int i = 0; i < gameElements.getLength(); i++) {
                Element gameElement = (Element) gameElements.item(i);

                long idMain = Long.parseLong(gameElement.getAttribute("gamecode"));
                logger.trace("Main trouvée :" + idMain);
                float montantBB = trouverMontantBB(gameElement);

                EnregistreurMain enregistreur = new EnregistreurMain(
                        idMain,
                        montantBB,
                        partie,
                        variante.getBuyIn(),
                        partie.getNomHero(),
                        PokerRoom.IPOKER,
                        variante.hasBounty(),
                        variante.getNombreJoueurs(),
                        session);

                ajouterJoueurs(gameElement, enregistreur);
                ajouterEntrees(gameElement, enregistreur, nomHero);

                enregistreur.mainFinie();
                compteMains++;
            }

        }

        catch (Exception exception) {
            exceptionApparue = exception;
        }

        return importTermine(exceptionApparue, compteMains);
    }

    private float trouverMontantBB(Element gameElement) throws InformationsIncorrectes {
        Float montantBB = null;

        NodeList tourElements = gameElement.getElementsByTagName("round");
        for (int k = 0; k < tourElements.getLength(); k++) {
            Element tourElement = (Element) tourElements.item(k);
            TourMain.Round round = getTour(Integer.parseInt(tourElement.getAttribute("no")));
            NodeList actionJoueurs = tourElement.getElementsByTagName("action");

            if (round == TourMain.Round.BLINDES) {
                for (int i = 0; i < actionJoueurs.getLength(); i++) {
                    Element actionBlindes = (Element) actionJoueurs.item(i);
                    int actionType = Integer.parseInt(actionBlindes.getAttribute("type"));
                    float valeurAnte = Float.parseFloat(corrigerString(actionBlindes.getAttribute("sum")));

                    if (actionType == 2) {
                        montantBB = valeurAnte;
                    }
                }
                break;
            }
        }

        if (montantBB == null) throw new InformationsIncorrectes("Montant BB non trouvé");
        return montantBB;
    }

    private void ajouterEntrees(Element gameElement, EnregistreurMain enregistreur, String nomHero) throws InformationsIncorrectes {
        NodeList tourElements = gameElement.getElementsByTagName("round");
        for (int k = 0; k < tourElements.getLength(); k++) {
            Element tourElement = (Element) tourElements.item(k);
            TourMain.Round round = getTour(Integer.parseInt(tourElement.getAttribute("no")));
            NodeList actionJoueurs = tourElement.getElementsByTagName("action");

            HashMap<String, Float> valeursAnte = new HashMap<>();

            if (round == TourMain.Round.BLINDES) {
                for (int i = 0; i < actionJoueurs.getLength(); i++) {
                    Element actionBlindes = (Element) actionJoueurs.item(i);
                    int actionType = Integer.parseInt(actionBlindes.getAttribute("type"));
                    float valeurAnte = Float.parseFloat(corrigerString(actionBlindes.getAttribute("sum")));

                    if (actionType == 15) {
                        valeursAnte.put(actionBlindes.getAttribute("player"), valeurAnte);
                    }

                    else if (actionType == 1 || actionType == 2) {
                        String nomJoueur = actionBlindes.getAttribute("player");
                        enregistreur.ajouterBlindes(nomJoueur, valeurAnte);
                    }
                }

                if (!valeursAnte.isEmpty()) {
                    enregistreur.ajouterAntes(valeursAnte);
                }
            }

            else if (round == TourMain.Round.PREFLOP) {
                boolean showdown = false;

                NodeList carteJoueurs = tourElement.getElementsByTagName("cards");
                ComboReel comboHero = null;
                for (int l = 0; l < carteJoueurs.getLength(); l++) {
                    Element cartes = (Element) carteJoueurs.item(l);
                    String nomJoueurCarte = cartes.getAttribute("player");
                    List<Carte> cartesExtraites = convertirNomCartes(cartes.getTextContent());
                    if (!cartesExtraites.isEmpty()) {
                        ComboReel comboReel = new ComboReel(cartesExtraites);
                        if (!nomJoueurCarte.equals(nomHero)) {
                            // on n'ajoute les cartes du hero que si showdown
                            enregistreur.ajouterCartes(nomJoueurCarte, comboReel);
                            showdown = true;
                        }
                        else comboHero = comboReel;
                    }
                }
                // on ne lève pas d'exception car ça arrive première main d'un tournoi quand on vient de s'installer
                if (comboHero == null) logger.warn("Aucune carte trouvée pour hero");
                else {
                    if (showdown) {
                        enregistreur.ajouterCartes(nomHero, comboHero);
                    }
                    enregistreur.ajouterCarteHero(comboHero);
                }

                ajouterActions(actionJoueurs, enregistreur);
            }

            else {
                Element cartesBoard = (Element) tourElement.getElementsByTagName("cards").item(0);
                List<Carte> cartesExtraites = convertirNomCartes(cartesBoard.getTextContent());
                Board board = new Board(cartesExtraites);
                enregistreur.ajouterTour(round, board);
                ajouterActions(actionJoueurs, enregistreur);
            }
        }
    }

    private void ajouterActions(NodeList actionJoueurs, EnregistreurMain enregistreurMain) throws InformationsIncorrectes {
        for (int m = 0; m < actionJoueurs.getLength(); m++) {
            Element action = (Element) actionJoueurs.item(m);
            // attention il n'y a pas que des actions dans ce bloc!
            if (!Objects.equals(action.getTagName(), "action")) continue;

            DTOLecteurTxt.DetailAction descriptionAction = convertirAction(
                    action.getAttribute("player"),
                    Integer.parseInt(action.getAttribute("type")),
                    Float.parseFloat(corrigerString(action.getAttribute("sum")))
            );

            enregistreurMain.ajouterAction(
                    descriptionAction.getAction(),
                    descriptionAction.getNomJoueur(),
                    descriptionAction.getBetTotal(),
                    descriptionAction.getBetComplet());
        }
    }

    private DTOLecteurTxt.DetailAction convertirAction(String player, int idActionIPoker, float montantBet) {
        boolean totalBet = false;
        boolean betComplet = true;
        Action action = new Action();
        action.setBetSize(montantBet);

        switch (idActionIPoker) {
            case 0:
                action.setMove(Move.FOLD);
                break;
            case 3:
            case 4:
            case 7:
                action.setMove(Move.CALL);
                break;
            case 5:
            case 23:
                action.setMove(Move.RAISE);
                totalBet = true;
                break;
            default:
                throw new IllegalArgumentException("Action non reconnue d'index : " + idActionIPoker);

        }
        return new DTOLecteurTxt.DetailAction(player, action, totalBet, betComplet);
    }


    private List<Carte> convertirNomCartes(String nomCartes) {
        String[] splittedCards = nomCartes.split("\\s+");
        List<Carte> cartes = new ArrayList<>();

        for (String splittedCard : splittedCards) {
            String strCards = splittedCard.trim();
            strCards = strCards.replace("10", "T");

            if ("X".equals(strCards)) {
                return new ArrayList<>();
            }
            cartes.add(new Carte(strCards.charAt(1), Character.toLowerCase(strCards.charAt(0))));
        }

        return cartes;
    }

    private TourMain.Round getTour(int nTour) {
        return switch (nTour) {
            case 0 -> TourMain.Round.BLINDES;
            case 1 -> TourMain.Round.PREFLOP;
            case 2 -> TourMain.Round.FLOP;
            case 3 -> TourMain.Round.TURN;
            case 4 -> TourMain.Round.RIVER;
            default -> throw new IllegalArgumentException("Round inconnu d'index : " + nTour);
        };
    }

    private void ajouterJoueurs(Element gameElement, EnregistreurMain enregistreurMain) {
        NodeList playerElements = gameElement.getElementsByTagName("player");
        for (int j = 0; j < playerElements.getLength(); j++) {
            Element joueurElement = (Element) playerElements.item(j);
            String nomJoueur = joueurElement.getAttribute("name");
            int siege = Integer.parseInt(joueurElement.getAttribute("seat"));
            float stack = Float.parseFloat(corrigerString(joueurElement.getAttribute("chips")));
            float gains = Float.parseFloat(corrigerString(joueurElement.getAttribute("win")));
            float bounty = 0f;

            enregistreurMain.ajouterJoueur(nomJoueur, siege, stack, bounty);
            enregistreurMain.ajouterGains(nomJoueur, gains);
        }
    }

    @Override
    public boolean fichierEstValide() {
        boolean correspond = nomFichier.matches("\\d{5,}.xml");

        if (correspond) {
            logger.trace("Format nom de fichier reconnu : " + nomFichier);
            return true;
        } else {
            logger.trace("Fichier non valide : " + nomFichier);
            return false;
        }
    }

    private Partie getPartie() throws ErreurImportation {
        try (InputStream streamFichier = Files.newInputStream(cheminDuFichier)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.document = builder.parse(streamFichier);
            //s'assurer de la bonne structure du fichier
            this.document.getDocumentElement().normalize();

            if (!creerVariante()) return null;

            return creerPartie();
        }
        catch (IOException | ParserConfigurationException | SAXException e) {
            throw new FichierCorrompu("Problème lors de la lecture du fichier XML");
        }


    }

    private Partie creerPartie() throws InformationsIncorrectes {
        long idTournoi;
        String nomHero;
        String nomPartie;
        LocalDateTime dateTournoi;

        Element generalElement = (Element) document.getElementsByTagName("general").item(0);

        String nomIdPartie = generalElement.getElementsByTagName("tablename").item(0).getTextContent();

        Pattern regexIdNom = Pattern.compile("(?<nomPartie>.+),\\s(?<idTournoi>\\d+)");
        Matcher matcher = regexIdNom.matcher(nomIdPartie);
        if (!(matcher.find())) throw new InformationsIncorrectes("Nom et id de partie non trouvé");
        idTournoi = Long.parseLong(matcher.group("idTournoi"));
        nomPartie = matcher.group("nomPartie");

        nomHero = generalElement.getElementsByTagName("nickname").item(0).getTextContent();


        String dateString = generalElement.getElementsByTagName("startdate").item(0).getTextContent();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTournoi = LocalDateTime.parse(dateString, formatter);

        Partie partie = new Partie(this.variante, PokerRoom.IPOKER, idTournoi, nomHero, nomPartie, dateTournoi);
        session.persist(partie);

        return partie;
    }

    private boolean creerVariante() throws ErreurImportation {
        Variante.VariantePoker variantePoker;
        Variante.PokerFormat pokerFormat;
        int nombreJoueurs;
        float buyIn;
        float ante = 0f;
        float rake = 0f;
        boolean ko = false;

        Element generalElement = (Element) document.getElementsByTagName("general").item(0);

        // on vérifie qu'on a du holdem no limit
        String gameType = generalElement.getElementsByTagName("gametype").item(0).getTextContent();
        if (gameType.contains("Holdem NL")) {
            variantePoker = Variante.VariantePoker.HOLDEM_NO_LIMIT;
        }
        else throw new FormatNonPrisEnCharge("Holdem no limit non détecté");

        // todo vérifier ces formats
        // on trouve le type de partie + ko pour MTT
        NodeList tournamentName = generalElement.getElementsByTagName("tournamentname");
        if (tournamentName.getLength() > 0) {
            String nomTournoi = tournamentName.item(0).getTextContent();
            if (nomTournoi.contains("Twister")) {
                pokerFormat = Variante.PokerFormat.SPIN;
            }
            // attention il y aussi les sit'n'go
            // todo vérifier un jour que c'est bien ça qui apparaît
            else if (nomTournoi.contains("Sit'n'Go")) {
                throw new FormatNonPrisEnCharge("Sit'N'Go non twister");
            }
            else {
                pokerFormat = Variante.PokerFormat.MTT;
                if (nomTournoi.contains("KO")) ko = true;
            }
        }

        else {
            pokerFormat = Variante.PokerFormat.CASH_GAME;
        }

        // on récupère le nombre de joueurs
        nombreJoueurs = Integer.parseInt(
                generalElement.getElementsByTagName("tablesize").item(0).getTextContent());

        // on trouve le buy-in
        buyIn = recupererBuyIn(generalElement);

        // si MTT on récupère les ante
        if (pokerFormat == Variante.PokerFormat.MTT) {
            ante = recupererAnte();
        }

        else if (pokerFormat == Variante.PokerFormat.CASH_GAME) {
            rake = RAKE_BETCLIC;
        }

        this.variante = ObjetUnique.variante(variantePoker, pokerFormat, buyIn, nombreJoueurs, ante, rake, ko);

        return true;
    }

    private float recupererAnte() {
        return ANTE_BETCLIC;
    }

    private float recupererBuyIn(Element generalElement) throws InformationsIncorrectes {
        String buyInStr;
        NodeList totalBuyIn = generalElement.getElementsByTagName("totalbuyin");
        NodeList bigBlind = generalElement.getElementsByTagName("bigblind");
        if (totalBuyIn.getLength() > 0) {
            buyInStr = totalBuyIn.item(0).getTextContent();
        }
        else if (bigBlind.getLength() > 0) {
            buyInStr = bigBlind.item(0).getTextContent();
        }
        else throw new InformationsIncorrectes("Le buy n'a pas pu être récupéré");

        return Float.parseFloat(corrigerString(buyInStr));
    }

    // retire les €, les espaces et remplace les virgules par des points
    private String corrigerString(String stringOriginale) {
        return stringOriginale.replace(",", ".").replaceAll("[^\\d.]", "");
    }
}
