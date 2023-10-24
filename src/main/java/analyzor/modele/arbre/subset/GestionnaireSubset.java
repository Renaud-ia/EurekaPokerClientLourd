package analyzor.modele.arbre.subset;

import analyzor.modele.poker.Board;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * gère l'encryptage et la récupération des subsets + flops GTO + HashMap
 * utilisé par GenerateurSubset pour créer des fichiers de stockage encodés
 * à l'initialisation, regarde les fichiers stockés et répertorie les découpages subsets nécessaires
 * et vérifie que tous les flops GTO sont référencés (doit donc garder une liste)
 * et que tous les subsets sont valides
 * renvoie des HashMap immuables quand on lui demande (cle = GTOrank, valeur = subset IntCode)
 */
public class GestionnaireSubset {
    private static Map<Integer, Map<Integer, Integer>> indexTableSubsets;

    static {
        recupererFlopsGTO();
        recupererToutesLesTables();
    }

    // interface pour obtenir les maps
    public static Map<Integer, Integer> obtenirTable(int nSubsets) {
        return indexTableSubsets.get(nSubsets);
    }

    public static Integer[] subsetsDisponibles() {
        return indexTableSubsets.keySet().toArray(new Integer[0]);
    }

    // récupération des données
    private static void recupererFlopsGTO() {

    }

    private static void recupererToutesLesTables() {

    }

    //méthodes utilisées par GenerateurSubset dans sa méthode main
    protected static void enregistrerFlopsGTO(List<Board> boardsGTO) {

    }

    protected static void enregistrerTableSubsets(HashMap<Integer, Integer> tableSubsets) {

    }
}
