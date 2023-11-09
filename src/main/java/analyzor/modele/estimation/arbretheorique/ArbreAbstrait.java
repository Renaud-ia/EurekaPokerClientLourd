package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;

import java.util.*;

/**
 * génère un arbre théorique à partir de séquences d'action (sans BETSIZE)
 * garde en mémoire
 * on le recrée à chaque fois qu'on en a besoin
 * configurable (nombre de relances)
 * flop, turn et river (=> rounds indépendants)
 * configurationArbre
 */
public class ArbreAbstrait {
    private final ConfigurationArbre configurationArbre;
    private final FormatSolution formatSolution;
    private final HashMap<Long, NoeudAbstrait> situationsPrecedentes;
    private final List<NoeudAbstrait> noeudsArbre;
    public ArbreAbstrait(FormatSolution formatSolution) {
        this.configurationArbre = obtenirConfig(formatSolution.getNomFormat());
        this.formatSolution = formatSolution;
        situationsPrecedentes = new HashMap<>();
        noeudsArbre = new ArrayList<>();
        genererArbre();
    }

    //interface publique

    // implémenté ici plutôt quand dans le noeud car dépend de si présent dans l'arbre

    // return null si noeud pas présent dans l'arbre
    public NoeudAbstrait noeudPrecedent(NoeudAbstrait noeudAbstrait) {
        return situationsPrecedentes.get(noeudAbstrait.toLong());
    }

    public NoeudAbstrait noeudPlusProche(NoeudAbstrait noeudAbstrait) {
        if (noeudPresent(noeudAbstrait)) return noeudAbstrait;
        NoeudAbstrait noeudPlusProche = null;
        float minDistance = 100000;
        for (NoeudAbstrait noeudArbre : noeudsArbre) {
            float distance = noeudArbre.distanceNoeud(noeudAbstrait);
            if (distance < minDistance) {
                minDistance = distance;
                noeudPlusProche = noeudArbre;
            }
        }
        return noeudPlusProche;
    }

    // retourne les entrées triées par la situation (= noeud abstrait précédent)
    // l'action est contenue dans l'Entrée
    public LinkedHashMap<NoeudAbstrait, List<Entree>> obtenirEntrees(TourMain.Round round) {
        //todo
        // important d'abord trier les noeuds par nombre d'actions
        //puis faire des sous-groupes par noeud abstrait précédent
        return new LinkedHashMap<>();
    }

    // méthodes privées

    private boolean noeudPresent(NoeudAbstrait noeudAbstrait) {
        for (NoeudAbstrait noeudArbre : noeudsArbre) {
            if (noeudArbre.equals(noeudAbstrait)) {
                return true;
            }
        }
        return false;
    }

    private ConfigurationArbre obtenirConfig(Variante.PokerFormat pokerFormat) {
        switch (pokerFormat) {
            case SPIN:
                return ConfigurateurArbre.SPIN();
            case MTT:
                return ConfigurateurArbre.MTT();
            case CASH_GAME:
                return ConfigurateurArbre.CASH();
            default:
                return ConfigurateurArbre.DEFAUT();
        }
    }

    private void genererArbre() {
        int nombreJoueurs = formatSolution.getNombreJoueurs();
        genererRound(TourMain.Round.PREFLOP, nombreJoueurs);

        // todo : devrait être dans configuration de l'arbre ??
        int MAX_JOUEURS_FLOP = 3;
        genererRound(TourMain.Round.FLOP, MAX_JOUEURS_FLOP);
    }

    private void genererRound(TourMain.Round round, int nombreJoueurs) {
        List<NoeudAbstrait> noeudsEnAttente = new ArrayList<>();
        NoeudAbstrait noeudInitial = new NoeudAbstrait(nombreJoueurs, round);
        noeudsEnAttente.add(noeudInitial);

        while(!noeudsEnAttente.isEmpty()) {
            NoeudAbstrait noeudTraite = noeudsEnAttente.get(0);
            genererProchainsNoeuds(noeudTraite, noeudsEnAttente);
        }
    }

    private void genererProchainsNoeuds(NoeudAbstrait noeudTraite,
                                        List<NoeudAbstrait> noeudsEnAttente) {
        List<Move> actionsPossibles = this.actionsSuivantes(noeudTraite);

        for (Move move : actionsPossibles) {
            NoeudAbstrait nouveauNoeud = noeudTraite.copie();
            nouveauNoeud.ajouterAction(move);
            noeudsEnAttente.add(nouveauNoeud);

            situationsPrecedentes.put(nouveauNoeud.toLong(), noeudTraite);
        }

        noeudsEnAttente.remove(noeudTraite);
        noeudsArbre.add(noeudTraite);
    }

    private List<Move> actionsSuivantes(NoeudAbstrait noeudTraite) {
        //todo
        return new ArrayList<>();
    }
}
