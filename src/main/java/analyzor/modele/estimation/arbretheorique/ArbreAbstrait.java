package analyzor.modele.estimation.arbretheorique;

import analyzor.modele.estimation.FormatSolution;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.Move;
import analyzor.modele.parties.TourMain;
import analyzor.modele.parties.Variante;
import org.apache.commons.math3.util.Pair;

import java.util.*;


public class ArbreAbstrait {
    private final ConfigurationArbre configurationArbre;
    private final FormatSolution formatSolution;
    private final HashMap<Long, NoeudAbstrait> situationsPrecedentes;
    private final HashMap<Long, List<NoeudAbstrait>> situationsSuivantes;
    private final List<NoeudAbstrait> noeudsArbre;

    public ArbreAbstrait(FormatSolution formatSolution) {
        this.configurationArbre = obtenirConfig(formatSolution.getPokerFormat());
        this.formatSolution = formatSolution;
        situationsPrecedentes = new HashMap<>();
        situationsSuivantes = new HashMap<>();
        noeudsArbre = new ArrayList<>();
        genererArbre();
    }

    

    
    
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
            
            if (noeudArbre == noeudsArbre.getFirst()) continue;
            float distance = noeudArbre.distanceNoeud(noeudAbstrait);
            if (distance < minDistance) {
                minDistance = distance;
                noeudPlusProche = noeudArbre;
            }
        }
        return noeudPlusProche;
    }

    
    public List<NoeudAbstrait> noeudsPlusProches(NoeudAbstrait noeudAbstrait) {
        List<Pair<NoeudAbstrait, Float>> noeudsEtDistances = new ArrayList<>();

        for (NoeudAbstrait noeudArbre : noeudsArbre) {
            
            if (noeudArbre.equals(noeudsArbre.getFirst())) continue;

            float distance = noeudArbre.distanceNoeud(noeudAbstrait);
            noeudsEtDistances.add(new Pair<>(noeudArbre, distance));
        }

        
        noeudsEtDistances.sort(Comparator.comparing(Pair::getValue));

        
        List<NoeudAbstrait> noeudsPlusProches = new ArrayList<>();
        for (int i = 0; i < noeudsEtDistances.size(); i++) {
            noeudsPlusProches.add(noeudsEtDistances.get(i).getKey());
        }

        return noeudsPlusProches;
    }

    
    
    public LinkedHashMap<NoeudAbstrait, List<Entree>> trierEntrees(List<Entree> toutesLesSituations) {
        
        TreeMap<NoeudAbstrait, List<Entree>> entreesTriees =
                new TreeMap<>(Comparator.comparingLong(NoeudAbstrait::toLong));

        for (Entree entree : toutesLesSituations) {
            
            NoeudAbstrait noeudAbstrait = new NoeudAbstrait(entree.getIdNoeudTheorique());
            if (!noeudAbstrait.isValide()) continue;
            NoeudAbstrait noeudPrecedent = this.noeudPrecedent(noeudAbstrait);
            
            if (noeudPrecedent != null) {
                entreesTriees.computeIfAbsent(noeudPrecedent, k -> new ArrayList<>()).add(entree);
            }
        }

        return new LinkedHashMap<>(entreesTriees);
    }

    
    public LinkedHashMap<NoeudAbstrait, List<NoeudAbstrait>> obtenirNoeudsGroupes(TourMain.Round round) {
        
        TreeMap<NoeudAbstrait, List<NoeudAbstrait>> entreesTriees =
                new TreeMap<>(Comparator.comparingLong(NoeudAbstrait::toLong));

        for (NoeudAbstrait noeudAction : this.noeudsArbre) {
            if (noeudAction.getRound() != round) continue;
            NoeudAbstrait noeudPrecedent = this.noeudPrecedent(noeudAction);
            
            if (noeudPrecedent != null) {
                entreesTriees.computeIfAbsent(noeudPrecedent, k -> new ArrayList<>()).add(noeudAction);
            }
        }

        return new LinkedHashMap<>(entreesTriees);
    }

    public List<NoeudAbstrait> obtenirNoeuds() {
        return noeudsArbre;
    }

    

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

    
    private List<Move> actionsSuivantes(NoeudAbstrait noeudTraite) {
        List<Move> actionsPossibles = toutesLesActions();

        if (noeudTraite.isLeaf()) return new ArrayList<>();

        
        if (noeudTraite.maxActionsAtteint(configurationArbre.maxActionsPreflop())) {
            actionsPossibles.remove(Move.CALL);
            actionsPossibles.remove(Move.RAISE);
            actionsPossibles.remove(Move.ALL_IN);
        }

        else if (noeudTraite.hasAllin()) {
            actionsPossibles.remove(Move.RAISE);
            actionsPossibles.remove(Move.ALL_IN);
        }

        else if (noeudTraite.nombreRaise() >= configurationArbre.getNombreReraises(noeudTraite.roundActuel())) {
            actionsPossibles.remove(Move.RAISE);
        }

        return actionsPossibles;
    }

    public List<Move> toutesLesActions() {
        List<Move> actions = List.of(Move.FOLD, Move.CALL, Move.RAISE, Move.ALL_IN);
        return new ArrayList<>(actions);
    }


}
