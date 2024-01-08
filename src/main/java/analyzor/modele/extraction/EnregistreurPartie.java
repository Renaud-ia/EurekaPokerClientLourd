package analyzor.modele.extraction;

import analyzor.modele.bdd.ObjetUnique;
import analyzor.modele.config.ValeursConfig;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.extraction.exceptions.ErreurImportation;
import analyzor.modele.extraction.exceptions.InformationsIncorrectes;
import analyzor.modele.parties.*;
import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboReel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.*;


public class EnregistreurPartie {
    private static final Logger logger = LogManager.getLogger(EnregistreurPartie.class);
    private static final int MIN_STACK_EFFECTIF = 4;
    private final int montantBB;
    private final String nomHero;
    private final PokerRoom room;
    private final MainEnregistree mainEnregistree;

    private final List<JoueurInfo> joueurs = new ArrayList<>();
    private final List<Entree> entreesSauvegardees = new ArrayList<>();
    /* déprecié
    private List<TourInfo> tours = new ArrayList<>();
     */
    private TourInfo tourActuel;
    private TourMain tourMainActuel;
    private MainInfo infoMain;

    private final Session session;
    private final Partie partie;
    private NoeudAbstrait generateurId;
    public EnregistreurPartie(long idMain,
                              int montantBB,
                              Partie partie,
                              String nomHero,
                              PokerRoom room,
                              Session session) {

        this.infoMain = new MainInfo();

        //initialisation
        this.montantBB = montantBB;
        this.nomHero = nomHero;
        this.room = room;

        this.session = session;
        this.partie = partie;

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
        Joueur joueurBDD = ObjetUnique.joueur(nom);

        // on associe le hero au profil hero
        ProfilJoueur profilJoueur;
        if (nom.equals(nomHero)) profilJoueur = ObjetUnique.profilJoueur(null, true);
        else profilJoueur = ObjetUnique.profilJoueur(null, false);

        session.merge(profilJoueur);
        profilJoueur.getJoueurs().add(joueurBDD);
        joueurBDD.setProfil(profilJoueur);

        session.merge(joueurBDD);
        JoueurInfo joueur = new JoueurInfo(nom, siege, stack, bounty, joueurBDD);
        this.joueurs.add(joueur);

        logger.trace("Joueur ajouté : " + joueur);


    }

    public void ajouterAntes(Map<String, Integer> antesJoueur) {
        if (antesJoueur == null) return;
        for (Map.Entry<String, Integer> entree : antesJoueur.entrySet()) {
            String nomJoueur = entree.getKey();
            int valeurAnte = entree.getValue();
            JoueurInfo joueurInfo = selectionnerJoueur(nomJoueur);
            joueurInfo.ajouterAnte(valeurAnte);
            infoMain.potActuel += valeurAnte;
        }
    }

    /**
     ajoute au pot les blindes et déduit les stacks
     IMPORTANT => il faut l'appeler APRES avoir rentré tous les joueurs
     prend en compte tous les formats (en théorie)
     */
    public void ajouterBlindes(String nomJoueurBB, String nomJoueurSB) {

        //todo ajouter le nombre de joueurs à la main (commit machin)

        ajouterTour(TourMain.Round.PREFLOP, null);

        JoueurInfo joueurBB = selectionnerJoueur(nomJoueurBB);
        int montantPayeBB = joueurBB.ajouterMise(this.montantBB);

        JoueurInfo joueurSB;
        int montantPayeSB;
        if (nomJoueurSB != null) {
            joueurSB = selectionnerJoueur(nomJoueurSB);
            montantPayeSB = joueurSB.ajouterMise((int) (this.montantBB/2));
        }

        else {
            montantPayeSB = 0;
        }

        infoMain.potActuel = montantPayeSB + montantPayeBB;
        tourActuel.setDernierBet(Math.max(montantPayeSB, montantPayeBB));
    }


