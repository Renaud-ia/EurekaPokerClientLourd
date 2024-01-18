package analyzor.modele.simulation;

import analyzor.modele.extraction.EnregistreurPartie;
import analyzor.modele.parties.Joueur;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.TourMain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * objet qui simule le fonctionnement d'une table de poker avec ses règles
 * partagé entre import des mains et simulation pour garantir un fonctionnement homogène
 * peut fonctionner en mode valeur absolue ou mode BB
 * todo : séparer davantage fonctionnalités de import main et de simulation (en particulier refaire des classes internes ?)
 */
public abstract class TablePoker {
    protected final static Logger logger = LogManager.getLogger(TablePoker.class);
    protected final Integer montantBB;
    protected final boolean modeBB;
    protected HashMap<String, JoueurTable> mapJoueursNom;
    protected final PotTable potTable;
    protected JoueurTable joueurActuel;
    private int nombreActions;

    /**
     * @param montantBB : si montant BB est null, ça veut dire que tout est exprimé en BB => mises etc
     * @param modeBB : si modeBB, va retourner tous les résultats exprimés en BB, sinon en valeur absolue
     */
    public TablePoker(Integer montantBB, boolean modeBB) {
        if (montantBB == null && modeBB)
            throw new IllegalArgumentException("Pour avoir les résultats en BB, on doit indiquer un montant de BB");
        this.montantBB = montantBB;
        this.modeBB = true;

        this.mapJoueursNom = new HashMap<>();
        potTable = new PotTable();
        nombreActions = 0;
    }

    // interface publique

    /**
     * passe au tour d'enchères suivant
     * retourne le nombre de mapJoueursNom initiaux
     */
    public int nouveauTour() {
        int nJoueursInitiaux = 0;

        for (JoueurTable joueur : mapJoueursNom.values()) {
            if (!joueur.estCouche()) nJoueursInitiaux++;
            joueur.nouveauTour();
        }

        potTable.nouveauTour(nJoueursInitiaux);

        return nJoueursInitiaux;
    }

    public void ajouterAnte(JoueurTable joueur, float valeurAnte) {
        float valeurReelle = joueur.setAnte(valeurAnte);
        potTable.incrementer(valeurReelle);
    }

    public void ajouterBlindes(JoueurTable joueurBB, JoueurTable joueurSB) {
        int montantPayeBB = (int) joueurBB.ajouterMise(this.montantBB);

        int montantPayeSB;
        if (joueurSB != null) {
            montantPayeSB = (int) joueurSB.ajouterMise(((float) this.montantBB / 2));
        }

        else {
            montantPayeSB = 0;
        }

        potTable.incrementer(montantPayeSB + montantPayeBB);
        potTable.setDernierBet(Math.max(montantPayeSB, montantPayeBB));
    }

    /**
     *
     * @param nomJoueur : nom du joueur qui fait l'action
     * @param betTotal : si vrai, c'est l'ensemble des mises jusqu'à présent, si faux c'est la mise complémentaire
     * attention le montant est indiqué en absolu et pas en relatif
     */
    public void ajouterAction(String nomJoueur, Move move, float betSize, boolean betTotal) {
        JoueurTable joueurAction = selectionnerJoueur(nomJoueur);
        float betSupplementaire;
        if (betSize > 0) {
            if (betTotal) betSupplementaire = betSize - joueurAction.montantInvesti();
            else betSupplementaire = betSize;
        }
        else {
            betSupplementaire = 0;
        }
        this.ajouterAction(joueurAction, move, betSupplementaire);
    }

    /**
     * méthode intern d'ajoute d'une action
     * @param betSupplementaire cela doit être le montant supplémentaire et pas total
     */
    protected void ajouterAction(JoueurTable joueurTable, Move move, float betSupplementaire) {
        float montantPaye = joueurTable.ajouterMise(betSupplementaire);
        if (montantPaye == betSupplementaire) {
            throw new IllegalArgumentException("Le stack du joueur est inférieur au montant qu'il doit payer");
        }
        potTable.setDernierBet(Math.max(potTable.getDernierBet(), joueurTable.montantInvesti()));

        potTable.incrementer(montantPaye);

        if (move == Move.FOLD) {
            joueurTable.setCouche(true);
        }
        nombreActions++;

        joueurActuel = joueurTable;

    }

    // interface de récupération des données

    public List<JoueurTable> getJoueurs() {
        return new ArrayList<>(mapJoueursNom.values());
    }

    /**
     * @return la taille du dernier bet = montant à call
     */
    public float dernierBet() {
        return potTable.getDernierBet();
    }

    /**
     * @return le nombre d'actions total sur tous les rounds
     */
    public int nombreActions() {
        return nombreActions;
    }

    /**
     * on prend le plus gros stack du joueur qui n'est pas le joueur concerné
     * s'il est supérieur au stack du joueur, on prend le stack du joueur
     * pas optimal mais caractérise le "risque" que prend le joueur
     * @return le stack effectif
     */
    public float stackEffectif() {
        float maxStack = 0;
        for (JoueurTable joueur : mapJoueursNom.values()) {
            if (joueur.estCouche() || joueur == joueurActuel) continue;
            if (joueur.getStackActuel() > maxStack) {
                maxStack = joueur.getStackActuel();
            }
        }

        return Math.min(maxStack, joueurActuel.getStackActuel());
    }

