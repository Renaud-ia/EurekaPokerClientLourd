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

/**
 * un classificateur est crée pour chaque noeud abstrait précédent
 */
public class ClassificateurCumulatif extends Classificateur {
    /**
     * @param entreesSituation entrées correspondant à un même noeud abstrait précédent
     * @param formatSolution
     * @return des noeuds dénombrables
     */
    // valeurs config
    // on fixe minPoints ici car dépend du round
    private final static int MIN_POINTS = 5000;
    private final static float MIN_FREQUENCE_ACTION = 0.01f;
    private static final float MIN_FREQUENCE_BET_SIZE = 0.10f;
    private final static int MIN_EFFECTIF_BET_SIZE = 200;

    // variables associés à l'instance
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
        // on vérifie que le nombre d'entrées est suffisant pour au moins 2 actions
        if (super.situationInvalide(entreesSituation).isEmpty()) return;

        List<ClusterSPRB> clustersSPRB = this.clusteriserSPRB(entreesSituation, MIN_POINTS);

        for (ClusterSPRB clusterGroupe : clustersSPRB) {
            if (clusterGroupe.noeudsPresents().size() < 2) {
                // todo PRODUCTION log critique à encrypter
                logger.error("Une seule action trouvée, ne sera pas pris en compte");
                continue;
            }
            NoeudAbstrait premierNoeud = new NoeudAbstrait(clusterGroupe.getIdPremierNoeud());
            NoeudAbstrait noeudPrecedent = arbreAbstrait.noeudPrecedent(premierNoeud);
            long idNoeudSituation = noeudPrecedent.toLong();

            NoeudDenombrableIso noeudDenombrable = new NoeudDenombrableIso(noeudPrecedent);
            // todo PRODUCTION log critique à supprimer
            logger.debug("#### STACK EFFECTIF #### : " + clusterGroupe.getStackEffectif());

            session = ConnexionBDD.ouvrirSession();
            Transaction transaction = session.beginTransaction();
            session.merge(formatSolution);
            NoeudSituation noeudSituation = new NoeudSituation(formatSolution, profilJoueur,
                    idNoeudSituation, clusterGroupe.getStackEffectif().getIdGenere(),
                    clusterGroupe.getPot(), clusterGroupe.getPotBounty());
            session.persist(noeudSituation);

            // les clusters sont sous-groupés par action
            for (Long idNoeudAction : clusterGroupe.noeudsPresents()) {
                List<Entree> entreesAction = clusterGroupe.obtenirEntrees(idNoeudAction);

                // on vérifie si l'action est assez fréquente
                float frequenceAction = (float) entreesAction.size() / clusterGroupe.getEffectif();
                if (frequenceAction < MIN_FREQUENCE_ACTION) continue;


                NoeudAbstrait noeudAbstraitAction = new NoeudAbstrait(idNoeudAction);
                // todo PRODUCTION log critique à supprimer
                logger.debug("Noeud abstrait : " + noeudAbstraitAction);
                logger.debug("Fréquence de l'action " + frequenceAction);
                logger.debug("Effectif  :" + entreesAction.size());

                if (noeudAbstraitAction.getMove() == Move.RAISE) {
                    // on clusterise les raises par bet size
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
        // todo : il faudrait prévoir le cas quand on traite la range de hero => les autres ranges doivent être celles de VILLAIN
        for (NoeudDenombrable noeudDenombrable : noeudDenombrables) {
            List<Entree> echantillon = noeudDenombrable.obtenirEchantillon();
            RecupRangeIso recuperateurRange = new RecupRangeIso(formatSolution, profilJoueur);
            try {
                OppositionRange oppositionRange = recuperateurRange.recupererRanges(echantillon);
                ((NoeudDenombrableIso) noeudDenombrable).construireCombosPreflop(oppositionRange);
            }
            catch (Exception e) {
                // todo PRODUCTION log critique à encrypter
                logger.error("Erreur lors de la récupération de ranges, les combos ne seront pas construits", e);
                throw new ErreurCritique("Impossible de récupérer les ranges précédentes");
            }
        }
        return true;
    }

    @Override
    public List<NoeudDenombrable> obtenirSituations() {
        return noeudDenombrables;
    }

    /**
     * clusterise par BetSize et crée les noeuds
     */
    private void creerNoeudParBetSize(List<Entree> entreesAction, ClusterSPRB clusterGroupe,
                                      NoeudSituation noeudSituation,
                                      long idNoeudAction,
                                      NoeudDenombrable noeudDenombrable) {
        int minEffectifCluster =
                (int) Math.max(MIN_EFFECTIF_BET_SIZE, entreesAction.size() * MIN_FREQUENCE_BET_SIZE);
        List<ClusterBetSize> clustersSizing = this.clusteriserBetSize(entreesAction, minEffectifCluster);

        if (clustersSizing.isEmpty()) {
            // todo PRODUCTION log critique à encrypter
            logger.error("Aucun betSize choisi par le clustering");
        }

        for (ClusterBetSize clusterBetSize : clustersSizing) {
            // todo PRODUCTION log critique à supprimer
            logger.debug("BETSIZE : " + clusterBetSize.getBetSize());
            logger.debug("POT : " + clusterGroupe.getPot());
            logger.debug("EFFECTIF : " + clusterBetSize.getEffectif());

            // on crée les noeuds actions et on les ajoute avec les entrées dans un noeud dénombrable
            NoeudPreflop noeudPreflop =
                    new NoeudPreflop(noeudSituation, idNoeudAction);
            noeudPreflop.setBetSize(clusterBetSize.getBetSize());
            noeudDenombrable.ajouterNoeud(noeudPreflop, clusterBetSize.getEntrees());

            noeudSituation.getNoeudsActions().add(noeudPreflop);
            session.persist(noeudPreflop);
        }

    }


    /**
     * créer les noeuds sans betSize
     */
    private void creerNoeudSansBetSize(List<Entree> entreesAction, ClusterSPRB clusterGroupe, NoeudSituation noeudSituation,
                                       long idNoeudAction, Move move, NoeudDenombrable noeudDenombrable) {


        // on crée les noeuds actions et on les ajoute avec les entrées dans un noeud dénombrable
        NoeudPreflop noeudPreflop =
                new NoeudPreflop(noeudSituation, idNoeudAction);

        // sinon on crée un noeud
        noeudDenombrable.ajouterNoeud(noeudPreflop, entreesAction);
        noeudSituation.getNoeudsActions().add(noeudPreflop);
        session.persist(noeudPreflop);
    }

}