    public void ajouterTour(TourMain.Round nomTour, Board board) {
        int nJoueursInitiaux = 0;

        for (JoueurInfo joueur : joueurs) {
            if (!joueur.estCouche()) nJoueursInitiaux++;
            joueur.nouveauTour();
        }
        if (tourMainActuel != null) session.merge(tourMainActuel);
        this.tourMainActuel = new TourMain(nomTour, this.mainEnregistree, board, nJoueursInitiaux);
        session.merge(tourMainActuel);
        mainEnregistree.getTours().add(tourMainActuel);


        //pas besoin d'enregistrer dans la BDD → automatiquement lors de enregistrement entrée

        infoMain.potAncien += infoMain.potActuel;
        infoMain.potActuel = 0;

        tourActuel = new TourInfo(nomTour, nJoueursInitiaux);
        logger.trace("Nouveau tour ajouté : " + nomTour);

        generateurId = new NoeudAbstrait(this.tourActuel.nJoueursInitiaux(), nomTour);
    }

    public void ajouterAction(Action action, String nomJoueur, boolean betTotal) {
        ajouterAction(action, nomJoueur, betTotal, false);
    }

    public void ajouterAction(Action action, String nomJoueur, boolean betTotal, boolean betComplet) {
        /*
        montantCall = (montant de la mise plus élevée ou stack du joueur) - déjà investi par le joueur
        bet_size = total_bet_size dans le stage
        last_bet = montant TOTAL de la mise plus élevée
        current_stack = min_current_stack
        */
        logger.info("Action de : " + nomJoueur + " : " + action.getBetSize());
        JoueurInfo joueurAction = selectionnerJoueur(nomJoueur);

        //GESTION BUG WINAMAX
        if (!betComplet) {
            action.augmenterBet(tourActuel.dernierBet);
            betTotal = true;
        }

        int betSupplementaire;
        if (action.getBetSize() > 0) {
            if (betTotal) betSupplementaire = action.getBetSize() - joueurAction.montantActuel;
            else betSupplementaire = action.getBetSize();
        }
        else {
            betSupplementaire = 0;
        }

        int montantCall;
        if (tourActuel.dernierBet > joueurAction.stackActuel) montantCall = joueurAction.stackActuel;
        else montantCall = tourActuel.dernierBet - joueurAction.montantActuel;
        if (montantCall < 0) montantCall = 0;

        //le bet est retiré du stack player après l'enregistrement du coup
        //le current pot est incrémenté après l'enregistrement du coup
        //les pots sont resets à la fin du round
        float stackEffectif = stackEffectif(joueurAction);

        float potBounty = 0;
        for (JoueurInfo joueur : joueurs) {
            potBounty += joueur.bounty * joueur.totalInvesti() / joueur.stackInitial;
        }

        action.setPot(infoMain.potTotal());
        generateurId.ajouterAction(action.getMove());

        // on enregistre dans la BDD
        Entree nouvelleEntree = new Entree(
                infoMain.nombreActions,
                tourMainActuel,
                generateurId.toLong(),
                action.getRelativeBetSize(),
                stackEffectif / montantBB,
                joueurAction.joueurBDD,
                (float) joueurAction.stackActuel / montantBB,
                (float) infoMain.potAncien / montantBB,
                (float) infoMain.potActuel / montantBB,
                potBounty
        );
        tourMainActuel.getEntrees().add(nouvelleEntree);
        entreesSauvegardees.add(nouvelleEntree);
        session.merge(nouvelleEntree);


        int montantPaye = joueurAction.ajouterMise(betSupplementaire);
        assert (montantPaye == betSupplementaire);
        tourActuel.dernierBet = Math.max(tourActuel.dernierBet, joueurAction.montantActuel);

        infoMain.potActuel += montantPaye;
        if (action.estFold()) {
            joueurAction.setCouche(true);
        }
        joueurAction.nActions++;
        infoMain.nombreActions++;
        tourActuel.ajouterAction(action);
    }

    public void ajouterGains(String nomJoueur, int gains) {
        JoueurInfo joueur = selectionnerJoueur(nomJoueur);
        joueur.gains = gains;
        logger.info("Gains ajoutés pour" + joueur + " : " + gains);
    }

    public void ajouterCartes(String nomJoueur, ComboReel combo) {
        JoueurInfo joueur = selectionnerJoueur(nomJoueur);
        joueur.cartesJoueur = combo.toInt();
        logger.info("Cartes ajoutés pour" + joueur + " : " + combo);
    }

