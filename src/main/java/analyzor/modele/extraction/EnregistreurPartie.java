package analyzor.modele.extraction;

import analyzor.modele.exceptions.ErreurInterne;
import analyzor.modele.logging.GestionnaireLog;
import analyzor.modele.parties.*;
import analyzor.modele.poker.Board;
import analyzor.modele.poker.evaluation.Combo;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EnregistreurPartie {
    private static final Logger logger = GestionnaireLog.getLogger("EnregistreurPartie");
    private static final int MIN_STACK_EFFECTIF = 4;

    private final int idMain;
    private final int montantBB;
    private final Partie partie;
    private final String nomHero;
    private final Variante.PokerRoom room;
    private final MainEnregistree mainEnregistree;

    private int potActuel = 0;
    private int potAncien = 0;
    private List<JoueurInfo> joueurs = new ArrayList<>();
    /* déprecié
    private List<TourInfo> tours = new ArrayList<>();
     */
    private TourInfo tourActuel;
    private TourMain tourMainActuel;
    public EnregistreurPartie(FileHandler fileHandlerGestionnaire,
                              int idMain,
                              int montantBB,
                              Partie partie,
                              String nomHero,
                              Variante.PokerRoom room) throws ErreurInterne {

        // configuration des logs
        GestionnaireLog.setHandler(logger, fileHandlerGestionnaire);
        GestionnaireLog.setHandler(logger, GestionnaireLog.warningImport);

        //initialisation
        this.idMain = idMain;
        this.montantBB = montantBB;
        this.partie = partie;
        this.nomHero = nomHero;
        this.room = room;

        this.mainEnregistree = (MainEnregistree) RequetesBDD.getOrCreate(new MainEnregistree(
                idMain,
                montantBB,
                partie
        ));
    }

    public void ajouterJoueur(String nom, int siege, int stack, float bounty) throws ErreurInterne {
        Joueur joueurBDD = (Joueur) RequetesBDD.getOrCreate(new Joueur(nom));
        JoueurInfo joueur = new JoueurInfo(nom, siege, stack, bounty, joueurBDD);
        this.joueurs.add(joueur);

        logger.fine("Joueur ajouté : " + joueur);
    }

    public void ajouterAntes(Map<String, Integer> antesJoueur) {
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
        ajouterTour(TourMain.Round.PREFLOP, null);

        JoueurInfo joueurBB = selectionnerJoueur(nomJoueurBB);
        int montantPayeBB = joueurBB.ajouterMise(this.montantBB);

        JoueurInfo joueurSB;
        int montantPayeSB;
        if (nomJoueurSB != null) {
            joueurSB = selectionnerJoueur(nomJoueurSB);
            montantPayeSB = joueurBB.ajouterMise((int) (this.montantBB/2));
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


    public void ajouterTour(TourMain.Round nomTour, String boardAsString) {
        logger.fine("Nouveau tour ajouté : " + nomTour);

        int nJoueursInitiaux = 0;
        for (JoueurInfo joueur : joueurs) {
            if (!joueur.estCouche()) nJoueursInitiaux++;
            joueur.nouveauTour();
        }

        Board board = new Board(boardAsString);
        this.tourMainActuel = new TourMain(nomTour, this.mainEnregistree, board, nJoueursInitiaux);

        //pas besoin d'enregistrer dans la BDD -> automatiquement lors de enregistrement entrée

        this.potAncien += this.potActuel;
        this.potActuel = 0;

        tourActuel = new TourInfo(nomTour, nJoueursInitiaux);
    }

    public void ajouterAction(Action action, String nomJoueur, boolean betTotal) throws ErreurInterne {
        ajouterAction(action, nomJoueur, betTotal, false);
    }

    public void ajouterAction(Action action, String nomJoueur, boolean betTotal, boolean betComplet) throws ErreurInterne {
        /*
        montantCall = (montant de la mise plus élevée ou stack du joueur) - déjà investi par le joueur
        bet_size = total_bet_size dans le stage
        last_bet = montant TOTAL de la mise plus élevée
        current_stack = min_current_stack
        */
        JoueurInfo joueurAction = selectionnerJoueur(nomJoueur);

        //GESTION BUG WINAMAX
        if (betComplet) {
            action.augmenterBet(tourActuel.dernierBet);
            betTotal = true;
        }

        int betSupplementaire;
        if (betTotal) betSupplementaire = action.getBetSize() - joueurAction.montantActuel;
        else betSupplementaire = action.getBetSize();

        int montantCall;
        if (tourActuel.dernierBet > joueurAction.stackActuel) montantCall = joueurAction.stackActuel;
        else montantCall = tourActuel.dernierBet - joueurAction.montantActuel;

        //le bet est retiré du stack player après l'enregistrement du coup
        //le current pot est incrémenté après l'enregistrement du coup
        //les pots sont resets à la fin du round
        int stackEffectif = stackEffectif();

        int potBounty = 0;
        for (JoueurInfo joueur : joueurs) {
            potBounty += joueur.bounty * joueur.totalInvesti() / joueur.stackInitial;
        }


        Joueur JoueurBDD = joueurAction.getBDD();

        Situation situation = new Situation(
                joueurAction.nActions,
                tourActuel.nJoueursActifs,
                tourActuel.nomTour,
                joueurAction.position
        );

        //attention session ne doit pas être déjà ouverte
        RequetesBDD.getOrCreate(action);
        RequetesBDD.getOrCreate(situation);

        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();
        Transaction transaction = session.beginTransaction();

        // pas besoin d'enregistrer tourMain car il sera enregistré en Cascade avec entrée

        // on enregistre dans la BDD
        Entree nouvelleEntree = new Entree(
                tourActuel.compteActions,
                action,
                tourMainActuel,
                situation,
                (float) stackEffectif / montantBB,
                joueurAction.joueurBDD,
                joueurAction.cartesJoueur,
                (float) joueurAction.stackActuel / montantBB,
                (float) potAncien / montantBB,
                (float) potActuel / montantBB,
                (float) montantCall / montantBB,
                potBounty
        );
        session.persist(nouvelleEntree);
        transaction.commit();
        RequetesBDD.fermerSession();

        int montantPaye = joueurAction.ajouterMise(betSupplementaire);
        assert (montantPaye == betSupplementaire);
        tourActuel.dernierBet = Math.max(tourActuel.dernierBet, joueurAction.montantActuel);

        potActuel += montantPaye;
        if (action.estFold()) joueurAction.setCouche(true);
        joueurAction.nActions++;
    }

    public void ajouterGains(String nomJoueur, int gains) {
        JoueurInfo joueur = selectionnerJoueur(nomJoueur);
        joueur.gains = gains;
    }

    public void ajouterCartes(String nomJoueur, Combo combo) {
        JoueurInfo joueur = selectionnerJoueur(nomJoueur);
        joueur.cartesJoueur = combo.toInt();

        if (Objects.equals(nomJoueur, nomHero)) mainEnregistree.setCartesHero(combo.toInt());
    }

    public void ajouterShowdown(boolean showdown) {
        mainEnregistree.setShowdown(showdown);
    }

    public void enregistrerGains() {
        corrigerGains();

        RequetesBDD.ouvrirSession();
        Session session = RequetesBDD.getSession();
        Transaction transaction = session.beginTransaction();

        List<Float> resultats = new ArrayList<>();

        for (JoueurInfo joueurTraite : joueurs) {
            int gains = joueurTraite.gains;
            int depense = joueurTraite.totalInvesti();

            //on ne peut pas perdre plus que la plus grosse mise adverse
            if (gains == 0) {
                for (JoueurInfo joueur : joueurs) {
                    if (joueur != joueurTraite) {
                        if (joueur.totalInvesti() < depense) {
                            depense = joueur.totalInvesti();
                        }
                    }
                }
            }

            float resultatNet = gains - depense;
            resultats.add(resultatNet);

            if (joueurTraite.nActions == 0) {
                logger.fine("Aucune action du joueur");
                GainSansAction gainSansAction = new GainSansAction(
                        joueurTraite.joueurBDD,
                        tourMainActuel,
                        resultatNet
                );
                session.persist(gainSansAction);
            }

            else {
                resultatNet /= joueurTraite.nActions;

                CriteriaBuilder cb = session.getCriteriaBuilder();
                CriteriaQuery<Entree> criteriaQuery = cb.createQuery(Entree.class);
                Root<Entree> actionRoot = criteriaQuery.from(Entree.class);

                Predicate playerPredicate = cb.equal(actionRoot.get("joueur"), joueurTraite.joueurBDD);
                Predicate handPredicate = cb.equal(actionRoot.get("tourMain"), tourMainActuel);

                criteriaQuery.select(actionRoot).where(cb.and(playerPredicate, handPredicate));

                TypedQuery<Entree> query = session.createQuery(criteriaQuery);
                List<Entree> entreesMAJ = query.getResultList();

                for (Entree entree : entreesMAJ) {
                    entree.setValue(resultatNet);
                }
            }
        }

        double sum = resultats.stream().mapToDouble(Float::doubleValue).sum();
        double tolerance = 0.1;
        assert Math.abs(sum) < tolerance : "La somme des gains n'est pas égale à 0";

        transaction.commit();
        RequetesBDD.fermerSession();
    }

    private void corrigerGains() {
        /*
        pour BETCLIC : on rajoute l'exédent misé par chaque gagnant comparé à 2è mise plus élevé
        */
        if (this.room == Variante.PokerRoom.BETCLIC) {
            List<JoueurInfo> winners = new ArrayList<>();
            for (JoueurInfo play : joueurs) {
                if (play.gains > 0) {
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
                    winner.gains += suppBet;
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

        private int stackInitial;
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

        public Joueur getBDD() {
            return joueurBDD;
        }

        public void setCouche(boolean couche) {
            this.couche = couche;
        }

        public int totalInvesti() {
            return montantInvesti + montantActuel;
        }
    }

    private class TourInfo {
        public TourMain.Round nomTour;
        private int nJoueursActifs;
        private int compteActions;

        private int dernierBet;
        private boolean actif;
        private TourInfo(TourMain.Round nomTour, int nJoueursInitiaux) {
            this.nomTour = nomTour;
            this.nJoueursActifs = nJoueursInitiaux;
            this.compteActions = 0;
            this.actif = true;
        }

        private void ajouterAction(Action action) {
            // todo : que fait-on de dernierBet
            compteActions++;
            if (action.estFold()) nJoueursActifs--;
        }

        private void setStatut(boolean actif) {
            this.actif = actif;
        }

        public void setDernierBet(int bet) {
            dernierBet = bet;
        }
    }

}
