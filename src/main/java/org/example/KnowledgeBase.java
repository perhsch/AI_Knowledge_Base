package org.example;

import java.io.*;
import java.util.*;

public class KnowledgeBase {
    public static List<int[]> generateKB(int P, int C, int Lmin, int Lmax) {
        if (Lmin > Lmax) {
            throw new IllegalArgumentException("Lmin (" + Lmin + ") must be <= Lmax (" + Lmax + ")");
        }
        if (P <= 0 || P > 26) {
            throw new IllegalArgumentException("P must be between 1 and 26 (got " + P + ")");
        }

        Random rnd = new Random();
        List<int[]> clauses = new ArrayList<>();// list of clauses for KB
        Set<String> seen = new HashSet<>(); // for duplicate clause detection

        while (clauses.size() < C) {

            int len = Lmin + rnd.nextInt(Lmax - Lmin + 1);

            Set<Integer> clauseSet = new HashSet<>(); // to build clause without duplicate variables (A and -A)

            int attempts = 0;
            while (clauseSet.size() < len && attempts < 100) {
                attempts++;
                int var = 1 + rnd.nextInt(P);      // pick variable 1..P
                boolean neg = rnd.nextBoolean();   // 50% chance for negative

                int lit = neg ? -var : var;

                // Reject if both x and -x appear in the clause
                if (clauseSet.contains(-lit)) continue;

                clauseSet.add(lit); // add literal to set so no duplicates
            }

            if (clauseSet.size() < len) continue; // couldn't generate a valid clause of this length

            // normalize clause for duplicate check:
            int[] clause = clauseSet.stream().mapToInt(i -> i).toArray();
            Arrays.sort(clause);

            String key = Arrays.toString(clause);
            if (seen.contains(key)) continue; // duplicate clause = reject

            seen.add(key);
            clauses.add(clause);
        }

        return clauses;
    }

    public static void saveKB(String filename, int P, int C, int Lmin, int Lmax, List<int[]> clauses)
            throws IOException {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {

            bw.write(P + " " + C + " " + Lmin + " " + Lmax); // header line
            bw.newLine();

            // each clause on one line
            for (int[] clause : clauses) {
                if (clause.length == 0) {
                    bw.write("0");
                    bw.newLine();
                    continue;
                }
                for (int i = 0; i < clause.length; i++) {
                    bw.write(litToLetter(clause[i]));
                    if (i < clause.length - 1) bw.write(" ");
                }
                bw.newLine();
            }
        }
    }
    public static List<int[]> loadKB(String filename) throws IOException {
        List<int[]> clauses = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine();
            if (header == null) return clauses;

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // handle empty clause
                if (line.equals("0")) {
                    clauses.add(new int[0]);
                    continue;
                }

                String[] parts = line.split("\\s+"); // split by whitespace
                int[] clause = new int[parts.length];

                // parse each literal
                for (int i = 0; i < parts.length; i++) {
                    String p = parts[i];
                    if (p.isEmpty()) continue;
                    boolean neg = p.startsWith("-");
                    int charIdx = neg ? 1 : 0;
                    if (charIdx >= p.length()) {
                        throw new IOException("Malformed literal in KB file: " + p);
                    }
                    char c = p.charAt(charIdx);
                    int var = charToVar(c);
                    clause[i] = neg ? -var : var;
                }
                clauses.add(clause);
            }
        }
        return clauses;
    }

    // HELPERS
    public static String litToLetter(int lit) {
        int v = Math.abs(lit);
        if (v < 1 || v > 26) {
             return (lit < 0 ? "-" : "") + "VAR" + v;
        }
        char c = (char) ('A' + v - 1);
        return (lit < 0 ? "-" : "") + c;
    }

    public static int charToVar(char c) {
        char upper = Character.toUpperCase(c);
        if (upper < 'A' || upper > 'Z') {
            throw new IllegalArgumentException("Variable must be A-Z, got: " + c);
        }
        return upper - 'A' + 1;
    }
}
