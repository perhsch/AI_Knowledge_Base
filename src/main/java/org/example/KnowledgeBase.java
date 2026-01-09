package org.example;

import java.io.*;
import java.util.*;

public class KnowledgeBase {
    public static List<int[]> generateKB(int P, int C, int Lmin, int Lmax) {
        Random rnd = new Random();

        List<int[]> clauses = new ArrayList<>();
        Set<String> seen = new HashSet<>(); // for duplicate clause detection

        while (clauses.size() < C) {

            int len = Lmin + rnd.nextInt(Lmax - Lmin + 1);

            Set<Integer> clauseSet = new HashSet<>();

            while (clauseSet.size() < len) {

                int var = 1 + rnd.nextInt(P);      // pick variable 1..P
                boolean neg = rnd.nextBoolean();   // 50% chance negation

                int lit = neg ? -var : var;

                // Reject if both x and ¬x appear in the clause
                if (clauseSet.contains(-lit)) continue;

                clauseSet.add(lit); // add literal to set so no duplicates
            }

            // normalize clause for duplicate check:
            // sort literals so (1 -3 5) == (5 1 -3)
            int[] clause = clauseSet.stream().mapToInt(i -> i).toArray();
            Arrays.sort(clause);

            String key = Arrays.toString(clause);
            if (seen.contains(key)) continue; // duplicate clause → reject

            seen.add(key);
            clauses.add(clause);
        }

        return clauses;
    }
    public static void checkDuplicates(String filename) throws IOException {
        List<int[]> clauses = loadKB(filename);

        Map<String, Integer> count = new HashMap<>();

        for (int[] clause : clauses) {
            int[] copy = Arrays.copyOf(clause, clause.length);
            Arrays.sort(copy);
            String key = Arrays.toString(copy);
            count.put(key, count.getOrDefault(key, 0) + 1);
        }

        int duplicates = 0;
        for (Map.Entry<String, Integer> e : count.entrySet()) {
            if (e.getValue() > 1) {
                duplicates += (e.getValue() - 1);
                System.out.println("DUPLICATE x" + e.getValue() + " : " + e.getKey());
            }
        }

        System.out.println("Total clauses: " + clauses.size());
        System.out.println("Duplicate instances: " + duplicates);
        System.out.println("Unique clauses: " + count.size());
    }
    public static void saveKB(String filename, int P, int C, int Lmin, int Lmax, List<int[]> clauses)
            throws IOException {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {

            // Header: P C Lmin Lmax
            bw.write(P + " " + C + " " + Lmin + " " + Lmax);
            bw.newLine();

            // Each clause on one line
            for (int[] clause : clauses) {
                if (clause.length == 0) {
                    bw.write("0");
                    bw.newLine();
                    continue;
                }
                for (int i = 0; i < clause.length; i++) {
                    bw.write(Integer.toString(clause[i]));
                    if (i < clause.length - 1) bw.write(" ");
                }
                bw.newLine();
            }
        }
    }
    public static List<int[]> loadKB(String filename) throws IOException {
        List<int[]> clauses = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine(); // header: P C Lmin Lmax (optional to parse)
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // skip empty lines
                String[] parts = line.split("\\s+");
                int[] clause = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    clause[i] = Integer.parseInt(parts[i]);
                }
                clauses.add(clause);
            }
        }
        return clauses;
    }
}
