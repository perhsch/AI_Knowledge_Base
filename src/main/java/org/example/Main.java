package org.example;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        //PARAMETERS
        int C = 0;      //Amount of sentences
        int Lmin = 2;   //Minimum length of a sentence
        int Lmax = 5;  //Maximum length of a sentence
        int P = 10;     //Amount of variables max 26
        String path = "file.txt";

        //USER INPUT
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the number of sentences (C): ");
        String input = sc.nextLine();
        if (!input.isBlank()) {
            C = Integer.parseInt(input);
        }
        System.out.print("Enter the minimum length of literals (Lmin): ");
        input = sc.nextLine();
        if (!input.isBlank()) {
            Lmin = Integer.parseInt(input);
        }
        System.out.print("Enter the maximum length of literals (Lmax): ");
        input = sc.nextLine();
        if (!input.isBlank()) {
            Lmax = Integer.parseInt(input);
        }
        do {
            System.out.print("Enter the number of variables 1-26 (P): ");
            input = sc.nextLine();
            if (!input.isBlank()) {
                P = Integer.parseInt(input);
            }
            if (P < 1 || P > 26) {
                System.out.println("Please enter a valid number of variables between 1 and 26.");
            }
        } while (P < 1 || P > 26);

        //CREATION OF KB
        List<int[]> clauses = KnowledgeBase.generateKB(P, C, Lmin, Lmax);
        KnowledgeBase.saveKB(path,P,C,Lmin,Lmax,clauses);
        System.out.println("Knowledge base saved to " + path);

        //ASK USER FOR LITERAL
        System.out.print("Enter a literal (+-)  (1-26)");
        input = sc.nextLine();
        if (!input.isBlank()) {
            int literal = Integer.parseInt(input);
            List<int[]>KB = KnowledgeBase.loadKB(path);
            KB.add(new int[]{-literal});
            if (false) {
                System.out.println("The literal " + literal + " is NOT entailed by the knowledge base.");
            } else {
                Resolution.Result res = Resolution.run(KB);
                if (res.contradiction) {
                    System.out.println("The literal " + literal + " IS entailed by the knowledge base (empty clause found).");

                    // Add all derived resolvents into KB
                    KB.addAll(res.derived);

                    // Rewrite file with updated C in header
                    int newC = KB.size();
                    KnowledgeBase.saveKB(path, P, newC, Lmin, Lmax, KB);

                    System.out.println("Resolution steps appended and KB rewritten with new C = " + newC);
                } else {
                    System.out.println("The literal " + literal + " is NOT entailed by the knowledge base (no empty clause).");
                }
                KnowledgeBase.checkDuplicates(path);

            }
        }
    }
}