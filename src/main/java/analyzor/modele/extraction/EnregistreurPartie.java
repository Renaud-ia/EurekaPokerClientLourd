package analyzor.modele.extraction;

import analyzor.modele.exceptions.ErreurInterne;
import analyzor.modele.logging.GestionnaireLog;
import analyzor.modele.parties.*;
import analyzor.modele.poker.Board;
import analyzor.modele.poker.ComboReel;
import org.hibernate.Session;


import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EnregistreurPartie {
    private static final Logger logger = GestionnaireLog.getLogger("EnregistreurPartie");
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
    public EnregistreurPartie(long idMain,
                              int montantBB,
                              Partie partie,
                              String nomHero,
                              PokerRoom room,
                              FileHandler handler,
                              Session session) {

        // configuration des logs => on écrit dans le fichier spécifique à la ROOM
        GestionnaireLog.setHandler(logger, handler);

        this.infoMain = new MainInfo();

        //initialisation
        this.montantBB = montantBB;
        this.nomHero = nomHero;
        this.room = room;

        this.session = session;

        this.mainEnregistree = new MainEnregistree(
                idMain,
                montantBB,
                partie
        );
        partie.getMains().add(mainEnregistree);
    }

    //méthodes publiques = interface

    public void ajouterJoueur(String nom, int siege, int stack, float bounty) {
        Joueur joueurBDD = new Joueur(nom);
        session.merge(joueurBDD);
        JoueurInfo joueur = new JoueurInfo(nom, siege, stack, bounty, joueurBDD);
        this.joueurs.add(joueur);

        logger.fine("Joueur ajouté : " + joueur);
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

    public void ajouterBlindes(String nomJoueurBB, String nomJoueurSB) {
        /*
        ajoute au pot les blindes et déduit les stacks
        prend en compte les mises des joueurs et calcule les positions quand la BB est rempli sur la base des seats
        IMPORTANT => il faut l'appeler APRES avoir rentré tous les joueurs
        prend en compte tous les formats (en théorie)
        */
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
            joueurSB = joueurBB;
            montantPayeSB = 0;
        }

        // on positionne BB et SB
        if (joueurs.size() == 2) {
            joueurSB.setPosition(0);
            joueurBB.setPosition(1);
        }

        else {
            //on ordonne les joueurs par leur siège en commençant par SB
            int max_siege = 16;
            joueurs.sort(Comparator.comparingInt(j -> (j.siege < joueurSB.getSiege()) ? j.siege + max_siege : j.siege));
            Collections.reverse(joueurs);

            int positionActuelle = 0;
            for (JoueurInfo joueurSelectionne: joueurs) {
                joueurSelectionne.setPosition(positionActuelle++);
            }
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
        mainEnregistree.getTours().add(tourMainActuel);


        //pas besoin d'enregistrer dans la BDD → automatiquement lors de enregistrement entrée

        infoMain.potAncien += infoMain.potActuel;
        infoMain.potActuel = 0;

        tourActuel = new TourInfo(nomTour, nJoueursInitiaux);
        logger.fine("Nouveau tour ajouté : " + nomTour);
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
        int stackEffectif = stackEffectif();

        int potBounty = 0;
        for (JoueurInfo joueur : joueurs) {
            potBounty += joueur.bounty * joueur.totalInvesti() / joueur.stackInitial;
        }

        Situation situation = new Situation(
                joueurAction.nActions,
                tourActuel.nJoueursActifs,
                tourActuel.nomTour,
                joueurAction.position
        );
        session.merge(situation);
        action.setPot(infoMain.potTotal());
        session.merge(action);


        long start = System.currentTimeMillis();
        // on enregistre dans la BDD
        Entree nouvelleEntree = new Entree(
                infoMain.nombreActions,
                action,
                tourMainActuel,
                situation,
                (float) stackEffectif / montantBB,
                joueurAction.joueurBDD,
                joueurAction.cartesJoueur,
                (float) joueurAction.stackActuel / montantBB,
                (float) infoMain.potAncien / montantBB,
                (float) infoMain.potActuel / montantBB,
                (float) montantCall / montantBB,
                potBounty
        );
        situation.getEntrees().add(nouvelleEntree);
        action.getEntrees().add(nouvelleEntree);
        tourMainActuel.getEntrees().add(nouvelleEntree);
        entreesSauvegardees.add(nouvelleEntree);
        session.merge(nouvelleEntree);


        int montantPaye = joueurAction.ajouterMise(betSupplementaire);
        assert (montantPaye == betSupplementaire);
        tourActuel.dernierBet = Math.max(tourActuel.dernierBet, joueurAction.montantActuel);

        infoMain.potActuel += montantPaye;
        if (action.estFold()) {
            joueurAction.setCouche(true);
            recalculerPositions(joueurAction.position);
        }
        joueurAction.nActions++;
        infoMain.nombreActions++;
        tourActuel.ajouterAction(action);
    }

    private void recalculerPositions(int positionJoueurFolde) {
        for (JoueurInfo joueur : joueurs) {
            if (!joueur.couche && joueur.position > positionJoueurFolde) {
                joueur.setPosition(joueur.position - 1);;
            }
        }
    }

    public void ajouterGains(String nomJoueur, int gains) {
        JoueurInfo joueur = selectionnerJoueur(nomJoueur);
        joueur.gains = gains;
        logger.info("Gains ajoutés pour" + joueur + " : " + gains);
    }

    public void ajouterCartes(String nomJoueur, ComboReel combo) {
        JoueurInfo joueur = selectionnerJoueur(nomJoueur);
        joueur.cartesJoueur = combo.toInt();

        if (Objects.equals(nomJoueur, nomHero)) mainEnregistree.setCartesHero(combo.toInt());
    }

    public void ajouterShowdown(boolean showdown) {
        mainEnregistree.setShowdown(showdown);
    }

    public void mainFinie() {
        enregistrerGains();
        session.merge(tourMainActuel);
        session.merge(mainEnregistree);
    }

    //méthodes privées

    private void enregistrerGains() {
        corrigerGains();

        List<Float> resultats = new ArrayList<>();

        for (JoueurInfo joueurTraite : joueurs) {
            logger.fine("Calcul de la value pour : " + joueurTraite);
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
            logger.fine("Depense pour " + joueurTraite + " : " + depense);
            logger.fine("Gain pour " + joueurTraite + " : " + resultatNet);

            if (joueurTraite.nActions == 0) {
                logger.fine("Aucune action du joueur, value : " + resultatNet);
                GainSansAction gainSansAction = new GainSansAction(
                        joueurTraite.joueurBDD,
                        tourMainActuel,
                        resultatNet
                );
                joueurTraite.joueurBDD.getGainSansActions().add(gainSansAction);
                session.merge(gainSansAction);
            }

            else {
                resultatNet /= joueurTraite.nActions;
                logger.fine("Value par action : " + resultatNet);

                for (Entree entree : entreesSauvegardees) {
                    if (entree.getJoueur() == joueurTraite.joueurBDD) {
                        entree.setValue(resultatNet);
                        session.merge(entree);
                    }
                }

            }
        }

        double sum = resultats.stream().mapToDouble(Float::doubleValue).sum();
        double tolerance = 30;
        if (Math.abs(sum) >= tolerance) {
            throw new IllegalArgumentException("La somme des gains n'est pas égale à 0 " + Math.abs(sum));
        }

    }

    private void corrigerGains() {
        /*
        pour BETCLIC : on rajoute l'exédent misé par chaque gagnant comparé à 2e mise plus élevé
        */
        if (this.room == PokerRoom.IPOKER) {
            logger.fine("Correction des gains");
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
     * on va prendre la moyenne des stacks significatifs
     * @return
     */
    private int stackEffectif() {
        int sommeStacksEffectifs = 0;
        int nJoueurs = 0;
        for (JoueurInfo joueur : joueurs) {
            if (joueur.getStackActuel() / montantBB >= MIN_STACK_EFFECTIF) {
                sommeStacksEffectifs += joueur.getStackActuel();
                nJoueurs++;
            }
        }

        //cas où tout le monde a moins de 4bb = MIN_STACK_EFFECTIF
        if (nJoueurs == 0) {
            for (JoueurInfo play : joueurs) {
                sommeStacksEffectifs += play.getStackActuel();
                nJoueurs++;
            }
        }
        return sommeStacksEffectifs / nJoueurs;
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

            logger.fine("Montant ajouté pour : " + this + " => " + montantPaye);

            return montantPaye;
        }

        public int getSiege() {
            return siege;
        }

        public void setPosition(int position) {
            logger.fine("Joueur placé en position : " + position);
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

        private int dernierBet;
        private TourInfo(TourMain.Round nomTour, int nJoueursInitiaux) {
            this.nomTour = nomTour;
            this.nJoueursActifs = nJoueursInitiaux;
            this.compteActions = 0;
        }

        private void ajouterAction(Action action) {
            // todo : on devrait pas gérer dernierBet ici ????
            compteActions++;
            if (action.estFold()) nJoueursActifs--;
        }

        public void setDernierBet(int bet) {
            dernierBet = bet;
        }
    }

}
