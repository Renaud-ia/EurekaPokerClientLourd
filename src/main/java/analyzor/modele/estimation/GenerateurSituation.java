package analyzor.modele.estimation;

import analyzor.modele.config.ValeursConfig;
import analyzor.modele.parties.Entree;
import analyzor.modele.parties.TourMain;

import java.util.*;
import java.util.stream.Collectors;

/**
 * crée les Situation (=arbre) selon des critères modifiables
 * on a peu d'intérêt à limiter l'arbre car ClassificateurSituation va ensuite vérifier
 * si le nomnbre d'entrées est significative
 * à voir éventuellement si on veut ajouter des critères restrictifs par la suite
 */
public class GenerateurSituation {
    // private int maxReraise;
    private final int nombreJoueurs;
    private final FormatSolution formatSolution;
    public GenerateurSituation(FormatSolution formatSolution) {
        //todo variable par round, type tournoi?
        this.nombreJoueurs = formatSolution.getNombreJoueurs();
        this.formatSolution = formatSolution;
    }

    public List<List<Entree>> getSituations(TourMain.Round round) {
        // on récupère toutes les entrées, on les trie
        List<Entree> toutesLesEntrees = GestionnaireFormat.getEntrees(formatSolution);
        boolean actionsDeuxiemeRang = (ValeursConfig.SUBSETS & ValeursConfig.SUBSETS_2E_RANK);

        Map<SituationKey, List<Entree>> groupedMap = toutesLesEntrees.stream()
                .filter(e -> e.getSituation().isRound(round)) // on ne garde que les entrees du Round
                // j'ai changé d'avis, à voir si ça convient
                //.filter(e -> actionsDeuxiemeRang || e.getSituation().getRang() != 2)
                .sorted(Comparator.comparing((Entree e) -> e.getSituation().getRang())
                .thenComparing(e -> e.getSituation().getPosition(), Comparator.reverseOrder())
                .thenComparing(e -> e.getSituation().getNJoueursActifs(), Comparator.reverseOrder()))
                .collect(Collectors.groupingBy(e ->
                                new SituationKey(
                                        e.getSituation().getRang(),
                                        e.getSituation().getPosition(),
                                        e.getSituation().getNJoueursActifs()
                                ),
                        LinkedHashMap::new, // pour conserver l'ordre d'insertion
                        Collectors.toList()
                ));

        List<List<Entree>> sortedAndFilteredEntrees = new ArrayList<>(groupedMap.values());

        return sortedAndFilteredEntrees;
    }

    static class SituationKey {
        private int rang;
        private int position;
        private int nJoueursActifs;

        protected SituationKey(int rang, int position, int nJoueursActifs) {
            this.rang = rang;
            this.position = position;
            this.nJoueursActifs = nJoueursActifs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SituationKey that = (SituationKey) o;

            if (rang != that.rang) return false;
            if (position != that.position) return false;
            if (nJoueursActifs != that.nJoueursActifs) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int result = rang;
            result = 31 * result + position;
            result = 31 * result + nJoueursActifs;
            return result;
        }
    }

}
