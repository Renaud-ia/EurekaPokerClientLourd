package analyzor.modele.arbre.classificateurs;

import analyzor.modele.arbre.noeuds.NoeudSituation;
import analyzor.modele.bdd.ConnexionBDD;
import analyzor.modele.denombrement.NoeudDenombrable;
import analyzor.modele.arbre.RecupRangeIso;
import analyzor.modele.arbre.noeuds.NoeudPreflop;
import analyzor.modele.clustering.cluster.ClusterBetSize;
import analyzor.modele.clustering.cluster.ClusterSPRB;
import analyzor.modele.denombrement.NoeudDenombrableIso;
import analyzor.modele.estimation.CalculInterrompu;
import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.estimation.arbretheorique.NoeudAbstrait;
import analyzor.modele.exceptions.ErreurCritique;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.ProfilJoueur;
import analyzor.modele.poker.evaluation.OppositionRange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ClassificateurCumulatif extends Classificateur {



    private final static int MIN_POINTS = 5000;
    private final static int MAX_CLUSTERS_SITUATIONS = 15;
    private final static float MIN_FREQUENCE_ACTION = 0.01f;
    private static final float MIN_FREQUENCE_BET_SIZE = 0.10f;
    private final static int MIN_EFFECTIF_BET_SIZE = 200;


    private final Random random;
    private final List<NoeudDenombrable> noeudDenombrables;
    private final ProfilJoueur profilJoueur;
    private Session session;

    public ClassificateurCumulatif(FormatSolution formatSolution, ProfilJoueur profilJoueur) {
        super(formatSolution);
        this.random = new Random();
        this.noeudDenombrables = new ArrayList<>();
        this.profilJoueur = profilJoueur;
    }

    @Override
    public void creerSituations(List<Entree> entreesSituation) throws CalculInterrompu {

        if (super.situationInvalide(entreesSituation).isEmpty()) return;

        int minPoints = Math.max(entreesSituation.size() / MAX_CLUSTERS_SITUATIONS, MIN_POINTS);

        List<ClusterSPRB> clustersSPRB = this.clusteriserSPRB(entreesSituation, minPoints);

        for (ClusterSPRB clusterGroupe : clustersSPRB) {
            if (clusterGroupe.noeudsPresents().size() < 2) {
                continue;
            }
            NoeudAbstrait premierNoeud = new NoeudAbstrait(clusterGroupe.getIdPremierNoeud());
            NoeudAbstrait noeudPrecedent = arbreAbstrait.noeudPrecedent(premierNoeud);
            long idNoeudSituation = noeudPrecedent.toLong();

            NoeudDenombrableIso noeudDenombrable = new NoeudDenombrableIso(noeudPrecedent);

            session = ConnexionBDD.ouvrirSession();
            Transaction transaction = session.beginTransaction();
            session.merge(formatSolution);
            NoeudSituation noeudSituation = new NoeudSituation(formatSolution, profilJoueur,
                    idNoeudSituation, clusterGroupe.getStackEffectif().getIdGenere(),
                    clusterGroupe.getPot(), clusterGroupe.getPotBounty());
            session.persist(noeudSituation);


            for (Long idNoeudAction : clusterGroupe.noeudsPresents()) {
                List<Entree> entreesAction = clusterGroupe.obtenirEntrees(idNoeudAction);


                float frequenceAction = (float) entreesAction.size() / clusterGroupe.getEffectif();
                if (frequenceAction < MIN_FREQUENCE_ACTION) continue;


                NoeudAbstrait noeudAbstraitAction = new NoeudAbstrait(idNoeudAction);

                if (noeudAbstraitAction.getMove() == Move.RAISE) {

                    creerNoeudParBetSize(entreesAction, clusterGroupe, noeudSituation, idNoeudAction, noeudDenombrable);
                }
                else {
                    creerNoeudSansBetSize(entreesAction, clusterGroupe, noeudSituation, idNoeudAction,
                            noeudAbstraitAction.getMove(), noeudDenombrable);
                }

            }
            noeudDenombrables.add(noeudDenombrable);

            transaction.commit();
            ConnexionBDD.fermerSession(session);
        }
    }

    @Override
    public boolean construireCombosDenombrables() {

        for (NoeudDenombrable noeudDenombrable : noeudDenombrables) {
            List<Entree> echantillon = noeudDenombrable.obtenirEchantillon();
            RecupRangeIso recuperateurRange = new RecupRangeIso(formatSolution, profilJoueur);
            try {
                OppositionRange oppositionRange = recuperateurRange.recupererRanges(echantillon);
                ((NoeudDenombrableIso) noeudDenombrable).construireCombosPreflop(oppositionRange);
            }
            catch (Exception e) {
                throw new ErreurCritique("EA2");
            }
        }
        return true;
    }

    @Override
    public List<NoeudDenombrable> obtenirSituations() {
        return noeudDenombrables;
    }


    private void creerNoeudParBetSize(List<Entree> entreesAction, ClusterSPRB clusterGroupe,
                                      NoeudSituation noeudSituation,
                                      long idNoeudAction,
                                      NoeudDenombrable noeudDenombrable) {
        int minEffectifCluster =
                (int) Math.max(MIN_EFFECTIF_BET_SIZE, entreesAction.size() * MIN_FREQUENCE_BET_SIZE);
        List<ClusterBetSize> clustersSizing = this.clusteriserBetSize(entreesAction, minEffectifCluster);

        for (ClusterBetSize clusterBetSize : clustersSizing) {

            NoeudPreflop noeudPreflop =
                    new NoeudPreflop(noeudSituation, idNoeudAction);
            noeudPreflop.setBetSize(clusterBetSize.getBetSize());
            noeudDenombrable.ajouterNoeud(noeudPreflop, clusterBetSize.getEntrees());

            noeudSituation.getNoeudsActions().add(noeudPreflop);
            session.persist(noeudPreflop);
        }

    }



    private void creerNoeudSansBetSize(List<Entree> entreesAction, ClusterSPRB clusterGroupe, NoeudSituation noeudSituation,
                                       long idNoeudAction, Move move, NoeudDenombrable noeudDenombrable) {



        NoeudPreflop noeudPreflop =
                new NoeudPreflop(noeudSituation, idNoeudAction);


        noeudDenombrable.ajouterNoeud(noeudPreflop, entreesAction);
        noeudSituation.getNoeudsActions().add(noeudPreflop);
        session.persist(noeudPreflop);
    }

}
