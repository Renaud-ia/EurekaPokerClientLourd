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

        this.tablePoker = new TableImport(montantBB);


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



    public void ajouterJoueur(String nom, int siege, float stack, float bounty) {
        Joueur joueurBDD = ObjetUnique.joueur(nom, session);


        ProfilJoueur profilJoueur;
        if (nom.equals(nomHero)) profilJoueur = ObjetUnique.selectionnerHero();
        else profilJoueur = ObjetUnique.selectionnerVillain();

        joueurBDD.addProfil(profilJoueur);
        session.merge(joueurBDD);

        tablePoker.ajouterJoueur(nom, siege, stack, bounty, joueurBDD);
    }


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



        int nJoueursTable = tablePoker.getJoueurs().size();
        if (tablePoker.tourActuel() == TourMain.Round.PREFLOP && nJoueursTable > 2) {
            while (nJoueursTable < nombreJoueursVariante) {
                generateurId.ajouterAction(Move.FOLD);
                nJoueursTable++;
            }
        }
    }

    public void ajouterAction(Action action, String nomJoueur, boolean betTotal) throws InformationsIncorrectes {
        ajouterAction(action, nomJoueur, betTotal, false);
    }


    public void ajouterAction(Action action, String nomJoueur, boolean betTotal, boolean betComplet)
            throws InformationsIncorrectes {



        if (!betComplet) {
            action.augmenterBet(tablePoker.dernierBet());
            betTotal = true;
        }


        TablePoker.JoueurTable joueurAction = tablePoker.setJoueur(nomJoueur);


        long stackEffectif = tablePoker.stackEffectif().getIdGenere();
        float potBounty = tablePoker.getPotBounty();

        if (buyIn > 0) {
            potBounty /= buyIn;
        }
        float stackJoueur = tablePoker.getStackJoueur(nomJoueur) / tablePoker.getMontantBB();



        float potAvantAction = tablePoker.getPotTotal() - tablePoker.getPotAnte();
        if (potAvantAction < 0) throw new InformationsIncorrectes("Le pot est inférieur à zéro");

        float potActuel = potAvantAction / tablePoker.getMontantBB();




        Action actionCorrigee = tablePoker.ajouterAction(nomJoueur, action.getMove(), action.getBetSize(), betTotal);
        generateurId.ajouterAction(actionCorrigee.getMove());




        float relativeBetSize = actionCorrigee.getBetSize() / potAvantAction;



        Entree nouvelleEntree = new Entree(
                tablePoker.nombreActions(),
                tourMainActuel,
                generateurId.toLong(),
                relativeBetSize,
                stackEffectif,
                joueurAction.getJoueurBDD(),
                stackJoueur,
                potActuel,
                potBounty
        );
        tourMainActuel.getEntrees().add(nouvelleEntree);
        entreesSauvegardees.add(nouvelleEntree);
        session.merge(nouvelleEntree);
    }

    public void ajouterGains(String nomJoueur, float gains) {
        tablePoker.ajouterGains(nomJoueur, gains);
    }

    public void ajouterCartes(String nomJoueur, ComboReel combo) {
        tablePoker.ajouterCartes(nomJoueur, combo.toInt());
    }


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



    private void enregistrerGains() throws ErreurImportation {
        corrigerGains();

        List<Float> resultats = new ArrayList<>();

        for (TablePoker.JoueurTable joueurTraite : tablePoker.getJoueurs()) {
            float gains = joueurTraite.gains();
            float depense = joueurTraite.totalInvesti();


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


            depense += joueurTraite.anteInvestie();

            float resultatNet = gains - depense;
            resultats.add(resultatNet);

            if (joueurTraite.nActions() == 0) {
                GainSansAction gainSansAction = new GainSansAction(
                        joueurTraite.getJoueurBDD(),
                        tourMainActuel,
                        resultatNet / tablePoker.getMontantBB()
                );
                session.persist(gainSansAction);
            }

            else {
                resultatNet /= joueurTraite.nActions();

                for (Entree entree : entreesSauvegardees) {
                    if (entree.getJoueur() == joueurTraite.getJoueurBDD()) {
                        entree.setValue(resultatNet / tablePoker.getMontantBB());



                        if (joueurTraite.cartesJoueur() != 0) entree.setCartes(joueurTraite.cartesJoueur());
                        session.merge(entree);
                    }
                }

            }
        }

        double sum = resultats.stream().mapToDouble(Float::doubleValue).sum();

        double tolerance = 0.10f * tablePoker.getPotTotal();
        if (sum != 0) {
            if ((Math.abs(sum) - rake) > tolerance) {
                throw new InformationsIncorrectes("La somme des gains n'est pas égale à 0 " + (Math.abs(sum) - rake) +
                        ", main n° : " + mainEnregistree.getIdNonUnique() +
                        ", tolérance : " + tolerance);
            }
        }

    }

    private void corrigerGains() {

        if (this.room == PokerRoom.IPOKER) {

            for (TablePoker.JoueurTable winner : tablePoker.getJoueurs()) {

                if (!(winner.gains() > 0)) continue;
                float maxOtherBet = 0;
                for (TablePoker.JoueurTable play : tablePoker.getJoueurs()) {
                    if (play != winner) {
                        if (play.totalInvesti() > maxOtherBet) {
                            maxOtherBet = play.totalInvesti();
                        }
                    }
                }

                float suppGains = winner.totalInvesti() - maxOtherBet;

                if (suppGains > 0) winner.ajouterGains(suppGains);
            }
        }

    }

}
