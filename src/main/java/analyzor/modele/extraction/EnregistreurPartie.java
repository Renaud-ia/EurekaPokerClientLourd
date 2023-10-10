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

    private int potActuel = 0;
    private int potAncien = 0;
    private final List<JoueurInfo> joueurs = new ArrayList<>();
    private final List<Entree> entreesSauvegardees = new ArrayList<>();
    private TourInfo tourActuel;
    private TourMain tourMainActuel;

    private final Session session;
    public EnregistreurPartie(int idMain,
                              int montantBB,
                              Partie partie,
                              String nomHero,
                              PokerRoom room,
                              FileHandler handler,
                              Session session) throws ErreurInterne {

        // configuration des logs => on écrit dans le fichier spécifique à la ROOM
        GestionnaireLog.setHandler(logger, handler);

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
            this.potActuel += valeurAnte;
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
        } else {
            joueurSB.setPosition(1);
            joueurBB.setPosition(2);
        }

        //on ordonne les joueurs restant par leur siège
        List<JoueurInfo> joueursOrdonnes = joueurs.stream()
                .filter(play -> play.getSiege() > joueurBB.getSiege() || play.getSiege() < joueurSB.getSiege())
                .sorted(Comparator.comparingInt(JoueurInfo::getSiege))
                .collect(Collectors.toList());

        int positionActuelle = 3;
        for (int index = 0; index < joueursOrdonnes.size(); index++) {
            JoueurInfo joueur = joueursOrdonnes.get(index);

            if (index + 1 == joueursOrdonnes.size()) {
                joueur.setPosition(0);
                break;
            }

            joueur.setPosition(positionActuelle);
            positionActuelle++;
        }

        potActuel = montantPayeSB + montantPayeBB;
        tourActuel.setDernierBet(Math.max(montantPayeSB, montantPayeBB));
    }


    public void ajouterTour(TourMain.Round nomTour, Board board) {
        int nJoueursInitiaux = 0;
        for (JoueurInfo joueur : joueurs) {
            if (!joueur.estCouche()) nJoueursInitiaux++;
            joueur.nouveauTour();
        }
        logger.fine("Enregistrement ancien tour");
        if (tourMainActuel != null) session.merge(tourMainActuel);
        this.tourMainActuel = new TourMain(nomTour, this.mainEnregistree, board, nJoueursInitiaux);
        mainEnregistree.getTours().add(tourMainActuel);

        this.potAncien += this.potActuel;
        this.potActuel = 0;

        tourActuel = new TourInfo(nomTour, nJoueursInitiaux);
        logger.fine("Nouveau tour ajouté : " + nomTour);
    }

    public void ajouterAction(Action action, String nomJoueur, boolean betTotal) {
        ajouterAction(action, nomJoueur, betTotal, false);
    }

    public void ajouterAction(Action action, String nomJoueur, boolean betTotal, boolean betComplet)  {
        /*
        montantCall = (montant de la mise plus élevée ou stack du joueur) - déjà investi par le joueur
        bet_size = total_bet_size dans le stage
        last_bet = montant TOTAL de la mise plus élevée
        current_stack = min_current_stack
        */
        logger.info("Nouvelle action");
        JoueurInfo joueurAction = selectionnerJoueur(nomJoueur);

        //todo devrait être fait au sein de action
        //GESTION BUG WINAMAX
        if (!betComplet) {
            action.augmenterBet(tourActuel.getDernierBet());
            betTotal = true;
        }

        int betSupplementaire;
        if (action.getBetSize() > 0) {
            if (betTotal) betSupplementaire = action.getBetSize() - joueurAction.getMiseCurrente();
            else betSupplementaire = action.getBetSize();
        }
        else {
            betSupplementaire = 0;
        }

        //dans la BDD, bet size exprimé en mise supplémentaire
        //action.setRelativeBetSize((float) betSupplementaire / (potActuel + potAncien));

        int montantCall;
        if (tourActuel.getDernierBet() > joueurAction.getStackActuel()) montantCall = joueurAction.getStackActuel();
        else montantCall = tourActuel.getDernierBet() - joueurAction.getMiseCurrente();
        if (montantCall < 0) montantCall = 0;

        logger.fine("Debut Tout va bien");

        //le bet est retiré du stack player après l'enregistrement du coup
        //le current pot est incrémenté après l'enregistrement du coup
        //les pots sont resets à la fin du round
        int stackEffectif = stackEffectif();

        logger.fine("Milieu tout va bien");

        // todo c'est quoi ce putain de délire
        int potBounty = 0;
        //for (JoueurInfo joueur : joueurs) {
        //    logger.fine("loop");
        //    potBounty += joueur.bounty * joueur.totalInvesti() / joueur.stackInitial;
        //}

        logger.fine("Milieu 2 tout va bien");

        Situation situation = new Situation(
                joueurAction.nActions,
                tourActuel.nJoueursActifs,
                tourActuel.getNomTour(),
                joueurAction.position
        );
        session.merge(situation);
        session.merge(action);

        logger.fine("Tout va bien");

        // on enregistre dans la BDD
        Entree nouvelleEntree = new Entree(
                tourActuel.getCompteActions(),
                action,
                tourMainActuel,
                situation,
                (float) stackEffectif / montantBB,
                joueurAction.getJoueurBDD(),
                joueurAction.cartesJoueur,
                (float) joueurAction.getStackActuel() / montantBB,
                (float) potAncien / montantBB,
                (float) potActuel / montantBB,
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

        potActuel += montantPaye;

        tourActuel.setDernierBet(Math.max(tourActuel.getDernierBet(), joueurAction.getMiseCurrente()));
        tourActuel.ajouterAction(action);
        joueurAction.ajouterAction(action);
    }

    public void ajouterGains(String nomJoueur, int gains) {
        JoueurInfo joueur = selectionnerJoueur(nomJoueur);
        joueur.setGains(gains);
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
            int gains = joueurTraite.getGains();
            int depense = joueurTraite.totalInvesti();

            //on ne peut pas perdre plus que la plus grosse mise adverse
            if (gains == 0) {
                for (JoueurInfo joueur : joueurs) {
                    if (joueur != joueurTraite) {
                        if (joueur.totalInvesti() < depense) {
                            depense = joueur.totalInvesti();
                            logger.info("Perte limitée pour " + joueurTraite + " à : " + depense);
                        }
                    }
                }
            }

            float resultatNet = gains - depense;
            resultats.add(resultatNet);

            logger.info(joueurTraite + " a un résultat de : " + resultatNet);

            if (joueurTraite.nActions == 0) {
                logger.fine("Aucune action du joueur");
                GainSansAction gainSansAction = new GainSansAction(
                        joueurTraite.getJoueurBDD(),
                        tourMainActuel,
                        resultatNet
                );
                joueurTraite.getJoueurBDD().getGainSansActions().add(gainSansAction);
                session.merge(gainSansAction);
            }

            else {
                logger.fine("Plusieurs actions du joueur");
                resultatNet /= joueurTraite.nActions;

                for (Entree entree : entreesSauvegardees) {
                    if (entree.getJoueur() == joueurTraite.getJoueurBDD()) {
                        entree.setValue(resultatNet);
                        session.merge(entree);
                    }
                }

            }
        }

        double sum = resultats.stream().mapToDouble(Float::doubleValue).sum();
        double tolerance = 0.1;
        assert Math.abs(sum) < tolerance : "La somme des gains n'est pas égale à 0";

    }

    private void corrigerGains() {
        /*
        pour BETCLIC : on rajoute l'exédent misé par chaque gagnant comparé à 2è mise plus élevé
        */
        if (this.room == PokerRoom.IPOKER) {
            logger.fine("Les gains vont être corrigés");
            List<JoueurInfo> winners = new ArrayList<>();
            for (JoueurInfo play : joueurs) {
                if (play.getGains() > 0) {
                    winners.add(play);
                }
            }

            for (JoueurInfo winner : winners) {
                // TODO : gestion des ante BETCLIC ???

                int maxOtherBet = 0;
                for (JoueurInfo play : joueurs) {
                    if (play != winner) {
                        if (play.totalInvesti() > maxOtherBet) {
                            maxOtherBet = play.totalInvesti();
                        }
                    }
                }

                int suppBet = winner.totalInvesti() - maxOtherBet;
                if (suppBet > 0) {
                    winner.incrementerGains(suppBet);
                }
            }
        }

    }

    private int stackEffectif() {
        List<Integer> effStacks = new ArrayList<>();
        for (JoueurInfo joueur : joueurs) {
            if (joueur.getStackActuel() / montantBB >= MIN_STACK_EFFECTIF) {
                effStacks.add(joueur.getStackActuel());
            }
        }

        //cas où tout le monde a moins de 4bb = MIN_STACK_EFFECTIF
        if (effStacks.isEmpty()) {
            for (JoueurInfo play : joueurs) {
                effStacks.add(play.getStackActuel());
            }
        }
        return Collections.min(effStacks);
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
            this.position = position;
        }

        public int getStackActuel() {
            return stackActuel;
        }

        public void setCouche(boolean couche) {
            this.couche = couche;
        }

        public int totalInvesti() {
            logger.fine("Total investi par " + this + " : " + totalInvesti());
            return montantInvesti + montantActuel + anteInvesti;
        }

        public int getMiseCurrente() {
            return montantActuel;
        }

        public Joueur getJoueurBDD() {
            return joueurBDD;
        }

        public int getGains() {
            return gains;
        }

        public void incrementerGains(int suppGain) {
            gains += suppGain;
        }

        public void setGains(int gains) {
            this.gains = gains;
        }

        public void ajouterAction(Action action) {
            if (action.estFold()) this.setCouche(true);
            this.nActions++;
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
            logger.fine("Compte actions vaut : " + compteActions);
            logger.fine("Nombre de joueurs actifs : " + nJoueursActifs);
        }

        public void setDernierBet(int bet) {
            dernierBet = bet;
        }

        public int getCompteActions() {
            return compteActions;
        }

        public TourMain.Round getNomTour() {
            return nomTour;
        }

        public int getDernierBet() {
            return dernierBet;
        }
    }

}