    public float getPotBounty() {
        float potBounty = 0f;
        for (TablePoker.JoueurTable joueur : getJoueurs()) {
            if (joueur.estCouche()) continue;
            potBounty += joueur.getBounty() * joueur.totalInvesti() / joueur.getStackInitial();
        }
        return potBounty;
    }

    /**
     * @return le pot total à table (=tous les tours)
     */
    public float getPotTotal() {
        return potTable.potTotal();
    }

    public float getPotActuel() {
        return potTable.potActuel();
    }

    /**
     * @return le pot cumulé des rounds passés
     */
    public float getAncienPot() {
        return potTable.ancienPot();
    }

    public JoueurTable selectionnerJoueur(String nomJoueur) {
        JoueurTable joueurTable = mapJoueursNom.get(nomJoueur);
        if (joueurTable == null) throw new IllegalArgumentException("Joueur non trouvé dans la table : " + nomJoueur);
        return joueurTable;
    }



    /**
     * classe publique pour récupérer les infos sur le joueur
     */
    public class JoueurTable {
        // variables finales
        private final String nom;
        private final Integer siege;
        private float bounty;
        private final Joueur joueurBDD;

        // variable non finales
        private float stackInitial;
        private float stackActuel;
        private int nActions = 0;
        private float investiTourPrecedents = 0;
        private float investiCeTour = 0;
        private int gains = 0;
        private int cartesJoueur;
        private boolean couche;
        private int position;
        private int anteInvesti = 0;
        // todo n'est pas utile pour import mains juste pour moteurJeu
        private boolean hero;

        public JoueurTable(String nom, int siege, int stack, float bounty, Joueur joueurBDD) {
            this.nom = nom;
            this.siege = siege;
            this.bounty = bounty;
            this.joueurBDD = joueurBDD;
            this.stackInitial = stack;
            this.gains = 0;
        }

        public JoueurTable(String nom, float stackDepart, float bounty) {
            this.nom = nom;
            this.stackInitial = stackDepart;
            this.stackActuel = stackDepart;
            this.bounty = bounty;
            this.siege = null;
            this.joueurBDD = null;
            this.hero = false;
        }

        // actions sur le joueur

        public void setCartes(int combo) {
            this.cartesJoueur = combo;
        }

        public float setAnte(float valeurAnte) {
            float antePose = Math.max(valeurAnte, stackActuel);
            this.investiCeTour += antePose;
            return antePose;
        }

        // important : on doit indiquer le montant de mise SUPPLEMENTAIRE
        public float ajouterMise(float miseSupplementaire) {
            // incrémenter le nombre d'actions
            float miseReelle = Math.max(miseSupplementaire, stackInitial);
            this.investiCeTour += miseReelle;
            this.nActions++;
            return miseReelle;
        }

        public void setCouche(boolean couche) {
            this.couche = couche;
        }

        public void setGains(int gains) {
            this.gains = gains;
        }

        public void ajouterGains(int suppBet) {
            this.gains += gains;
        }

        // récupération des infos

        public float nActions() {
            return nActions;
        }

        public float totalInvesti() {
            return investiCeTour + investiTourPrecedents;
        }

        public int gains() {
            return gains;
        }

        public Joueur getJoueurBDD() {
            return joueurBDD;
        }

        public int cartesJoueur() {
            return cartesJoueur;
        }

        public float getStackActuel() {
            return stackActuel;
        }

        public boolean estCouche() {
            return couche;
        }

        public void nouveauTour() {
            this.investiTourPrecedents += this.investiCeTour;
            this.investiCeTour = 0;
        }

        public float montantInvesti() {
            return investiCeTour;
        }

        public float getBounty() {
            return bounty;
        }

        public float getStackInitial() {
            return stackInitial;
        }

        public boolean estHero() {
            return hero;
        }

        public void setStack(float stack) {
            this.stackActuel = stack;
        }

        public String getNom() {
            return nom;
        }

        public void setStackDepart(float stack) {
            this.stackInitial = stack;
        }

        public void setBounty(float bounty) {
            this.bounty = bounty;
        }

        public void setHero(boolean hero) {
            this.hero = hero;
        }
    }


    protected class PotTable {
        private float potAncien;
        private float potActuel;
        private float dernierBet;
        private TourMain.Round roundActuel;

        public PotTable() {
            this.potAncien = 0;
            this.potActuel = 0;
            this.dernierBet = 0;
            this.roundActuel = null;
        }

        public void nouveauTour(int nJoueursInitiaux) {
            potAncien += potActuel;
            potActuel = 0;

            if (roundActuel == null) {
                roundActuel = TourMain.Round.PREFLOP;
            }
            else {
                roundActuel.suivant();
            }
        }

        public void incrementer(float valeurReelle) {
            this.potActuel += valeurReelle;
            logger.trace("Pot incréménté de : " + valeurReelle + ", vaut maintenant : " + potTotal());
        }

        public void setDernierBet(float valeur) {
            this.dernierBet = valeur;
        }

        public float getDernierBet() {
            return dernierBet;
        }

        public float potTotal() {
            return potActuel + potAncien;
        }

        public float ancienPot() {
            return potAncien;
        }

        public float potActuel() {
            return potActuel;
        }

        public void reset() {
            this.potActuel = 0;
            this.potAncien = 0;
        }
    }
    
}
