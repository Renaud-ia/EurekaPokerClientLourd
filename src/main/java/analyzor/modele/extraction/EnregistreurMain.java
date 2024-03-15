package analyzor.modele.extraction;

import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.extraction.exceptions.ErreurImportation;
import analyzor.modele.extraction.exceptions.InformationsIncorrectes;
import analyzor.modele.parties.*;
import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboReel;
import analyzor.modele.simulation.TablePoker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.*;


public class EnregistreurMain {
    // todo indiquer le nombre de joueurs, et folder les joueurs précédents si moins de joueur
    // si plus de joueurs => tf (vérifier que c'est le cas pour Betclic)??
    private static final Logger logger = LogManager.getLogger(EnregistreurMain.class);
    private final String nomHero;
    private final PokerRoom room;
    private final MainEnregistree mainEnregistree;
    private final float buyIn;
    private final List<Entree> entreesSauvegardees = new ArrayList<>();
    private TourMain tourMainActuel;
    private final TableImport tablePoker;
    private final boolean bountyVariante;
    private final int nombreJoueursVariante;
    private float rake = 0f;
    private final Session session;
    private NoeudAbstrait generateurId;
    public EnregistreurMain(long idMain,
                            float montantBB,
                            Partie partie,
                            float buyIn,
                            String nomHero,
                            PokerRoom room,
                            boolean bountyVariante,
                            int nombreJoueursVariante,
                            Session session) {
        // on initialise la table en mode valeur absolue
        this.tablePoker = new TableImport(montantBB);

        //initialisation
        this.nomHero = nomHero;
        this.room = room;
        this.bountyVariante = bountyVariante;
        this.nombreJoueursVariante = nombreJoueursVariante;
        this.buyIn = buyIn;

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

    public void ajouterJoueur(String nom, int siege, float stack, float bounty) {
        if (bountyVariante && bounty == 0) {
            // todo : ça va probablement être tout le temps le cas pour Ipoker MTT -> à supprimer,??
            logger.debug("Aucun bounty trouvé alors que la variante en a un");
        }
        Joueur joueurBDD = ObjetUnique.joueur(nom, session);

        // on associe le hero au profil hero
        ProfilJoueur profilJoueur;
        if (nom.equals(nomHero)) profilJoueur = ObjetUnique.selectionnerHero();
        else profilJoueur = ObjetUnique.selectionnerVillain();

        joueurBDD.addProfil(profilJoueur);
        session.merge(joueurBDD);

        tablePoker.ajouterJoueur(nom, siege, stack, bounty, joueurBDD);
        logger.trace("Joueur ajouté (" + nom + " avec stack : " + stack);
    }

    /**
     * méthode utilisée pour ajouter les antes
     * important : ne pas les poser nous même car les règles sont obscures
     */
    public void ajouterAntes(Map<String, Float> antesJoueur) {
        if (antesJoueur == null) return;
        for (Map.Entry<String, Float> entree : antesJoueur.entrySet()) {
            String nomJoueur = entree.getKey();
            float valeurAnte = entree.getValue();
            tablePoker.ajouterAnte(nomJoueur, valeurAnte);
        }
    }

    public void ajouterAnte(String nomJoueur, float valeurAnte) {
        tablePoker.ajouterAnte(nomJoueur, valeurAnte);
    }

    /**
     ajoute au pot les blindes et déduit les stacks
     IMPORTANT => il faut l'appeler APRES avoir rentré tous les joueurs
     prend en compte tous les formats (en théorie)
     méthode plus utilisée car des fois on a plein de blindes posées en CG = taxe d'entrée
     */
    @Deprecated
    public void ajouterBlindes(String nomJoueurBB, String nomJoueurSB) {
        ajouterTour(TourMain.Round.PREFLOP, null);
        tablePoker.ajouterBlindes(nomJoueurBB, nomJoueurSB);
    }

    public void ajouterBlindes(String nomJoueur, float valeurBlinde) {
        if (tablePoker.tourActuel() == null) {
            ajouterTour(TourMain.Round.PREFLOP, null);
        }
        tablePoker.ajouterBlindes(nomJoueur, valeurBlinde);
    }


    public void ajouterTour(TourMain.Round nomTour, Board board) {
        int nJoueursInitiaux = tablePoker.nouveauTour();

        if (tourMainActuel != null) session.merge(tourMainActuel);
        this.tourMainActuel = new TourMain(nomTour, this.mainEnregistree, board, nJoueursInitiaux);
        session.merge(tourMainActuel);
        mainEnregistree.getTours().add(tourMainActuel);

        generateurId = new NoeudAbstrait(nJoueursInitiaux, nomTour);

        // on va folder les joueurs si moins de joueurs que dans la variante
        // mais on garde le mode HU (=2 joueurs) car le fonctionnement est différent
        int nJoueursTable = tablePoker.getJoueurs().size();
        if (tablePoker.tourActuel() == TourMain.Round.PREFLOP && nJoueursTable > 2) {
            while (nJoueursTable < nombreJoueursVariante) {
                logger.info("Moins de joueurs que dans la variante");
                generateurId.ajouterAction(Move.FOLD);
                nJoueursTable++;
            }
        }
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
        logger.info("Action de : " + nomJoueur + " : " + action.getMove() + ", bet size :" + action.getBetSize());

        // uniformisation des taille de BetSize entre les différentes rooms
        //GESTION BUG WINAMAX
        if (!betComplet) {
            action.augmenterBet(tablePoker.dernierBet());
            betTotal = true;
        }

        // on fixe le joueur qui va jouer
        TablePoker.JoueurTable joueurAction = tablePoker.setJoueur(nomJoueur);

        // on récupère les infos sur la situation
        long stackEffectif = tablePoker.stackEffectif().getIdGenere();
        float potBounty = tablePoker.getPotBounty() / buyIn;
        float stackJoueur = tablePoker.getStackJoueur(nomJoueur) / tablePoker.getMontantBB();

        // on retire les ante du pot car c'est très chiant pour les prendre en compte après dans Simulation
        // et ça fait buguer les valeurs normalisées
        float potAncien = tablePoker.getAncienPot() - tablePoker.getPotAnte() / tablePoker.getMontantBB();
        float potActuel = tablePoker.getPotActuel() - tablePoker.getPotAnte() / tablePoker.getMontantBB();

        // le montant du bet size est exprimé relativement au pot
        // on ne met pas les ante dans le pot car c'est le bordel pour retrouver les mises après
        float relativeBetSize = action.getBetSize() /
                (tablePoker.getPotTotal() - tablePoker.getPotAnte());

        // on ajoute l'action après pour avoir les valeurs de la situation AVANT l'action
        tablePoker.ajouterAction(nomJoueur, action.getMove(), action.getBetSize(), betTotal);
        generateurId.ajouterAction(action.getMove());


        // on enregistre dans la BDD
        Entree nouvelleEntree = new Entree(
                tablePoker.nombreActions(),
                tourMainActuel,
                generateurId.toLong(),
                relativeBetSize,
                stackEffectif,
                joueurAction.getJoueurBDD(),
                stackJoueur,
                potAncien,
                potActuel,
                potBounty
        );
        tourMainActuel.getEntrees().add(nouvelleEntree);
        entreesSauvegardees.add(nouvelleEntree);
        session.merge(nouvelleEntree);
    }

    public void ajouterGains(String nomJoueur, float gains) {
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

    public void ajouterRake(float rake) {
        this.rake = rake;
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
            float gains = joueurTraite.gains();
            float depense = joueurTraite.totalInvesti();

            //on ne peut pas perdre plus que la plus grosse mise adverse
            float maxPlusGrosBet = 0;
            if (gains == 0) {
                for (TablePoker.JoueurTable joueur : tablePoker.getJoueurs()) {
                    if (joueur != joueurTraite) {
                        if (joueur.totalInvesti() > maxPlusGrosBet) {
                            maxPlusGrosBet = joueur.totalInvesti();
                        }
                    }
                }
                depense = Math.min(depense, maxPlusGrosBet);
            }

            // important, il faut ajouter les ante après car certains la posent d'autres non
            depense += joueurTraite.anteInvestie();

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
        // on prévoit une petite tolérance car on ne connait pas le rake prélévé pour Betclic (= 10% du pot)
        double tolerance = 0.10f * tablePoker.getPotTotal();
        if (sum != 0) {
            logger.trace("Somme des gains : " + sum + ", rake" + rake);
            if ((Math.abs(sum) - rake) > tolerance) {
                throw new InformationsIncorrectes("La somme des gains n'est pas égale à 0 " + (Math.abs(sum) - rake) +
                        ", main n° : " + mainEnregistree.getIdNonUnique() +
                        ", tolérance : " + tolerance);
            }
            else {
                logger.debug("La somme des gains n'est pas égale à 0 " + (Math.abs(sum) - rake)
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
                float maxOtherBet = 0;
                for (TablePoker.JoueurTable play : tablePoker.getJoueurs()) {
                    if (play != winner) {
                        if (play.totalInvesti() > maxOtherBet) {
                            logger.info("Max other bet trouvé : " + play.totalInvesti());
                            maxOtherBet = play.totalInvesti();
                        }
                    }
                }
                logger.trace("Total investi par winner " + winner.getNom() + ": " + winner.totalInvesti());

                float suppGains = winner.totalInvesti() - maxOtherBet;
                // on ne corrige que si supérieur à 0
                if (suppGains > 0) winner.ajouterGains(suppGains);
                logger.info("Gains corrigés pour " + winner + " : " + winner.gains());
            }
        }

    }

}
