package analyzor.modele.extraction;

import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.config.ValeursConfig;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.extraction.exceptions.ErreurImportation;
import analyzor.modele.extraction.exceptions.InformationsIncorrectes;
import analyzor.modele.parties.*;
import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboReel;
import analyzor.modele.simulation.TablePoker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.formula.functions.T;
import org.hibernate.Session;

import java.util.*;


public class EnregistreurPartie {
    private static final Logger logger = LogManager.getLogger(EnregistreurPartie.class);
    private final String nomHero;
    private final PokerRoom room;
    private final MainEnregistree mainEnregistree;
    private final List<Entree> entreesSauvegardees = new ArrayList<>();
    private TourMain tourMainActuel;
    private final TableImport tablePoker;

    private final Session session;
    private NoeudAbstrait generateurId;
    public EnregistreurPartie(long idMain,
                              int montantBB,
                              Partie partie,
                              String nomHero,
                              PokerRoom room,
                              Session session) {
        // on initialise la table en mode valeur absolue
        this.tablePoker = new TableImport(montantBB);

        //initialisation
        this.nomHero = nomHero;
        this.room = room;

        this.session = session;

        this.mainEnregistree = new MainEnregistree(
                idMain,
                montantBB,
                partie
        );
        session.merge(partie);
        session.merge(mainEnregistree);
        partie.getMains().add(mainEnregistree);
    }

    //méthodes publiques = interface

    public void ajouterJoueur(String nom, int siege, int stack, float bounty) {
        Joueur joueurBDD = ObjetUnique.joueur(nom, session);

        // on associe le hero au profil hero
        ProfilJoueur profilJoueur;
        if (nom.equals(nomHero)) profilJoueur = ObjetUnique.selectionnerHero();
        else profilJoueur = ObjetUnique.selectionnerVillain();

        joueurBDD.addProfil(profilJoueur);
        session.merge(joueurBDD);

        tablePoker.ajouterJoueur(nom, siege, stack, bounty, joueurBDD);
    }

    /**
     * méthode utilisée pour ajouter les antes
     * important : ne pas les poser nous même car les règles sont obscures
     */
    public void ajouterAntes(Map<String, Integer> antesJoueur) {
        if (antesJoueur == null) return;
        for (Map.Entry<String, Integer> entree : antesJoueur.entrySet()) {
            String nomJoueur = entree.getKey();
            int valeurAnte = entree.getValue();
            tablePoker.ajouterAnte(nomJoueur, valeurAnte);
        }
    }

    /**
     ajoute au pot les blindes et déduit les stacks
     IMPORTANT => il faut l'appeler APRES avoir rentré tous les joueurs
     prend en compte tous les formats (en théorie)
     */
    public void ajouterBlindes(String nomJoueurBB, String nomJoueurSB) {
        ajouterTour(TourMain.Round.PREFLOP, null);
        tablePoker.ajouterBlindes(nomJoueurBB, nomJoueurSB);
    }


    public void ajouterTour(TourMain.Round nomTour, Board board) {
        int nJoueursInitiaux = tablePoker.nouveauTour();

        if (tourMainActuel != null) session.merge(tourMainActuel);
        this.tourMainActuel = new TourMain(nomTour, this.mainEnregistree, board, nJoueursInitiaux);
        session.merge(tourMainActuel);
        mainEnregistree.getTours().add(tourMainActuel);

        generateurId = new NoeudAbstrait(nJoueursInitiaux, nomTour);
    }

    public void ajouterAction(Action action, String nomJoueur, boolean betTotal) {
        ajouterAction(action, nomJoueur, betTotal, false);
    }

    /**
     * montantCall = (montant de la mise plus élevée ou stack du joueur) - déjà investi par le joueur
     * bet_size = total_bet_size dans le stage
     * last_bet = montant TOTAL de la mise plus élevée
     * current_stack = min_current_stack
     */
    public void ajouterAction(Action action, String nomJoueur, boolean betTotal, boolean betComplet) {

        logger.info("Action de : " + nomJoueur + " : " + action.getBetSize());

        // uniformisation des taille de BetSize entre les différentes rooms
        //GESTION BUG WINAMAX
        if (!betComplet) {
            action.augmenterBet((int) tablePoker.dernierBet());
            betTotal = true;
        }

        float betSupplementaire = tablePoker.ajouterAction(nomJoueur, action.getMove(), action.getBetSize(), betTotal);

        //le bet est retiré du stack player après l'enregistrement du coup
        //le current pot est incrémenté après l'enregistrement du coup
        //les pots sont resets à la fin du round
        float stackEffectif = tablePoker.stackEffectif();
        float potBounty = tablePoker.getPotBounty();

        action.setPot((int) tablePoker.getPotTotal());
        generateurId.ajouterAction(action.getMove());

        TablePoker.JoueurTable joueurAction = tablePoker.selectionnerJoueur(nomJoueur);

        // on enregistre dans la BDD
        Entree nouvelleEntree = new Entree(
                tablePoker.nombreActions(),
                tourMainActuel,
                generateurId.toLong(),
                betSupplementaire / tablePoker.getMontantBB(),
                stackEffectif / tablePoker.getMontantBB(),
                joueurAction.getJoueurBDD(),
                tablePoker.getStackJoueur(nomJoueur) / tablePoker.getMontantBB(),
                tablePoker.getAncienPot() / tablePoker.getMontantBB(),
                tablePoker.getPotActuel() / tablePoker.getMontantBB(),
                potBounty
        );
        tourMainActuel.getEntrees().add(nouvelleEntree);
        entreesSauvegardees.add(nouvelleEntree);
        session.merge(nouvelleEntree);
    }

