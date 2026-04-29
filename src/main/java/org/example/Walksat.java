package org.example;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Walksat {

    public static boolean walksat(List<int[]> clauses, int maxFlips, int maxTries, double p, int k) {
        // Early check for empty clause
        for (int[] clause : clauses) {
            if (clause.length == 0) {
                System.out.println("Empty clause found! Formula is unsatisfiable.");
                return false;
            }
        }

        Random rnd = ThreadLocalRandom.current();

        // infer variables
        Set<Integer> vars = getVariables(clauses);
        int P = vars.stream().max(Integer::compareTo).orElse(0);
        System.out.println("P: " + P);

        if (P == 0) return true; // empty KB or no variables is vacuously satisfied

        for (int tryNo = 1; tryNo <= maxTries; tryNo++) {

            boolean[] A = new boolean[P + 1];

            // init assignment
            if (tryNo == 1) {
               semiGreedyInit(A, clauses, vars, k, rnd);
            } else {
                randomInit(A, vars, rnd);
            }

            for (int flip = 1; flip <= maxFlips; flip++) {

                if (isFormulaSatisfied(clauses, A)) return true;

                List<int[]> unsat = getUnsatisfiedClauses(clauses, A);
                if (unsat.isEmpty()) return true; // Should be handled by isFormulaSatisfied but safe to have

                int[] c = unsat.get(rnd.nextInt(unsat.size()));

                int v = chooseVarFromClause(c, clauses, A, p, rnd);
                if (v > 0 && v <= P) {
                    A[v] = !A[v];
                }
            }
        }
        return false;
    }
    private static void semiGreedyInit(boolean[] A, List<int[]> clauses, Set<Integer> vars, int k, Random rnd) {

        Boolean[] partial = new Boolean[A.length]; // all null = unassigned

        Set<Integer> unassigned = new HashSet<>(vars);

        class Candidate {
            int var;
            boolean value;
            int score;
            Candidate(int var, boolean value, int score) {
                this.var = var;
                this.value = value;
                this.score = score;
            }
        }

        while (!unassigned.isEmpty()) {

            List<Candidate> candidates = new ArrayList<>();

            for (int v : unassigned) {
                // test v = true
                partial[v] = true;
                int scoreTrue = scoreSatisfiedPartial(clauses, partial);
                partial[v] = null; // restore

                // test v = false
                partial[v] = false;
                int scoreFalse = scoreSatisfiedPartial(clauses, partial);
                partial[v] = null; // restore

                // add both options as candidates
                candidates.add(new Candidate(v, true, scoreTrue));
                candidates.add(new Candidate(v, false, scoreFalse));
            }
            // sort candidates by score descending
            candidates.sort((a, b) -> Integer.compare(b.score, a.score));

            // keep top k candidates
            int rclSize = Math.min(k, candidates.size());
            if (rclSize <= 0) break;
            Candidate chosen = candidates.get(rnd.nextInt(rclSize));

            // commit chosen literal into partial assignment
            partial[chosen.var] = chosen.value;
            unassigned.remove(chosen.var);
        }

        // finalize A from partial
        for (int v : vars) {
             if (v < partial.length && partial[v] != null) {
                 A[v] = partial[v];
             }
        }
    }

    private static int chooseVarFromClause(int[] clause, List<int[]> clauses, boolean[] A, double p, Random rnd) {
        if (clause.length == 0) return -1;

        int baseScore = scoreSatisfied(clauses, A);

        // store candidate vars that improve score
        List<Integer> improvingVars = new ArrayList<>();

        int bestVar = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int lit : clause) {
            int v = Math.abs(lit);
            if (v >= A.length) continue;

            // flip temporarily
            A[v] = !A[v];
            int newScore = scoreSatisfied(clauses, A);
            A[v] = !A[v]; // flip back

            int delta = newScore - baseScore;

            if (delta > 0) {
                improvingVars.add(v);
            }

            if (newScore > bestScore) {
                bestScore = newScore;
                bestVar = v;
            }
        }

        // RULE 1: if there are improving flips, choose randomly among them
        if (!improvingVars.isEmpty()) {
            return improvingVars.get(rnd.nextInt(improvingVars.size()));
        }

        // RULE 2: otherwise with probability p choose random var from clause or best var
        if (rnd.nextDouble() < p) {
            int lit = clause[rnd.nextInt(clause.length)];
            return Math.abs(lit);
        } else {
            return bestVar;
        }
    }

    // HELPERS
    private static void randomInit(boolean[] A, Set<Integer> vars, Random rnd) {
        for (int v : vars) {
            if (v < A.length) {
                A[v] = rnd.nextBoolean();
            }
        }
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

    // scoring function for partial assignments
    private static int scoreSatisfiedPartial(List<int[]> clauses, Boolean[] A) {
        int count = 0;
        for (int[] clause : clauses) {
            if (isClauseSatisfiedPartial(clause, A)) count++;
        }
        return count;
    }
    // check if clause is satisfied under partial assignment
    private static boolean isClauseSatisfiedPartial(int[] clause, Boolean[] A) {
        for (int lit : clause) {
            int v = Math.abs(lit);
            if (v >= A.length) continue;
            Boolean val = A[v];
            if (val == null) continue; // unknown, can't satisfy yet

            boolean litTrue = (lit > 0) ? val : !val;
            if (litTrue) return true;
        }
        return false;
    }

    private static boolean isLiteralTrue(int lit, boolean[] A) {
        int v = Math.abs(lit);
        if (v >= A.length) return false;
        boolean val = A[v];
        return lit > 0 ? val : !val;
    }

    private static boolean isClauseSatisfied(int[] clause, boolean[] A) {
        for (int lit : clause) {
            if (isLiteralTrue(lit, A)) return true;
        }
        return false;
    }

    private static boolean isFormulaSatisfied(List<int[]> clauses, boolean[] A) {
        for (int[] clause : clauses) {
            if (!isClauseSatisfied(clause, A)) return false;
        }
        return true;
    }

    private static List<int[]> getUnsatisfiedClauses(List<int[]> clauses, boolean[] A) {
        List<int[]> unsat = new ArrayList<>();
        for (int[] clause : clauses) {
            if (!isClauseSatisfied(clause, A)) unsat.add(clause);
        }
        return unsat;
    }

    private static int scoreSatisfied(List<int[]> clauses, boolean[] A) {
        int count = 0;
        for (int[] clause : clauses) {
            if (isClauseSatisfied(clause, A)) count++;
        }
        return count;
    }
}
