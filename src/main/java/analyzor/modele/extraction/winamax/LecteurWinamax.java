package analyzor.modele.extraction.winamax;

import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.extraction.DTOLecteurTxt;
import analyzor.modele.extraction.EnregistreurMain;
import analyzor.modele.extraction.InterpreteurPartie;
import analyzor.modele.extraction.LecteurPartie;
import analyzor.modele.extraction.exceptions.ErreurImportation;
import analyzor.modele.extraction.exceptions.FormatNonPrisEnCharge;
import analyzor.modele.parties.Partie;
import analyzor.modele.parties.PokerRoom;
import analyzor.modele.parties.Variante;
import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboReel;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class LecteurWinamax extends LecteurPartie {
    private final InterpreteurPartie interpreteur;
    private final RegexPartieWinamax regexPartie;
    private int nMainsImportees;

    public LecteurWinamax(Path cheminDuFichier) {
        super(cheminDuFichier, PokerRoom.WINAMAX);
        interpreteur = new InterpreteurPartieWinamax();
        regexPartie = new RegexPartieWinamax();
        nMainsImportees = 0;
    }


    @Override
    public Integer sauvegarderPartie() {
        super.ouvrirTransaction();
        logger.info("Enregistrement de la partie dans la BDD : " + cheminDuFichier);

        Exception exceptionApparue = null;

        Partie partie;
        // on ouvre deux fois le même fichier mais pas trop le choix car on peut pa
        try (BufferedReader reader = Files.newBufferedReader(cheminDuFichier, StandardCharsets.UTF_8)) {
            partie = creerPartie(reader);
        } catch (Exception e) {
            return importTermine(e, 0);
        }

        try (BufferedReader reader = Files.newBufferedReader(cheminDuFichier, StandardCharsets.UTF_8)) {
            importerLesMains(reader, partie);
        } catch (Exception e) {
            exceptionApparue = e;
        }

        return importTermine(exceptionApparue, nMainsImportees);
    }

    // import des mains

    private void importerLesMains(BufferedReader reader, Partie partie) throws ErreurImportation, IOException {
        // todo séparer en petites fonctions ce bloc ?
        String ligne;

        long idMain;
        float montantBB;
        Board board;
        DTOLecteurTxt.SituationJoueur situationJoueur;
        DTOLecteurTxt.DetailAction detailAction;
        DTOLecteurTxt.DetailGain detailGain;

        // on lit la première ligne en amont (important car on termine le tour dans l'enregistreur si nouveau tour)
        ligne = reader.readLine();
        idMain = regexPartie.trouverIdMain(ligne);
        montantBB = regexPartie.trouverMontantBB(ligne);

        EnregistreurMain enregistreur = new EnregistreurMain(idMain,
                montantBB,
                partie,
                variante.getBuyIn(),
                partie.getNomHero(),
                PokerRoom.WINAMAX,
                variante.hasBounty(),
                variante.getNombreJoueurs(),
                session);

        //on lit les lignes suivantes
        while ((ligne = reader.readLine()) != null) {
            interpreteur.lireLigne(ligne);

            if (interpreteur.nouvelleMain()) {
                // on termine la main en cours
                enregistreur.mainFinie();
                nMainsImportees++;

                idMain = regexPartie.trouverIdMain(ligne);
                montantBB = regexPartie.trouverMontantBB(ligne);

                enregistreur = new EnregistreurMain(idMain,
                        montantBB,
                        partie,
                        variante.getBuyIn(),
                        partie.getNomHero(),
                        PokerRoom.WINAMAX,
                        variante.hasBounty(),
                        variante.getNombreJoueurs(),
                        session);
            }

            else if (interpreteur.joueurCherche()) {
                situationJoueur = regexPartie.trouverInfosJoueur(ligne);
                enregistreur.ajouterJoueur(
                        situationJoueur.getNomJoueur(),
                        situationJoueur.getSiege(),
                        situationJoueur.getStack(),
                        situationJoueur.getBounty()
                );
            }

            else if (interpreteur.cartesHeroCherchees()) {
                ComboReel comboHero = regexPartie.cartesHero(ligne);
                enregistreur.ajouterCarteHero(comboHero);
            }

            else if (interpreteur.nouveauTour()) {
                if (interpreteur.pasPreflop()) {
                    board = regexPartie.trouverBoard(ligne);
                    enregistreur.ajouterTour(interpreteur.nomTour(), board);
                }
            }

            else if (interpreteur.blindesAntesCherchees()) {
                DTOLecteurTxt.BlindesAnte blindesAnte = regexPartie.trouverBlindesAnte(ligne);
                if (blindesAnte.estAnte()) {
                    enregistreur.ajouterAnte(blindesAnte.getNomJoueur(), blindesAnte.getValeur());
                }

                else if (blindesAnte.estBlinde()) {
                    enregistreur.ajouterBlindes(blindesAnte.getNomJoueur(), blindesAnte.getValeur());
                }
            }

            else if (interpreteur.actionCherchee()) {
                detailAction = regexPartie.trouverAction(ligne);
                enregistreur.ajouterAction(
                        detailAction.getAction(),
                        detailAction.getNomJoueur(),
                        detailAction.getBetTotal(),
                        detailAction.getBetComplet());
            }

            else if (interpreteur.potTrouveCashGame()) {
                enregistreur.ajouterRake(regexPartie.trouverRakeTotal(ligne));
            }


            else if (interpreteur.gainCherche()) {
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
        nMainsImportees++;
    }


    // récupération des infos générales sur la partie


    private Partie creerPartie(BufferedReader reader) throws IOException, ErreurImportation {
        DTOLecteurWinamax.InfosPartie infosPartie = new DTOLecteurWinamax.InfosPartie();
        trouverInfosPartie(reader, infosPartie);

        if (!infosPartie.formatPrisEnCharge()) throw new FormatNonPrisEnCharge(infosPartie.getNomFormat());

        this.variante = ObjetUnique.variante(
                infosPartie.getVariantePoker(),
                infosPartie.getFormatPoker(),
                infosPartie.getBuyIn(),
                infosPartie.getNombreJoueurs(),
                infosPartie.getAnte(),
                infosPartie.getRake(),
                infosPartie.getBounty());

        Partie partie = new Partie(
                this.variante,
                PokerRoom.WINAMAX,
                infosPartie.getNumeroTable(),
                infosPartie.getNomHero(),
                infosPartie.getNomTable(),
                infosPartie.getDate());

        session.persist(partie);

        return partie;
    }

    private void trouverInfosPartie(BufferedReader reader, DTOLecteurWinamax.InfosPartie infosPartie)
            throws IOException, ErreurImportation {
        Boolean bounty = null;
        String nomHero = null;

        String ligne;
        while ((ligne = reader.readLine()) != null) {
            interpreteur.lireLigne(ligne);

            if (interpreteur.nouvelleMain()) {
                // première ligne on vérifie que c'est du holdem, on récupère le format,
                // le buy in, la date, le numéro de table, le numéro de main + ante si MTT
                regexPartie.infosPremiereLigne(ligne, infosPartie);
            }

            else if (interpreteur.infosTable()) {
                // seconde ligne, on récupère le nom de la table, le nombre de joueurs
                regexPartie.infosTable(ligne, infosPartie);
            }

            else if (interpreteur.joueurCherche() && bounty == null) {
                // on regarde si on peut récupérer le bounty du joueur, ne sera pas le cas avec l'ancien encodage
                DTOLecteurTxt.SituationJoueur situationJoueur = regexPartie.trouverInfosJoueur(ligne);
                bounty = situationJoueur.hasBounty();
                infosPartie.setBounty(bounty);
            }

            else if (interpreteur.cartesHeroCherchees()) {
                // recupérer nom héro
                nomHero = regexPartie.nomHero(ligne);
                infosPartie.setNomHero(nomHero);

                // sauf pour cash game normalement on a tout trouvé
                if (infosPartie.getFormatPoker() != Variante.PokerFormat.CASH_GAME) {
                    break;
                }
            }

            else if (interpreteur.potTrouveCashGame()) {
                infosPartie.setRake(regexPartie.trouverRake(ligne));

                // parfois le nom du hero n'est pas dans la première main car cartes servies à la deuxième
                if (nomHero != null) break;

            }
        }
    }


    @Override
    public boolean fichierEstValide() {
        // on prend les summary comme non valides → sinon ils seront comptés par le gestionnaire

        return nomFichier.matches("^[0-9]{8}_.+real_holdem_no-limit\\.txt$");

    }
}
