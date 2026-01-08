package org.example;

import java.util.*;

public class Walksat {

    public static Boolean walksat(List<int[]> clauses, int maxFlips, int maxRetries, float p, int k) {

        Set<Integer> variables = getVariables(clauses);

        // Print all variable IDs found
        System.out.println("Variables found: " + variables);

        // Print all clauses correctly
        System.out.println("Clauses:");
        for (int i = 0; i < clauses.size(); i++) {
            System.out.println(i + ": " + Arrays.toString(clauses.get(i)));
        }

        return false; // still just test
    }

    private static Set<Integer> getVariables(List<int[]> clauses) {
        Set<Integer> variables = new HashSet<>();

        for (int[] clause : clauses) {
            for (int lit : clause) {
                variables.add(Math.abs(lit));
            }
        }
        return variables;
    }
}