    // procédure séparée car sinon c'est le bordel car IPoker détecte toujours les cartes Hero
    public void ajouterCarteHero(ComboReel combo) {
        mainEnregistree.setCartesHero(combo.toInt());
    }

    /**
     * pas pertinent car le fait de voir des cartes est presque identiques
     * et surtout indique QUEL JOUEUR EST ALLE AU SHOWDOWN et non pas global
     */
    @Deprecated
    public void ajouterShowdown(boolean showdown) {
        mainEnregistree.setShowdown(showdown);
    }

    public void mainFinie() throws ErreurImportation {
        enregistrerGains();
        session.merge(mainEnregistree);
    }

    //méthodes privées

    private void enregistrerGains() throws ErreurImportation {
        corrigerGains();

        List<Float> resultats = new ArrayList<>();

        for (JoueurInfo joueurTraite : joueurs) {
            logger.trace("Calcul de la value pour : " + joueurTraite);
            int gains = joueurTraite.gains;
            int depense = joueurTraite.totalInvesti();

            //on ne peut pas perdre plus que la plus grosse mise adverse
            int maxPlusGrosBet = 0;
            if (gains == 0) {
                for (JoueurInfo joueur : joueurs) {
                    if (joueur != joueurTraite) {
                        if (joueur.totalInvesti() > maxPlusGrosBet) {
                            maxPlusGrosBet = joueur.totalInvesti();
                        }
                    }
                }
                depense = Math.min(depense, maxPlusGrosBet);
            }

            float resultatNet = gains - depense;
            resultats.add(resultatNet);
            logger.trace("Depense pour " + joueurTraite + " : " + depense);
            logger.trace("Gain pour " + joueurTraite + " : " + resultatNet);

            if (joueurTraite.nActions == 0) {
                logger.trace("Aucune action du joueur, value : " + resultatNet);
                GainSansAction gainSansAction = new GainSansAction(
                        joueurTraite.joueurBDD,
                        tourMainActuel,
                        resultatNet
                );
                session.persist(gainSansAction);
                joueurTraite.joueurBDD.getGainSansActions().add(gainSansAction);
                session.merge(joueurTraite.joueurBDD);
            }

            else {
                resultatNet /= joueurTraite.nActions;
                logger.trace("Value par action : " + resultatNet);

                for (Entree entree : entreesSauvegardees) {
                    if (entree.getJoueur() == joueurTraite.joueurBDD) {
                        entree.setValue(resultatNet);
                        // il faut ajouter les cartes à la fin sinon c'est 0 avec Winamax
                        // si on a vu les cartes, le joueur est forcément allé au showdown donc value
                        // TODO : problème avec BetClic showdown ne veut pas dire que hero est allé au showdown
                        if (joueurTraite.cartesJoueur != 0) entree.setCartes(joueurTraite.cartesJoueur);
                        session.merge(entree);
                    }
                }

            }
        }

        double sum = resultats.stream().mapToDouble(Float::doubleValue).sum();
        double tolerance = 30;
        if (Math.abs(sum) >= tolerance) {
            System.out.println("La somme des gains n'est pas égale à 0 " + Math.abs(sum));
            throw new InformationsIncorrectes("La somme des gains n'est pas égale à 0 " + Math.abs(sum));
        }

    }

    private void corrigerGains() {
        /*
        pour BETCLIC : on rajoute l'exédent misé par chaque gagnant comparé à 2e mise plus élevé
        */
        if (this.room == PokerRoom.IPOKER) {
            logger.trace("Correction des gains");
            List<JoueurInfo> winners = new ArrayList<>();
            for (JoueurInfo play : joueurs) {
                if (play.gains > 0) {
                    winners.add(play);
                }
            }

            for (JoueurInfo winner : winners) {
                int maxOtherBet = 0;
                for (JoueurInfo play : joueurs) {
                    if (play != winner) {
                        if (play.totalInvesti() > maxOtherBet) {
                            logger.info("Max other bet trouvé : " + play.totalInvesti());
                            maxOtherBet = play.totalInvesti();
                        }
                    }
                }

                int suppBet = winner.totalInvesti() - maxOtherBet;
                if (suppBet > 0) {
                    winner.gains += suppBet;
                    logger.info("Gains corrigés pour " + winner + " : " + winner.gains);
                }
            }
        }

    }

