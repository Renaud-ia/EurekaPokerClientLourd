package analyzor.modele.utils;

import java.util.ArrayList;
import java.util.List;

public class Permutations<T> {

    public List<List<T>> generate(T[] arr, int k) {
        List<List<T>> results = new ArrayList<>();
        if (k > arr.length || k <= 0) {
            return results;
        }
        generate(arr, k, 0, new ArrayList<T>(), results);
        return results;
    }

    private void generate(T[] arr, int k, int start, List<T> current, List<List<T>>  results) {
        if (k == 0) {
            results.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i <= arr.length - k; i++) {
            current.add(arr[i]);
            generate(arr, k - 1, i + 1, current, results);
            current.remove(current.size() - 1);
        }
    }

    public static void main(String[] args) {
        Permutations<Integer> intPermutations = new Permutations<>();
        Integer[] intArray = {1, 2, 3, 4};
        List<List<Integer>> intResults = intPermutations.generate(intArray, 2);
        for (List<Integer> permutation : intResults) {
            System.out.println(permutation);
        }

        Permutations<String> stringPermutations = new Permutations<>();
        String[] stringArray = {"A", "B", "C", "D"};
        List<List<String>> stringResults = stringPermutations.generate(stringArray, 2);
        for (List<String> permutation : stringResults) {
            System.out.println(permutation);
        }
    }
}