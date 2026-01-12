package org.example;

import java.io.*;
import java.util.*;

public class KnowledgeBase {
    public static List<int[]> generateKB(int P, int C, int Lmin, int Lmax) {
        Random rnd = new Random();
        List<int[]> clauses = new ArrayList<>();// list of clauses for KB
        Set<String> seen = new HashSet<>(); // for duplicate clause detection

        while (clauses.size() < C) {

            int len = Lmin + rnd.nextInt(Lmax - Lmin + 1);

            Set<Integer> clauseSet = new HashSet<>(); // to build clause without duplicate variables (A and -A)

            while (clauseSet.size() < len) {

                int var = 1 + rnd.nextInt(P);      // pick variable 1..P
                boolean neg = rnd.nextBoolean();   // 50% chance for negative

                int lit = neg ? -var : var;

                // Reject if both x and -x appear in the clause
                if (clauseSet.contains(-lit)) continue;

                clauseSet.add(lit); // add literal to set so no duplicates
            }

            // normalize clause for duplicate check:
            // sort literals so (1 -3 5) == (5 1 -3)
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
            br.readLine(); // skip header since we dont use it

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
                    boolean neg = p.startsWith("-");
                    char c = neg ? p.charAt(1) : p.charAt(0);
                    int var = charToVar(c);
                    clause[i] = neg ? -var : var;
                }
                clauses.add(clause);
            }
        }
        return clauses;
    }

    // HELPERS
    private static String litToLetter(int lit) {
        int v = Math.abs(lit);
        char c = (char) ('A' + v - 1);
        return (lit < 0 ? "-" : "") + c;
    }

    static int charToVar(char c) {
        return c - 'A' + 1;
    }
}
