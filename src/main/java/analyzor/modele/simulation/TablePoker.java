package analyzor.modele.simulation;

import analyzor.modele.parties.Joueur;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.TourMain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public abstract class TablePoker {
    protected final Float montantBB;
    protected HashMap<String, JoueurTable> mapJoueursNom;
    protected final PotTable potTable;
    protected JoueurTable joueurActuel;
    private int nombreActions;

    protected TablePoker(float montantBB) {
        this.montantBB = montantBB;

        this.mapJoueursNom = new HashMap<>();
        potTable = new PotTable();
        nombreActions = 0;
    }

    protected void reset() {
        potTable.reset();
        nombreActions = 0;
    }



    

    
    public int nouveauTour() {
        int nJoueursInitiaux = 0;

        for (JoueurTable joueur : mapJoueursNom.values()) {
            if (!joueur.estCouche()) nJoueursInitiaux++;
            joueur.nouveauTour();
        }

        potTable.nouveauTour(nJoueursInitiaux);

        return nJoueursInitiaux;
    }

    protected void ajouterJoueur(String nom, JoueurTable nouveauJoueur) {
        mapJoueursNom.put(nom, nouveauJoueur);
    }

    protected void ajouterAnte(JoueurTable joueur, float valeurAnte) {
        float valeurReelle = joueur.setAnte(valeurAnte);
        potTable.ajouterAnte(valeurReelle);
    }

    protected void ajouterBlindes(JoueurTable joueurBB, JoueurTable joueurSB) {
        float montantPayeBB = joueurBB.setBlinde(this.montantBB);

        float montantPayeSB;
        if (joueurSB != null) {
            montantPayeSB = joueurSB.setBlinde(((float) this.montantBB / 2));
        }

        else {
            montantPayeSB = 0;
        }

        potTable.incrementer(montantPayeSB + montantPayeBB);
        potTable.setDernierBet(Math.max(montantPayeSB, montantPayeBB));
    }

    
    protected float ajouterAction(JoueurTable joueurAction, Move move, float betSize, boolean betTotal) {

        float betSupplementaire = normaliserBetSize(joueurAction, betSize, betTotal);

        return this.ajouterAction(joueurAction, move, betSupplementaire);
    }

    
    protected float normaliserBetSize(JoueurTable joueurTable, float betSize, boolean betTotal) {
        float betSupplementaire;
        if (betSize > 0) {
            if (betTotal) betSupplementaire = betSize - joueurTable.investiCeTour();
            else betSupplementaire = betSize;
        }
        else {
            betSupplementaire = 0;
        }
        return betSupplementaire;
    }

    
    protected float ajouterAction(JoueurTable joueurTable, Move move, float betSupplementaire) {
        float montantPaye = joueurTable.ajouterMise(betSupplementaire);

        
        if (((montantPaye - betSupplementaire) / betSupplementaire) > 0.01f) {
            throw new IllegalArgumentException(
                    "Le stack du joueur est inférieur au montant qu'il doit payer : " + montantPaye + ", " + betSupplementaire);
        }
        potTable.setDernierBet(Math.max(potTable.getDernierBet(), joueurTable.investiCeTour()));

        potTable.incrementer(montantPaye);

        if (move == Move.FOLD) {
            joueurTable.setCouche(true);
        }
        nombreActions++;

        joueurActuel = joueurTable;

        return betSupplementaire;
    }


    

    public List<JoueurTable> getJoueurs() {
        return new ArrayList<>(mapJoueursNom.values());
    }

    
    public float dernierBet() {
        return potTable.getDernierBet();
    }

    
    public int nombreActions() {
        return nombreActions;
    }

    
    public StacksEffectifs stackEffectif() {
        DeuxPremiersStacksEffectifs stacksEffectifs =
                new DeuxPremiersStacksEffectifs((joueurActuel.getStackActuel() / montantBB), nombreJoueursActifs());
        for (JoueurTable joueur : mapJoueursNom.values()) {
            if (joueur.estCouche() || joueur == joueurActuel) continue;
            float stackPrisEnCompte;

            
            
            if (joueur.getStackActuel() == 0) {
                stackPrisEnCompte = joueur.getStackInitial();
            }
            else stackPrisEnCompte = joueur.getStackActuel();

            stacksEffectifs.ajouterStackVillain((stackPrisEnCompte / montantBB));
        }

        return stacksEffectifs;
    }

    public int nombreJoueursActifs() {
        int nJoueursActifs = 0;
        for (JoueurTable joueurTable : getJoueurs()) {
            if (!joueurTable.estCouche() && !joueurTable.estAllIn()) nJoueursActifs++;
        }

        return nJoueursActifs;
    }

    public float getPotBounty() {
        float potBounty = 0f;
        for (TablePoker.JoueurTable joueur : getJoueurs()) {
            
            
            if (joueur.estCouche() || joueur == joueurActuel
                    || joueur.getStackInitial() > joueurActuel.getStackInitial()) continue;

            
            if (joueur.getStackInitial() > 0) {
                potBounty += joueur.getBounty() * joueur.totalInvesti() / joueur.getStackInitial();
            }
            else potBounty+= joueur.getBounty();
        }
        return potBounty;
    }

    
    public float getPotTotal() {
        return potTable.potTotal();
    }

    public JoueurTable selectionnerJoueur(String nomJoueur) {
        JoueurTable joueurTable = mapJoueursNom.get(nomJoueur);
        if (joueurTable == null)
            throw new IllegalArgumentException("Joueur non trouvé dans la table : " + nomJoueur +
                    ", joueurs : " + getJoueurs());
        return joueurTable;
    }

    public void poserAntes(float valeurAnte) {
        for (JoueurTable joueurTable : getJoueurs()) {
            this.ajouterAnte(joueurTable, valeurAnte);
        }
    }

    public TourMain.Round tourActuel() {
        return potTable.roundActuel;
    }

    public float getPotAnte() {
        return potTable.getPotAnte();
    }


    
    public class JoueurTable {
        
        private final String nom;
        private final Integer siege;
        private float bounty;
        private final Joueur joueurBDD;

        
        private float stackInitial;
        private float stackActuel;
        private int nActions = 0;
        private float investiTourPrecedents = 0;
        private float investiCeTour = 0;
        private float gains = 0;
        private int cartesJoueur;
        private boolean couche;
        private int position;
        private float anteInvesti = 0;
        
        private boolean hero;

        public JoueurTable(String nom, int siege, float stack, float bounty, Joueur joueurBDD) {
            this.nom = nom;
            this.siege = siege;
            this.bounty = bounty;
            this.joueurBDD = joueurBDD;
            this.stackInitial = stack;
            this.stackActuel = stack;
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

        

        public void setCartes(int combo) {
            this.cartesJoueur = combo;
        }

        
        
        private float setAnte(float valeurAnte) {
            float montantReel = Math.min(valeurAnte, stackActuel);
            this.stackActuel -= montantReel;
            this.anteInvesti = montantReel;

            return montantReel;
        }

        public float setBlinde(float valeurBlinde) {
            return deduireStack(valeurBlinde);
        }

        
        private float ajouterMise(float miseSupplementaire) {
            
            float miseReelle = deduireStack(miseSupplementaire);
            this.nActions++;
            return miseReelle;
        }

        private float deduireStack(float montant) {
            float montantReel = Math.min(montant, stackActuel);
            this.investiCeTour += montantReel;
            this.stackActuel -= montantReel;

            return montantReel;
        }

        public void setCouche(boolean couche) {
            this.couche = couche;
        }

        public void setGains(float gains) {
            this.gains = gains;
        }

        public void ajouterGains(float suppBet) {
            this.gains += suppBet;
        }

        

        public float nActions() {
            return nActions;
        }

        public float totalInvesti() {
            return investiCeTour + investiTourPrecedents;
        }

        public float gains() {
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

        public float investiCeTour() {
            return investiCeTour;
        }

        public float anteInvestie() {
            return anteInvesti;
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
            this.stackActuel = stack;
        }

        public void setBounty(float bounty) {
            this.bounty = bounty;
        }

        public void setHero(boolean hero) {
            this.hero = hero;
        }

        public void setMontantInvesti(float dejaInvesti) {
            this.investiTourPrecedents = 0;
            this.investiCeTour = dejaInvesti;
        }

        public void reset() {
            this.investiCeTour = 0;
            this.investiTourPrecedents = 0;
            this.couche = false;
        }

        public boolean estAllIn() {
            return (stackActuel == 0);
        }
    }


    protected class PotTable {
        private float potAncien;
        private float potActuel;
        private float dernierBet;
        private float potAnte;
        private TourMain.Round roundActuel;

        public PotTable() {
            this.potAncien = 0;
            this.potActuel = 0;
            this.potAnte = 0;
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
            this.potAnte = 0;
            this.dernierBet = 0;
            roundActuel = null;
        }

        public float getPotAnte() {
            return potAnte;
        }

        public void ajouterAnte(float valeurAnte) {
            this.incrementer(valeurAnte);
            potAnte += valeurAnte;
        }
    }
    
}
