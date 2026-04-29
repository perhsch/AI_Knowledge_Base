package org.example;

import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            // PARAMETERS
            int C = 10;      // Amount of sentences
            int Lmin = 2;   // Minimum length of a sentence
            int Lmax = 5;   // Maximum length of a sentence
            int P = 10;     // Number of variables (max 26)
            String filepath = "file.txt";

            int maxFlips = 10000; // max flips for Walksat
            int maxTries = 100;   // max tries for Walksat
            double p = 0.5;       // probability for Walksat
            int k = 2;            // k for semi-greedy init

            // USER INPUT
            C = readInt(sc, "Enter the number of sentences [C] (default = 10) : ", C);
            Lmin = readInt(sc, "Enter the minimum length of literals [Lmin] (default = 2) : ", Lmin);
            Lmax = readInt(sc, "Enter the maximum length of literals [Lmax] (default = 5) : ", Lmax);
            P = readInt(sc, "Enter the number of variables 1-26 [P] (default 10) : ", P);

            // CREATION OF KB
            List<int[]> clauses = KnowledgeBase.generateKB(P, C, Lmin, Lmax);
            KnowledgeBase.saveKB(filepath, P, C, Lmin, Lmax, clauses);
            System.out.println("Knowledge base saved to " + filepath);

            // MAIN CODE - QUERY EXECUTION
            System.out.print("Enter a literal (+-) (A-Z): ");
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) {
                String queryStr = input;
                int literal = parseLiteral(queryStr); // convert string to int representation
                List<int[]> KB = KnowledgeBase.loadKB(filepath); // load existing KB from file
                KB.add(new int[]{-literal}); // add negative query to KB

                // get parameters for Walksat
                maxFlips = readInt(sc, "Enter max flips (default 10000): ", maxFlips);
                maxTries = readInt(sc, "Enter max tries (default 100): ", maxTries);
                p = readDouble(sc, "Enter probability p (default 0.5): ", p);
                k = readInt(sc, "Enter k for semi-greedy init (default 2): ", k);

                // run Walksat
                if (Walksat.walksat(KB, maxFlips, maxTries, p, k)) {
                    System.out.println("The literal " + queryStr + " is NOT entailed by the knowledge base.");
                } else {
                    System.out.println("Walksat could not find a satisfying assignment; proceeding with resolution...");
                    Resolution.Result res = Resolution.run(KB);
                    if (res.contradiction) {
                        System.out.println("The literal " + queryStr + " IS entailed by the knowledge base (empty clause found).");
                        // Add all derived resolvents into KB
                        KB.remove(KB.size() - 1);// remove negative query used for contradiction
                        KB.addAll(res.derived);// add all derived clauses
                        if (!hasUnitClause(KB, literal)) { // add unit clause if not already present
                            KB.add(new int[]{literal});
                        }
                        int newC = KB.size();
                        KnowledgeBase.saveKB(filepath, P, newC, Lmin, Lmax, KB);
                        System.out.println("Resolution steps appended and KB rewritten with new C = " + newC);
                    } else {
                        System.out.println("The literal " + queryStr + " is NOT entailed by the knowledge base (no empty clause).");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    // HELPERS
    private static int readInt(Scanner sc, String prompt, int defaultValue) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        if (input.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, using default: " + defaultValue);
            return defaultValue;
        }
    }

    private static double readDouble(Scanner sc, String prompt, double defaultValue) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        if (input.isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, using default: " + defaultValue);
            return defaultValue;
        }
    }

    private static int parseLiteral(String input) {
        input = input.trim();
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Empty input");
        }

        boolean neg = input.startsWith("-");
        int charIdx = neg ? 1 : 0;

        if (charIdx >= input.length()) {
            throw new IllegalArgumentException("Invalid literal format: " + input);
        }

        char c = input.charAt(charIdx);
        c = Character.toUpperCase(c);
        if (c < 'A' || c > 'Z') {
            throw new IllegalArgumentException("Expected A–Z or -A–-Z, got: " + c);
        }

        int var = (c - 'A') + 1;
        return neg ? -var : var;
    }

    private static boolean hasUnitClause(List<int[]> clauses, int lit) {
        for (int[] c : clauses) {
            if (c.length == 1 && c[0] == lit) return true;
        }
        return false;
    }
}