    /**
     * on prend le plus gros stack du joueur qui n'est pas le joueur concerné
     * s'il est supérieur au stack du joueur, on prend le stack du joueur
     * pas optimal mais caractérise le "risque" que prend le joueur
     * @return le stack effectif
     */
    private float stackEffectif(JoueurInfo joueurAction) {
        int maxStack = 0;
        for (JoueurInfo joueur : joueurs) {
            if (joueur.estCouche() || joueur == joueurAction) continue;
            if (joueur.getStackActuel() > maxStack) {
                maxStack = joueur.getStackActuel();
            }
        }

        return (float) Math.min(maxStack, joueurAction.getStackActuel());
    }

    private JoueurInfo selectionnerJoueur(String nomJoueur) {
        List<JoueurInfo> joueursTrouves = new ArrayList<>();

        for (JoueurInfo joueur : joueurs) {
            if (joueur.getNom().equals(nomJoueur)) {
                joueursTrouves.add(joueur);
            }
        }

        if (joueursTrouves.size() != 1) {
            throw new IllegalArgumentException("Nombre de joueurs trouvés pour " + nomJoueur + " : " + joueursTrouves.size());
        }

        return joueursTrouves.get(0);
    }

    private class MainInfo {
        int potAncien;
        int potActuel;
        int nombreActions;

        protected MainInfo() {
            potActuel = 0;
            potAncien = 0;
            nombreActions = 0;
        }

        public int potTotal() {
            return potAncien + potActuel;
        }
    }


    private class JoueurInfo {
        private final String nom;
        private final int siege;
        private final float bounty;
        private final Joueur joueurBDD;

        private final int stackInitial;
        private int stackActuel;
        private int nActions = 0;
        private int montantInvesti = 0;
        private int montantActuel = 0;
        private int gains = 0;
        private int cartesJoueur;
        private boolean couche;
        private int position;
        private int anteInvesti = 0;

        JoueurInfo(String nom, int siege, int stack, float bounty, Joueur joueurBDD) {
            this.nom = nom;
            this.siege = siege;
            this.bounty = bounty;
            this.joueurBDD = joueurBDD;

            this.stackInitial = stack;
            this.stackActuel = stack;
        }

        private Object getNom() {
            return this.nom;
        }

        private void ajouterAnte(int valeurAnte) {
            this.anteInvesti = valeurAnte;
        }

        @Override
        public String toString() {
            return "JoueurInfo(" + nom + ")";
        }

        public void nouveauTour() {
            montantInvesti += montantActuel;
            montantActuel = 0;
        }

        public boolean estCouche() {
            return couche;
        }

        public int ajouterMise(int montantMise) {
            int montantPaye = Math.min(montantMise, this.stackActuel);

            this.stackActuel -= montantPaye;
            this.montantActuel += montantPaye;

            logger.trace("Montant ajouté pour : " + this + " => " + montantPaye);

            return montantPaye;
        }

        public int getSiege() {
            return siege;
        }

        public void setPosition(int position) {
            logger.trace("Joueur placé en position : " + position);
            this.position = position;
        }

        public int getStackActuel() {
            return stackActuel;
        }

        public void setCouche(boolean couche) {
            this.couche = couche;
        }

        public int totalInvesti() {
            return montantInvesti + montantActuel + anteInvesti;
        }
    }

    private class TourInfo {
        public TourMain.Round nomTour;
        private int nJoueursActifs;
        private int compteActions;
        private final int nJoueursInitiaux;

        private int dernierBet;
        private TourInfo(TourMain.Round nomTour, int nJoueursInitiaux) {
            this.nomTour = nomTour;
            this.nJoueursActifs = nJoueursInitiaux;
            this.compteActions = 0;
            this.nJoueursInitiaux = nJoueursInitiaux;
        }

        private void ajouterAction(Action action) {
            // todo : on devrait pas gérer dernierBet ici ????
            compteActions++;
            if (action.estFold()) nJoueursActifs--;
        }

        public void setDernierBet(int bet) {
            dernierBet = bet;
        }

        public int nJoueursInitiaux() {
            return nJoueursInitiaux;
        }
    }

}
