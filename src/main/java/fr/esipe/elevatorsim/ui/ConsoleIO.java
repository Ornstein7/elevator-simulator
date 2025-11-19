package fr.esipe.elevatorsim.ui;

import java.util.Scanner;

public final class ConsoleIO {
    private final Scanner sc;

    public ConsoleIO(Scanner sc) { this.sc = sc; }

    public String prompt(String msg) {
        System.out.print(msg);
        return sc.nextLine();
    }

    public int readInt(String msg, int min, int max) {
        while (true) {
            String s = prompt(msg);
            try {
                int v = Integer.parseInt(s.trim());
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (Exception e) {
                System.out.printf("Veuillez entrer un entier [%d..%d].%n", min, max);
            }
        }
    }

    public Integer readIntOptional(String msg, Integer min, Integer max) {
        while (true) {
            String s = prompt(msg + " (Enter pour conserver) : ");
            if (s.isBlank()) return null;
            try {
                int v = Integer.parseInt(s.trim());
                if (min != null && v < min) throw new NumberFormatException();
                if (max != null && v > max) throw new NumberFormatException();
                return v;
            } catch (Exception e) {
                System.out.print("Valeur invalide. ");
            }
        }
    }

    public double readPosDouble(String msg) {
        while (true) {
            String s = prompt(msg);
            try {
                double v = Double.parseDouble(s.trim());
                if (v <= 0) throw new NumberFormatException();
                return v;
            } catch (Exception e) {
                System.out.print("Veuillez entrer un nombre > 0.\n");
            }
        }
    }

    public Double readPosDoubleOptional(String msg) {
        while (true) {
            String s = prompt(msg + " (Enter pour conserver) : ");
            if (s.isBlank()) return null;
            try {
                double v = Double.parseDouble(s.trim());
                if (v <= 0) throw new NumberFormatException();
                return v;
            } catch (Exception e) {
                System.out.print("Valeur invalide. ");
            }
        }
    }

    public boolean confirm(String msg) {
        String s = prompt(msg + " [o/N] ").trim().toLowerCase();
        return s.equals("o") || s.equals("oui") || s.equals("y") || s.equals("yes");
    }

    public void pause() {
        System.out.print("(Entrée pour continuer) ");
        sc.nextLine();
    }

    public void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
