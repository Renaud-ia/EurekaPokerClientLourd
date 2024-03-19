package analyzor.modele.utils;

import java.util.ArrayList;
import java.util.List;

public class Combinations<T> {

    private final List<T> inputList;

    public Combinations(List<T> inputList) {
        if (inputList == null) {
            throw new IllegalArgumentException("Input list cannot be null.");
        }
        this.inputList = inputList;
    }

    public Combinations(T[] inputList) {
        if (inputList == null) {
            throw new IllegalArgumentException("Input list cannot be null.");
        }
        this.inputList = List.of(inputList);
    }

    public List<List<T>> getCombinations(int combinationLength) {
        List<List<T>> result = new ArrayList<>();
        if (combinationLength == 0 || combinationLength > inputList.size()) {
            return result;
        }

        combine(result, new ArrayList<>(), 0, combinationLength);
        return result;
    }

    private void combine(List<List<T>> result, List<T> temp, int start, int combinationLength) {
        if (combinationLength == 0) {
            result.add(new ArrayList<>(temp));
            return;
        }
        for (int i = start; i <= inputList.size() - combinationLength; i++) {
            temp.add(inputList.get(i));
            combine(result, temp, i + 1, combinationLength - 1);
            temp.remove(temp.size() - 1);
        }
    }

    public static void main(String[] args) {
        List<Integer> myList = List.of(1, 2, 3, 4);
        Combinations<Integer> combinations = new Combinations<>(myList);
        List<List<Integer>> allCombinations = combinations.getCombinations(3);
        System.out.println(allCombinations);
    }
}
