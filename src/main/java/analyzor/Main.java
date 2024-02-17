package analyzor;
import java.util.ArrayList;
import java.util.List;
public class Main {
    public static void main(String[] args) {
        int n = 3; // Nombre de variables
        int MAX_VALEUR = 5; // Valeur maximale

        // Créer une liste pour stocker toutes les combinaisons possibles
        List<List<Integer>> combinations = new ArrayList<>();

        // Générer toutes les combinaisons possibles
        generateCombinations(n, MAX_VALEUR, 1, new ArrayList<>(), combinations);

        int nombreTotal = 0;
        // Afficher toutes les combinaisons possibles
        for (List<Integer> combination : combinations) {
            int produitCombinaisons = 1;
            for (Integer valeur : combination) {
                produitCombinaisons *= valeur;
            }
            if (produitCombinaisons <= 10) {
                nombreTotal++;
                System.out.println(combination);
            }

        }

        System.out.println(combinations.size());
        System.out.println(nombreTotal);
    }

    // Fonction récursive pour générer toutes les combinaisons possibles
    private static void generateCombinations(int n, int MAX_VALEUR, int currentVariable, List<Integer> currentCombination, List<List<Integer>> combinations) {
        // Si toutes les variables ont été attribuées une valeur, ajouter la combinaison à la liste
        if (currentVariable > n) {
            combinations.add(new ArrayList<>(currentCombination));
            return;
        }

        // Pour chaque valeur possible de la variable actuelle
        for (int value = 1; value <= MAX_VALEUR; value++) {
            // Attribuer la valeur à la variable actuelle
            currentCombination.add(value);
            // Générer les combinaisons pour les variables suivantes
            generateCombinations(n, MAX_VALEUR, currentVariable + 1, currentCombination, combinations);
            // Retirer la valeur de la variable actuelle pour tester les autres valeurs
            currentCombination.removeLast();
        }
    }
}
