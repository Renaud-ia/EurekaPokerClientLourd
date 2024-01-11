package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;
import org.apache.commons.math3.util.Pair;

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
    private final HashMap<Long, List<NoeudAbstrait>> situationsSuivantes;
    private final List<NoeudAbstrait> noeudsArbre;

    public ArbreAbstrait(FormatSolution formatSolution) {
        this.configurationArbre = obtenirConfig(formatSolution.getNomFormat());
        this.formatSolution = formatSolution;
        situationsPrecedentes = new HashMap<>();
        situationsSuivantes = new HashMap<>();
        noeudsArbre = new ArrayList<>();
        genererArbre();
    }

    //interface publique

    // implémenté ici plutôt quand dans le noeud car dépend de si présent dans l'arbre
    // return null si noeud pas présent dans l'arbre
    public NoeudAbstrait noeudPrecedent(NoeudAbstrait noeudAbstrait) {
        return situationsPrecedentes.get(noeudAbstrait.toLong());
    }

    public List<NoeudAbstrait> noeudsSuivants(NoeudAbstrait noeudAbstrait) {
        return situationsSuivantes.get(noeudAbstrait.toLong());
    }

    @Deprecated
    public NoeudAbstrait noeudPlusProche(NoeudAbstrait noeudAbstrait) {
        if (noeudPresent(noeudAbstrait)) return noeudAbstrait;
        NoeudAbstrait noeudPlusProche = null;
        float minDistance = Float.MAX_VALUE;
        for (NoeudAbstrait noeudArbre : noeudsArbre) {
            // pas d'équivalence avec root
            if (noeudArbre == noeudsArbre.get(0)) continue;
            float distance = noeudArbre.distanceNoeud(noeudAbstrait);
            if (distance < minDistance) {
                minDistance = distance;
                noeudPlusProche = noeudArbre;
            }
        }
        return noeudPlusProche;
    }

    // vu que des noeuds vont plus exister, on veut tester tous les noeuds
    public List<NoeudAbstrait> noeudsPlusProches(NoeudAbstrait noeudAbstrait) {
        List<Pair<NoeudAbstrait, Float>> noeudsEtDistances = new ArrayList<>();

        for (NoeudAbstrait noeudArbre : noeudsArbre) {
            // Exclure le nœud racine ou d'autres nœuds si nécessaire
            if (noeudArbre.equals(noeudsArbre.get(0))) continue;

            float distance = noeudArbre.distanceNoeud(noeudAbstrait);
            noeudsEtDistances.add(new Pair<>(noeudArbre, distance));
        }

        // Tri des noeuds par distance
        noeudsEtDistances.sort(Comparator.comparing(Pair::getValue));

        // Récupérer les n premiers noeuds les plus proches
        List<NoeudAbstrait> noeudsPlusProches = new ArrayList<>();
        for (int i = 0; i < noeudsEtDistances.size(); i++) {
            noeudsPlusProches.add(noeudsEtDistances.get(i).getKey());
        }

        return noeudsPlusProches;
    }

    // retourne les entrées triées par la situation (= noeud abstrait précédent)
    // l'action est contenue dans l'Entrée
    public LinkedHashMap<NoeudAbstrait, List<Entree>> trierEntrees(List<Entree> toutesLesSituations) {
        // on trie par ordre croissant de long = plus d'actions
        TreeMap<NoeudAbstrait, List<Entree>> entreesTriees =
                new TreeMap<>(Comparator.comparingLong(NoeudAbstrait::toLong));

        for (Entree entree : toutesLesSituations) {
            // on regroupe par noeud précédent
            NoeudAbstrait noeudAbstrait = new NoeudAbstrait(entree.getIdNoeudTheorique());
            if (!noeudAbstrait.isValide()) continue;
            NoeudAbstrait noeudPrecedent = this.noeudPrecedent(noeudAbstrait);
            // si le noeud n'est pas dans l'arbre il sera null
            if (noeudPrecedent != null) {
                entreesTriees.computeIfAbsent(noeudPrecedent, k -> new ArrayList<>()).add(entree);
            }
        }

        return new LinkedHashMap<>(entreesTriees);
    }

    // retourne les noeuds du round groupés par noeud précédent
    public LinkedHashMap<NoeudAbstrait, List<NoeudAbstrait>> obtenirNoeudsGroupes(TourMain.Round round) {
        // on trie par ordre croissant de long = plus d'actions
        TreeMap<NoeudAbstrait, List<NoeudAbstrait>> entreesTriees =
                new TreeMap<>(Comparator.comparingLong(NoeudAbstrait::toLong));

        for (NoeudAbstrait noeudAction : this.noeudsArbre) {
            if (noeudAction.getRound() != round) continue;
            NoeudAbstrait noeudPrecedent = this.noeudPrecedent(noeudAction);
            // si le noeud n'est pas dans l'arbre il sera null
            if (noeudPrecedent != null) {
                entreesTriees.computeIfAbsent(noeudPrecedent, k -> new ArrayList<>()).add(noeudAction);
            }
        }

        return new LinkedHashMap<>(entreesTriees);
    }

    public List<NoeudAbstrait> obtenirNoeuds() {
        return noeudsArbre;
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
        return switch (pokerFormat) {
            case SPIN -> ConfigurateurArbre.SPIN();
            case MTT -> ConfigurateurArbre.MTT();
            case CASH_GAME -> ConfigurateurArbre.CASH();
            default -> ConfigurateurArbre.DEFAUT();
        };
    }

    private void genererArbre() {
        int nombreJoueurs = formatSolution.getNombreJoueurs();
        if (configurationArbre.headsUpPreflop()) genererRound(TourMain.Round.PREFLOP, 2);
        genererRound(TourMain.Round.PREFLOP, nombreJoueurs);

        int MAX_JOUEURS_FLOP = configurationArbre.maxActionsPreflop();
        for (int i = 2; i <= MAX_JOUEURS_FLOP; i++) {
            genererRound(TourMain.Round.FLOP, i);
        }
    }

    private void genererRound(TourMain.Round round, int nombreJoueurs) {
        List<NoeudAbstrait> noeudsEnAttente = new ArrayList<>();
        NoeudAbstrait noeudInitial = new NoeudAbstrait(nombreJoueurs, round);
        noeudsEnAttente.add(noeudInitial);

        while (!noeudsEnAttente.isEmpty()) {
            NoeudAbstrait noeudTraite = noeudsEnAttente.get(0);
            genererProchainsNoeuds(noeudTraite, noeudsEnAttente);
        }
    }

    private void genererProchainsNoeuds(NoeudAbstrait noeudTraite,
                                        List<NoeudAbstrait> noeudsEnAttente) {
        List<Move> actionsPossibles = this.actionsSuivantes(noeudTraite);

        List<NoeudAbstrait> noeudsSuivants = new ArrayList<>();
        for (Move move : actionsPossibles) {
            NoeudAbstrait nouveauNoeud = noeudTraite.copie();
            nouveauNoeud.ajouterAction(move);
            // todo parfois les actions sont trop longues
            if (nouveauNoeud.isValide()) {
                noeudsEnAttente.add(nouveauNoeud);
                situationsPrecedentes.put(nouveauNoeud.toLong(), noeudTraite);
                noeudsSuivants.add(nouveauNoeud);
            }
        }
        this.situationsSuivantes.put(noeudTraite.toLong(), noeudsSuivants);

        noeudsEnAttente.remove(noeudTraite);
        noeudsArbre.add(noeudTraite);
    }

    // c'est l'arbre qui fixe les conditions des prochaines actions
    private List<Move> actionsSuivantes(NoeudAbstrait noeudTraite) {
        List<Move> actionsPossibles = toutesLesActions();

        if (noeudTraite.isLeaf()) return new ArrayList<>();

        // si trop de joueurs actifs on empêche d'autres joueurs de rentrer dans le coup
        if (noeudTraite.maxActionsAtteint(configurationArbre.maxActionsPreflop())) {
            actionsPossibles.remove(Move.CALL);
            actionsPossibles.remove(Move.RAISE);
            actionsPossibles.remove(Move.ALL_IN);
        } else if (noeudTraite.hasAllin()) {
            actionsPossibles.remove(Move.RAISE);
            actionsPossibles.remove(Move.ALL_IN);
        } else if (noeudTraite.nombreRaise() >= configurationArbre.getNombreReraises(noeudTraite.roundActuel())) {
            actionsPossibles.remove(Move.RAISE);
        }

        return actionsPossibles;
    }

    public List<Move> toutesLesActions() {
        List<Move> actions = List.of(Move.FOLD, Move.CALL, Move.RAISE, Move.ALL_IN);
        return new ArrayList<>(actions);
    }


}
