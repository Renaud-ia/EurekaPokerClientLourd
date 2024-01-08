package analyzor.modele.extraction.ipoker;

import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.extraction.DTOLecteurTxt;
import analyzor.modele.extraction.EnregistreurPartie;
import analyzor.modele.extraction.LecteurPartie;
import analyzor.modele.parties.*;
import analyzor.modele.poker.Board;
import analyzor.modele.poker.Carte;
import analyzor.modele.poker.ComboReel;
import analyzor.modele.bdd.ConnexionBDD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LecteurIPoker implements LecteurPartie {
    private final Logger logger = LogManager.getLogger(LecteurIPoker.class);
    private final Path cheminDuFichier;
    private final String nomFichier;
    private Variante variante;
    private Document document;
    public LecteurIPoker(Path cheminDuFichier) {
        this.cheminDuFichier = cheminDuFichier;
        this.nomFichier = cheminDuFichier.getFileName().toString();
    }

    @Override
    public Integer sauvegarderPartie() {
        logger.info("Sauvegarde en cours dans la BDD pour : " + cheminDuFichier.toString());
        Partie partie = getPartie();
        if (partie == null) return null;

        boolean success = true;
        int compteMains = 0;


        Session session = ConnexionBDD.ouvrirSession();
        Transaction transaction = session.beginTransaction();

        Element generalElement = (Element) document.getElementsByTagName("general").item(0);
        String nomHero = generalElement.getElementsByTagName("nickname").item(0).getTextContent();

        try {
            NodeList gameElements = document.getElementsByTagName("game");
            for (int i = 0; i < gameElements.getLength(); i++) {
                Element gameElement = (Element) gameElements.item(i);

                long idMain = Long.parseLong(gameElement.getAttribute("gamecode"));
                int montantBB = Integer.parseInt(
                        gameElement.getElementsByTagName("bigblind").item(0).getTextContent());

                EnregistreurPartie enregistreur = new EnregistreurPartie(idMain,
                        montantBB,
                        partie,
                        partie.getNomHero(),
                        PokerRoom.IPOKER,
                        session);

                ajouterJoueurs(gameElement, enregistreur);
                ajouterMains(gameElement, enregistreur, nomHero);

                enregistreur.mainFinie();
                compteMains++;
            }

        }
        catch (Exception e) {
            logger.warn("Problème dans le traitement des mains", e);
            success = false;
        }


        if (success) {
            //variante.genererId();

            session.merge(partie);

            session.merge(variante);
            transaction.commit();
        }
        else {transaction.rollback();
        return null;}
        ConnexionBDD.fermerSession(session);

        return compteMains;
    }

    private void ajouterMains(Element gameElement, EnregistreurPartie enregistreur, String nomHero) {
        NodeList tourElements = gameElement.getElementsByTagName("round");
        for (int k = 0; k < tourElements.getLength(); k++) {
            Element tourElement = (Element) tourElements.item(k);
            TourMain.Round round = getTour(Integer.parseInt(tourElement.getAttribute("no")));
            NodeList actionJoueurs = tourElement.getElementsByTagName("action");

            if (round == TourMain.Round.BLINDES) {
                //TODO que se passe-t-il si un joueur ne pose pas de blindes ?
                Element actionSB = (Element) actionJoueurs.item(0);
                String joueurSB = actionSB.getAttribute("player");

                Element actionBB = (Element) actionJoueurs.item(1);
                String joueurBB = actionBB.getAttribute("player");

                enregistreur.ajouterBlindes(joueurBB, joueurSB);
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
                if (comboHero == null) throw new RuntimeException("Aucune carte trouvée pour hero");
                if (showdown) {
                    enregistreur.ajouterCartes(nomHero, comboHero);
                }
                enregistreur.ajouterCarteHero(comboHero);

                // todo : inutile cf Enregistreur
                //enregistreur.ajouterShowdown(showdown);
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

    private void ajouterActions(NodeList actionJoueurs, EnregistreurPartie enregistreurPartie) {
        for (int m = 0; m < actionJoueurs.getLength(); m++) {
            Element action = (Element) actionJoueurs.item(m);

            DTOLecteurTxt.DetailAction descriptionAction = convertirAction(
                    action.getAttribute("player"),
                    Integer.parseInt(action.getAttribute("type")),
                    Integer.parseInt(action.getAttribute("sum").replace(" ", "")));

            enregistreurPartie.ajouterAction(
                    descriptionAction.getAction(),
                    descriptionAction.getNomJoueur(),
                    descriptionAction.getBetTotal(),
                    descriptionAction.getBetComplet());
        }
    }

    private DTOLecteurTxt.DetailAction convertirAction(String player, int idActionIPoker, int montantBet) {
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

    private void ajouterJoueurs(Element gameElement, EnregistreurPartie enregistreurPartie) {
        NodeList playerElements = gameElement.getElementsByTagName("player");
        for (int j = 0; j < playerElements.getLength(); j++) {
            Element joueurElement = (Element) playerElements.item(j);
            String nomJoueur = joueurElement.getAttribute("name");
            int siege = Integer.parseInt(joueurElement.getAttribute("seat"));
            int stack = Integer.parseInt(joueurElement.getAttribute("chips").replace(" ", ""));
            int gains = Integer.parseInt(joueurElement.getAttribute("win").replace(" ", ""));
            //TODO gérer les bounty
            float bounty = 0f;

            enregistreurPartie.ajouterJoueur(nomJoueur, siege, stack, bounty);
            enregistreurPartie.ajouterGains(nomJoueur, gains);
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

    private Partie getPartie() {
        try (InputStream streamFichier = Files.newInputStream(cheminDuFichier)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.document = builder.parse(streamFichier);
            //s'assurer de la bonne structure du fichier
            this.document.getDocumentElement().normalize();
        }
        catch (Exception e) {
            logger.warn("Problème lors de la lecture du fichier XML");
            return null;
        }

        if (!creerVariante()) return null;

        return creerPartie();
    }

    private Partie creerPartie() {
        int idTournoi;
        float buyIn;
        String nomHero;
        String nomPartie;
        LocalDateTime dateTournoi;

        try {

            Element generalElement = (Element) document.getElementsByTagName("general").item(0);
            idTournoi = Integer.parseInt(
                    generalElement.getElementsByTagName("tournamentcode").item(0).getTextContent());

            String buyInString = generalElement.getElementsByTagName("totalbuyin").item(0).getTextContent();
            buyInString = buyInString.replaceAll("[^0-9.]", "");
            buyIn = Float.parseFloat(buyInString);

            nomHero = generalElement.getElementsByTagName("nickname").item(0).getTextContent();
            nomPartie = generalElement.getElementsByTagName("tournamentname").item(0).getTextContent();

            String dateString = generalElement.getElementsByTagName("startdate").item(0).getTextContent();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dateTournoi = LocalDateTime.parse(dateString, formatter);

            Partie partie = new Partie(this.variante, idTournoi, buyIn, nomHero, nomPartie, dateTournoi);
            variante.getParties().add(partie);
            return partie;
        }
        catch (Exception e) {
            logger.warn("Problème de récupération des données de la Partie");
            return null;
        }
    }

    private boolean creerVariante() {
        Variante.PokerFormat pokerFormat;
        Variante.Vitesse vitesse;
        int stackDepart;
        int nombreJoueurs;
        //TODO : où ça se trouve??
        float ante = 0f;
        boolean ko = false;

        try {

            Element generalElement = (Element) document.getElementsByTagName("general").item(0);
            String nomTable = generalElement.getElementsByTagName("tablename").item(0).getTextContent();

            if (nomTable.contains("Twister")) {
                pokerFormat = Variante.PokerFormat.SPIN;
                vitesse = Variante.Vitesse.TURBO;
            }
            //TODO : gérer les autres cas (MTT/CASH GAME/ETC..)
            else {
                pokerFormat = Variante.PokerFormat.INCONNU;
                vitesse = Variante.Vitesse.INCONNU;
            }

            nombreJoueurs = Integer.parseInt(
                    generalElement.getElementsByTagName("tablesize").item(0).getTextContent());

            Element gameElement = (Element) document.getElementsByTagName("game").item(0);
            Element playersElement = (Element) gameElement.getElementsByTagName("players").item(0);
            Element firstPlayerElement = (Element) playersElement.getElementsByTagName("player").item(0);
            stackDepart = Integer.parseInt(firstPlayerElement.getAttribute("chips"));
            this.variante = ObjetUnique.variante(PokerRoom.IPOKER, pokerFormat, vitesse, ante, ko);
        }
        catch (Exception e) {
            logger.warn("Problème de récupération des données de la Variante");
            return false;
        }

        return true;
    }
}
