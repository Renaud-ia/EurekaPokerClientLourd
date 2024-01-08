package analyzor.modele.extraction.winamax;

import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.extraction.DTOLecteurTxt;
import analyzor.modele.extraction.EnregistreurPartie;
import analyzor.modele.extraction.InterpreteurPartie;
import analyzor.modele.extraction.LecteurPartie;
import analyzor.modele.parties.Partie;
import analyzor.modele.parties.PokerRoom;
import analyzor.modele.bdd.ConnexionBDD;
import analyzor.modele.parties.Variante;
import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboReel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LecteurWinamax implements LecteurPartie {
    private final Logger logger = LogManager.getLogger(LecteurWinamax.class);
    private final Path cheminDuFichier;
    private final String nomFichier;
    private Variante variante;
    private Session session;

    // patterns regex
    private static final Pattern patternInfos = Pattern.compile(
            "Winamax Poker - Tournament summary : (?<nomTournoi>.+?)\\((?<idTournoi>\\d+)\\)");
    private static final Pattern patternHero = Pattern.compile("Player : (?<nomJoueur>.+)");
    private static final Pattern patternBI = Pattern.compile(
            "Buy-In : ([0-9]+(?:\\.[0-9]+)?)\\u20AC \\+ ([0-9]+(?:\\.[0-9]+)?)\\u20AC");
    private static final Pattern patternFormat = Pattern.compile("Mode : (?<format>.+)");
    private static final Pattern patternVitesse = Pattern.compile("Speed : (?<vitesse>.+)");
    private static final Pattern patternAnte = Pattern.compile("^.*?\\[\\d+-(?<bb>\\d+):(?<ante>\\d+)");
    private static final Pattern patternDate = Pattern.compile(
            "^Tournament started (\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}) UTC$");
    private static final Pattern patternType = Pattern.compile("Type : (?<knockout>.+)");
    public LecteurWinamax(Path cheminDuFichier) {
        this.cheminDuFichier = cheminDuFichier;
        nomFichier = cheminDuFichier.getFileName().toString();
    }
    @Override
    public Integer sauvegarderPartie() {
        session = ConnexionBDD.ouvrirSession();
        Transaction transaction = session.beginTransaction();
        logger.info("Enregistrement de la partie dans la BDD : " + cheminDuFichier);
        Partie partie = creerPartie();
        if (partie == null) return null;

        boolean success = true;




        int compteMains = 0;

        //pour initialisation variante
        //todo : si fichier en 2 parties, une variante sera mal initialisée mais est-ce grave?
        int nombreJoueursVariante = 0;
        int stackDepartVariante = 0;
        boolean stackCherche = true;
        boolean nombreJoueursCherche = true;
        try (BufferedReader reader = Files.newBufferedReader(cheminDuFichier, StandardCharsets.UTF_8)) {
            String ligne;

            // todo : en changeant ça, on obtient un lecteur générique de .txt
            InterpreteurPartie interpreteur = new InterpreteurPartieWinamax();
            RegexPartieWinamax regexPartie = new RegexPartieWinamax();

            EnregistreurPartie enregistreur;
            long idMain;
            int montantBB;
            Board board;
            Map<String, Integer> antesJoueur = new HashMap<>();
            DTOLecteurTxt.SituationJoueur situationJoueur;
            DTOLecteurTxt.DetailAction detailAction;
            DTOLecteurTxt.DetailGain detailGain;
            DTOLecteurTxt.StructureBlinde structureBlinde = new DTOLecteurTxt.StructureBlinde();

            // on lit la première ligne en amont (important car on termine le tour dans l'enregistreur si nouveau tour)
            ligne = reader.readLine();
            idMain = regexPartie.trouverIdMain(ligne);
            montantBB = regexPartie.trouverMontantBB(ligne);

            enregistreur = new EnregistreurPartie(idMain,
                    montantBB,
                    partie,
                    partie.getNomHero(),
                    PokerRoom.WINAMAX,
                    session);

            //on lit les lignes suivantes
            try {
                while ((ligne = reader.readLine()) != null) {

                    logger.trace("Ligne lue : " + ligne);
                    interpreteur.lireLigne(ligne);

                    if (interpreteur.nouvelleMain()) {
                        logger.trace("Ligne nouvelle main détectée : " + ligne);
                        // on remet à zéro les compteurs
                        structureBlinde = new DTOLecteurTxt.StructureBlinde();
                        antesJoueur = new HashMap<>();
                        // on termine la main en cours
                        enregistreur.mainFinie();
                        compteMains++;

                        idMain = regexPartie.trouverIdMain(ligne);
                        montantBB = regexPartie.trouverMontantBB(ligne);

                        enregistreur = new EnregistreurPartie(idMain,
                                montantBB,
                                partie,
                                partie.getNomHero(),
                                PokerRoom.WINAMAX,
                                session);
                    }

                    else if (interpreteur.joueurCherche()) {
                        logger.trace("Ligne joueur détecté : " + ligne);
                        situationJoueur = regexPartie.trouverInfosJoueur(ligne);
                        enregistreur.ajouterJoueur(
                                situationJoueur.getNomJoueur(),
                                situationJoueur.getSiege(),
                                situationJoueur.getStack(),
                                situationJoueur.getBounty()
                        );
                        if (stackCherche) {
                            stackDepartVariante = situationJoueur.getStack();
                            stackCherche = false;
                        }
                        if (nombreJoueursCherche) {
                            nombreJoueursVariante++;
                        }
                    }

                    else if (interpreteur.cartesHeroCherchees()) {
                        ComboReel comboHero = regexPartie.cartesHero(ligne);
                        enregistreur.ajouterCarteHero(comboHero);
                    }

                    else if (interpreteur.nouveauTour()) {
                        nombreJoueursCherche = false;
                        logger.trace("Ligne nouveau tour détectée : " + ligne);
                        if (interpreteur.pasPreflop()) {
                            board = regexPartie.trouverBoard(ligne);
                            enregistreur.ajouterTour(interpreteur.nomTour(), board);
                        }
                        //si préflop, on enregistre les antes et blindes
                        else {
                            enregistreur.ajouterBlindes(
                                    structureBlinde.getJoueurBB(),
                                    structureBlinde.getJoueurSB()
                            );
                            enregistreur.ajouterAntes(antesJoueur);
                        }
                    }

                    else if (interpreteur.blindesAntesCherchees()) {
                        logger.trace("Ligne blindes/ante détectée : " + ligne);
                        regexPartie.trouverBlindesAntes(ligne, structureBlinde, antesJoueur);
                    }

                    else if (interpreteur.actionCherchee()) {
                        logger.trace("Ligne action détectée : " + ligne);
                        detailAction = regexPartie.trouverAction(ligne);
                        enregistreur.ajouterAction(
                                detailAction.getAction(),
                                detailAction.getNomJoueur(),
                                detailAction.getBetTotal(),
                                detailAction.getBetComplet());
                    }

                    /*
                    todo : inutile cf Enregistreur
                    else if (interpreteur.showdownTrouve()) {
                        enregistreur.ajouterShowdown(true);
                    }
                    */

                    else if (interpreteur.gainCherche()) {
                        logger.trace("Ligne gain détectée : " + ligne);
                        detailGain = regexPartie.trouverGain(ligne);
                        enregistreur.ajouterGains(
                                detailGain.getNomJoueur(),
                                detailGain.getGains()
                        );
                        if (detailGain.cartesTrouvees()) {
                            enregistreur.ajouterCartes(detailGain.getNomJoueur(), detailGain.getCombo());
                        }
                    }

                }
                enregistreur.mainFinie();

            }
            catch (Exception e) {
                //todo : lever une nouvelle exception captée par le worker
                logger.error("Problème de lecture du fichier : " + cheminDuFichier, e);
                success=false;
            }


        }
        catch (IOException e) {
            //todo : lever une nouvelle exception captée par le worker
            logger.error("Impossible d'ouvrir le fichier de la partie : " + cheminDuFichier, e);
            return 0;
        }

        if (success) {
            partie.setNombreJoueurs(nombreJoueursVariante);
            transaction.commit();
        }
        else {
            transaction.rollback();
            ConnexionBDD.fermerSession(session);
            return null;
        }
        ConnexionBDD.fermerSession(session);

        return compteMains;
    }



    private Partie creerPartie() {
        //todo : restructurer comme sauvegarderPartie()
        String baseNom = cheminDuFichier.toString().replace(".txt", "");
        Path fichierSummary = Paths.get(baseNom + "_summary.txt");

        Variante.PokerFormat pokerFormat = Variante.PokerFormat.INCONNU;
        Variante.Vitesse vitesse = Variante.Vitesse.INCONNU;
        float antePourcent = 0.0f;
        boolean ko = false;
        float buyIn = 0.0f;
        String nomHero = null;
        String nomPartie = null;
        Integer idTournoi = null;
        LocalDateTime dateTournoi = null;

        try (BufferedReader reader = Files.newBufferedReader(fichierSummary, StandardCharsets.UTF_8)){
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    if (line.startsWith("Winamax Poker")) {
                        Matcher matcher = patternInfos.matcher(line);
                        matcher.find();
                        nomPartie = matcher.group("nomTournoi");
                        idTournoi = Integer.parseInt(matcher.group("idTournoi"));

                    }
                    else if (line.startsWith("Player")) {
                        Matcher matcher = patternHero.matcher(line);
                        matcher.find();
                        nomHero = matcher.group("nomJoueur");
                    }
                    else if (line.startsWith("Mode")) {
                        Matcher matcher = patternFormat.matcher(line);
                        matcher.find();
                        switch (matcher.group("format")) {
                            case "tt":
                                pokerFormat = Variante.PokerFormat.MTT;
                                break;
                            case "sng":
                                assert nomPartie != null;
                                if (nomPartie.startsWith("Expresso")) pokerFormat = Variante.PokerFormat.SPIN;
                                break;

                            //todo rajouter les autres cas (Nitro, Cash Game etc)
                            default:
                                logger.warn("Format de tournoi inconnu");
                                break;

                        }
                    }
                    else if (line.startsWith("Speed")) {
                        Matcher matcher = patternVitesse.matcher(line);
                        matcher.find();
                        switch (matcher.group("vitesse")) {
                            case "turbo":
                                vitesse = Variante.Vitesse.TURBO;
                                break;
                            case "normal":
                                vitesse = Variante.Vitesse.NORMALE;
                                break;
                            case "semiturbo":
                                vitesse = Variante.Vitesse.SEMI_TURBO;
                                break;


                            //todo rajouter les autres cas ()
                            default:
                                logger.warn("Vitesse de tournoi inconnu");
                        }
                    }
                    else if (line.startsWith("Buy-In")) {
                        Matcher matcher = patternBI.matcher(line);
                        matcher.find();
                        float entree = Float.parseFloat(matcher.group(1));
                        float taxe = Float.parseFloat(matcher.group(2));

                        buyIn = entree + taxe;
                    }
                    else if (line.startsWith("Levels")) {
                        Matcher matcher = patternAnte.matcher(line);
                        matcher.find();
                        int valeurBB = Integer.parseInt(matcher.group("bb"));
                        int valeurAnte = Integer.parseInt(matcher.group("ante"));

                        antePourcent = (float) (valeurAnte * 100) / valeurBB;
                    }
                    else if (line.startsWith("Tournament started")) {
                        Matcher matcher = patternDate.matcher(line);
                        matcher.find();
                        String dateTimeStr = matcher.group(1);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                        dateTournoi = LocalDateTime.parse(dateTimeStr, formatter);
                    }
                    else if (line.startsWith("Type")) {
                        Matcher matcher = patternType.matcher(line);
                        matcher.find();
                        if (Objects.equals(matcher.group(0), "knockout")) ko = true;
                    }
                }
                catch (IllegalStateException e) {
                    logger.error("Problème de match : " + fichierSummary, e);
                }
            }


        }
        catch (IOException e) {
            logger.error("Impossible d'ouvrir le fichier summary : " + fichierSummary, e);
            return null;
        }

       assert dateTournoi != null;

        this.variante = ObjetUnique.variante(PokerRoom.WINAMAX, pokerFormat, vitesse, antePourcent, ko);

        Partie partie = new Partie(this.variante, idTournoi, buyIn, nomHero, nomPartie, dateTournoi);
        session.persist(partie);
        variante.getParties().add(partie);
        session.merge(variante);

        return partie;
    }

    @Override
    public boolean fichierEstValide() {
        // on prend les summary comme non valides → sinon ils seront comptés par le gestionnaire
        boolean correspond = nomFichier.matches("^[0-9]{8}_.+real_holdem_no-limit\\.txt$");

        if (correspond) {
            logger.trace("Format nom de fichier reconnu : " + nomFichier);
            return true;
        } else {
            logger.trace("Fichier non valide : " + nomFichier);
            return false;
        }

    }

}
