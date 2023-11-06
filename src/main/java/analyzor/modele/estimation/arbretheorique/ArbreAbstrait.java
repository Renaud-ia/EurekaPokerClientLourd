package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Entree;
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

    }
}
