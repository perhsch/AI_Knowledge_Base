package org.example;

import java.util.*;

public class Resolution {

    public static class Result {
        public final boolean contradiction;
        public final List<int[]> derived;

        public Result(boolean contradiction, List<int[]> derived) {
            this.contradiction = contradiction;
            this.derived = derived;
        }
    }

    public static Result run(List<int[]> inputClauses) {

        List<Set<Integer>> clauses = new ArrayList<>();
        for (int[] c : inputClauses) clauses.add(toSet(c));

        Set<String> seen = new HashSet<>();
        for (Set<Integer> c : clauses) seen.add(keyOf(c));

        List<int[]> derived = new ArrayList<>();

        boolean added = true;

        while (added) {
            added = false;

            int n = clauses.size();
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {

                    Set<Integer> ci = clauses.get(i);
                    Set<Integer> cj = clauses.get(j);

                    for (int lit : ci) {
                        if (!cj.contains(-lit)) continue;

                        Set<Integer> resolvent = resolve(ci, cj, lit);

                        if (isTautology(resolvent)) continue;

                        if (resolvent.isEmpty()) {
                            derived.add(new int[0]); // empty clause
                            return new Result(true, derived);
                        }

                        String key = keyOf(resolvent);
                        if (seen.contains(key)) continue;

                        seen.add(key);
                        clauses.add(resolvent);
                        derived.add(toSortedArray(resolvent));
                        added = true;
                    }
                }
            }
        }

        return new Result(false, derived);
    }

    private static Set<Integer> toSet(int[] clause) {
        Set<Integer> s = new HashSet<>();
        for (int lit : clause) s.add(lit);
        return s;
    }

    private static String keyOf(Set<Integer> clause) {
        int[] arr = clause.stream().mapToInt(x -> x).sorted().toArray();
        return Arrays.toString(arr);
    }

    private static Set<Integer> resolve(Set<Integer> c1, Set<Integer> c2, int lit) {
        Set<Integer> res = new HashSet<>();
        res.addAll(c1);
        res.addAll(c2);
        res.remove(lit);
        res.remove(-lit);
        return res;
    }

    private static boolean isTautology(Set<Integer> clause) {
        for (int lit : clause) {
            if (clause.contains(-lit)) return true;
        }
        return false;
    }

    private static int[] toSortedArray(Set<Integer> clause) {
        int[] arr = clause.stream().mapToInt(x -> x).toArray();
        Arrays.sort(arr);
        return arr;
    }
}