    public void ajouterGains(String nomJoueur, int gains) {
        tablePoker.ajouterGains(nomJoueur, gains);
        logger.info("Gains ajoutés pour" + nomJoueur + " : " + gains);
    }

    public void ajouterCartes(String nomJoueur, ComboReel combo) {
        tablePoker.ajouterCartes(nomJoueur, combo.toInt());
        logger.info("Cartes ajoutés pour" + nomJoueur + " : " + combo);
    }

    // procédure séparée car sinon c'est le bordel car IPoker détecte toujours les cartes Hero
    public void ajouterCarteHero(ComboReel combo) {
        mainEnregistree.setCartesHero(combo.toInt());
    }

    public void mainFinie() throws ErreurImportation {
        enregistrerGains();
        session.merge(mainEnregistree);
    }

    //méthodes privées

    private void enregistrerGains() throws ErreurImportation {
        corrigerGains();

        List<Float> resultats = new ArrayList<>();

        for (TablePoker.JoueurTable joueurTraite : tablePoker.getJoueurs()) {
            logger.trace("Calcul de la value pour : " + joueurTraite.getNom());
            int gains = joueurTraite.gains();
            int depense = (int) joueurTraite.totalInvesti();

            //on ne peut pas perdre plus que la plus grosse mise adverse
            int maxPlusGrosBet = 0;
            if (gains == 0) {
                for (TablePoker.JoueurTable joueur : tablePoker.getJoueurs()) {
                    if (joueur != joueurTraite) {
                        if (joueur.totalInvesti() > maxPlusGrosBet) {
                            maxPlusGrosBet = (int) joueur.totalInvesti();
                        }
                    }
                }
                depense = Math.min(depense, maxPlusGrosBet);
            }

            // important, il faut ajouter les ante après car certains la posent d'autres non
            depense += (int) joueurTraite.anteInvestie();

            float resultatNet = gains - depense;
            resultats.add(resultatNet);
            logger.trace("Gain total pour " + joueurTraite.getNom() + " : " + gains);
            logger.trace("Depense pour " + joueurTraite.getNom() + " : " + depense);
            logger.trace("Gain net pour " + joueurTraite.getNom() + " : " + resultatNet);

            if (joueurTraite.nActions() == 0) {
                logger.trace("Aucune action du joueur " + joueurTraite.getNom() + ", value : " + resultatNet);
                GainSansAction gainSansAction = new GainSansAction(
                        joueurTraite.getJoueurBDD(),
                        tourMainActuel,
                        resultatNet / tablePoker.getMontantBB()
                );
                session.persist(gainSansAction);
            }

            else {
                resultatNet /= joueurTraite.nActions();
                logger.trace("Value par action : " + resultatNet);

                for (Entree entree : entreesSauvegardees) {
                    if (entree.getJoueur() == joueurTraite.getJoueurBDD()) {
                        entree.setValue(resultatNet / tablePoker.getMontantBB());
                        // il faut ajouter les cartes à la fin sinon c'est 0 avec Winamax
                        // si on a vu les cartes, le joueur est forcément allé au showdown donc value
                        // TODO : problème avec BetClic showdown ne veut pas dire que hero est allé au showdown
                        if (joueurTraite.cartesJoueur() != 0) entree.setCartes(joueurTraite.cartesJoueur());
                        session.merge(entree);
                    }
                }

            }
        }

        double sum = resultats.stream().mapToDouble(Float::doubleValue).sum();
        // si l'erreur est inférieure à 20% BB on va dire que c'est ok
        double tolerance = 0.2f;
        if (sum != 0) {
            if (Math.abs(sum) > (tablePoker.getMontantBB() * tolerance)) {
                throw new InformationsIncorrectes("La somme des gains n'est pas égale à 0 " + sum
                        + ", main n° : " + mainEnregistree.getIdNonUnique());
            }
            else {
                logger.error("La somme des gains n'est pas égale à 0 " + sum
                        + ", main n° : " + mainEnregistree.getIdNonUnique());
            }
        }

    }

    private void corrigerGains() {
        /*
        pour BETCLIC : on rajoute l'exédent misé par chaque gagnant comparé à 2e mise plus élevé
        */
        if (this.room == PokerRoom.IPOKER) {
            logger.trace("Correction des gains");

            for (TablePoker.JoueurTable winner : tablePoker.getJoueurs()) {
                // on ne corrige les gains que des gagnants
                if (!(winner.gains() > 0)) continue;
                int maxOtherBet = 0;
                for (TablePoker.JoueurTable play : tablePoker.getJoueurs()) {
                    if (play != winner) {
                        if (play.totalInvesti() > maxOtherBet) {
                            logger.info("Max other bet trouvé : " + play.totalInvesti());
                            maxOtherBet = (int) play.totalInvesti();
                        }
                    }
                }
                logger.trace("Total investi par winner " + winner.getNom() + ": " + winner.totalInvesti());

                int suppGains = (int) winner.totalInvesti() - maxOtherBet;
                // on ne corrige que si supérieur à 0
                if (suppGains > 0) winner.ajouterGains(suppGains);
                logger.info("Gains corrigés pour " + winner + " : " + winner.gains());
            }
        }

    }

}
