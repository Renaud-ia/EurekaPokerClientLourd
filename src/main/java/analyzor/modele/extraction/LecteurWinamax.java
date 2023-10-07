package analyzor.modele.extraction;

import analyzor.modele.logging.GestionnaireLog;
import analyzor.modele.parties.Partie;
import analyzor.modele.parties.PokerRoom;
import analyzor.modele.parties.RequetesBDD;
import analyzor.modele.parties.Variante;
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
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class LecteurWinamax implements LecteurPartie {
    private Logger logger = GestionnaireLog.getLogger("LecteurWinamax");
    private final Path cheminDuFichier;
    private final String nomFichier;
    public LecteurWinamax(Path cheminDuFichier) {
        this.cheminDuFichier = cheminDuFichier;
        nomFichier = cheminDuFichier.getFileName().toString();
        GestionnaireLog.setHandler(logger, GestionnaireLog.importWinamax);
    }
    @Override
    public Integer sauvegarderPartie() {
        logger.fine("Enregistrement de la partie dans la BDD");
        Partie partie = creerPartie();
        if (partie == null) return null;

        return 0;
    }

    private Partie creerPartie() {
        String baseNom = cheminDuFichier.toString().replace(".txt", "");
        Path fichierSummary = Paths.get(baseNom + "_summary.txt");

        Pattern patternInfos = Pattern.compile(
                "Winamax Poker - Tournament summary : (?<nomTournoi>.+?)\\((?<idTournoi>\\d+)\\)");
        Pattern patternHero = Pattern.compile("Player : (?<nomJoueur>.+)");
        Pattern patternBI = Pattern.compile(
                "Buy-In : ([0-9]+(?:\\.[0-9]+)?)[\\u20AC] \\+ ([0-9]+(?:\\.[0-9]+)?)[\\u20AC]");
        Pattern patternFormat = Pattern.compile("Mode : (?<format>.+)");
        Pattern patternVitesse = Pattern.compile("Speed : (?<vitesse>.+)");
        Pattern patternAnte = Pattern.compile("^.*?\\[\\d+-(?<bb>\\d+):(?<ante>\\d+)");
        Pattern patternDate = Pattern.compile(
                "^Tournament started (\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}) UTC$");
        Pattern patternType = Pattern.compile("Type : (?<knockout>.+)");


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
                            case "tt" -> pokerFormat = Variante.PokerFormat.MTT;
                            case "sng" -> {
                                assert nomPartie != null;
                                if (nomPartie.startsWith("Expresso")) pokerFormat = Variante.PokerFormat.SPIN;
                            }

                            //todo rajouter les autres cas (Nitro, Cash Game etc)
                            default -> logger.warning("Format de tournoi inconnu");

                        }
                    }
                    else if (line.startsWith("Speed")) {
                        Matcher matcher = patternVitesse.matcher(line);
                        matcher.find();
                        switch (matcher.group("vitesse")) {
                            case "turbo" -> vitesse = Variante.Vitesse.TURBO;
                            case "normal" -> vitesse = Variante.Vitesse.NORMALE;
                            case "semiturbo" -> vitesse = Variante.Vitesse.SEMI_TURBO;


                            //todo rajouter les autres cas ()
                            default -> logger.warning("Vitesse de tournoi inconnu");
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
                    else if (line.startsWith("Tournament Started")) {
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
                    logger.log(Level.WARNING, "Problème de match : " + fichierSummary, e);
                    logger.warning("Ligne concernée : " + line);
                }
            }


        }
        catch (IOException e) {
            logger.log(Level.WARNING, "Impossible d'ouvrir le fichier summary : " + fichierSummary, e);
            return null;
        }

        Variante variante = new Variante(PokerRoom.WINAMAX, pokerFormat, vitesse, antePourcent, ko);
        Variante varianteObtenue = (Variante) RequetesBDD.getOrCreate(variante);

        assert varianteObtenue != null;
        logger.info("Id de l'objet variante : " + varianteObtenue.getId());

        Partie partie = new Partie(varianteObtenue, idTournoi, buyIn, nomHero, nomPartie, dateTournoi);

        return (Partie) RequetesBDD.getOrCreate(partie);
    }

    @Override
    public boolean fichierEstValide() {
        // on prend les summary comme non valides → sinon ils seront comptés par le gestionnaire
        boolean correspond = nomFichier.matches("^[0-9]{8}_.+real_holdem_no-limit\\.txt$");

        if (correspond) {
            logger.fine("Format nom de fichier reconnu : " + nomFichier);
            return true;
        } else {
            logger.fine("Fichier non valide : " + nomFichier);
            return false;
        }

    }

}
