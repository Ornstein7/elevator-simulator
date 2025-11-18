package fr.esipe.elevatorsim.ui;

import fr.esipe.elevatorsim.config.SimulationConfig;
import fr.esipe.elevatorsim.model.Building;
import fr.esipe.elevatorsim.model.Elevator;
import fr.esipe.elevatorsim.simulation.Simulation;
import fr.esipe.elevatorsim.simulation.SimulationClock;
import fr.esipe.elevatorsim.stats.JsonReportWriter;
import fr.esipe.elevatorsim.stats.SimulationStats;
import fr.esipe.elevatorsim.strategy.ElevatorStrategy;
import fr.esipe.elevatorsim.strategy.FcfsElevatorStrategy;
import fr.esipe.elevatorsim.strategy.NearestRequestStrategy;

import java.util.Scanner;

public class ConsoleUI {

    private final Building building;
    private final SimulationConfig config;
    private ElevatorStrategy strategy;
    private String reportPath;

    public ConsoleUI(Building building,
                     SimulationConfig config,
                     ElevatorStrategy initialStrategy,
                     String initialReportPath) {
        this.building = building;
        this.config = config;
        this.strategy = initialStrategy;
        this.reportPath = initialReportPath;
    }


    public void start() {
        try (Scanner sc = new Scanner(System.in)) {
            clear();
            printHeader();

            while (true) {
                printMenu();
                System.out.print("> ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1" -> showSummary();
                    case "2" -> addElevator(sc);
                    case "3" -> removeElevator(sc);
                    case "4" -> replaceElevator(sc);
                    case "5" -> changeStrategy(sc);
                    case "6" -> changeReportPath(sc);
                    case "7" -> runSimulationOnce();
                    case "m", "menu", "?" -> printMenu();
                    case "0", "q", "quit", "exit" -> { System.out.println("Bye."); return; }

                    default -> System.out.println("Choix invalide.");
                }
            }
        }
    }

    private void printHeader() {
        System.out.println("=== Elevator Simulator 1.0 ===");
        System.out.println("Stratégie : " + strategyName());
        System.out.println("Rapport   : " + reportPath);
        System.out.println();
    }

    private void printMenu() {
        System.out.println("(1) Afficher le résumé bâtiment/ascenseurs");
        System.out.println("(2) Ajouter un ascenseur");
        System.out.println("(3) Supprimer un ascenseur");
        System.out.println("(4) Remplacer/Modifier un ascenseur (supprimer puis ajouter)");
        System.out.println("(5) Changer de stratégie (fcfs / nearest)");
        System.out.println("(6) Changer le chemin du rapport JSON");
        System.out.println("(7) Lancer une simulation maintenant");
        System.out.println("(m) réafficher le menu — (q) quitter");
    }

    private void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void showSummary() {
        int floors = building.getFloorsCount();
        int residents = building.getAllResidents().size();
        System.out.println("Bâtiment : " + floors + " étages (0.." + floors + ")");
        System.out.println("Résidents : " + residents);
        System.out.println("Ascenseurs :");
        for (Elevator e : building.getElevators()) {
            System.out.printf(" - id=%d, cap=%d, vmax=%.2f, acc=%.2f, door=%ds%n",
                    e.getId(), e.getCapacity(),
                    e.getMaxSpeedFloorsPerSecond(),
                    e.getAccelerationFloorsPerSecond2(),
                    e.getDoorOpenTimeSeconds());
        }
        if (building.getElevators().isEmpty()) {
            System.out.println(" (aucun ascenseur installé)");
        }
    }

    private void addElevator(Scanner sc) {
        try {
            System.out.print("Nouvel id : ");
            int id = Integer.parseInt(sc.nextLine().trim());
            if (building.findElevatorById(id) != null) {
                System.out.println("Un ascenseur avec cet id existe déjà.");
                return;
            }
            System.out.print("Capacité : ");
            int cap = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Vitesse max (étages/s) : ");
            double vmax = Double.parseDouble(sc.nextLine().trim());
            System.out.print("Accélération (étages/s^2) : ");
            double acc = Double.parseDouble(sc.nextLine().trim());
            System.out.print("Temps portes ouvertes (s) : ");
            int door = Integer.parseInt(sc.nextLine().trim());

            Elevator e = new Elevator(id, cap, vmax, acc, door);
            building.addElevator(e);
            System.out.println("Ascenseur ajouté.");
        } catch (Exception ex) {
            System.out.println("Entrées invalides. Annulé.");
        }
    }

    private void removeElevator(Scanner sc) {
        System.out.print("Id à supprimer : ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            boolean ok = building.removeElevatorById(id);
            System.out.println(ok ? "Supprimé." : "Id introuvable.");
        } catch (Exception ex) {
            System.out.println("Entrée invalide.");
        }
    }

    private void replaceElevator(Scanner sc) {
        System.out.print("Id de l'ascenseur à remplacer : ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            if (!building.removeElevatorById(id)) {
                System.out.println("Id introuvable.");
                return;
            }
            System.out.println("Paramètres du nouvel ascenseur (même id ou différent) :");
            addElevator(sc);
        } catch (Exception ex) {
            System.out.println("Entrée invalide.");
        }
    }

    private void changeStrategy(Scanner sc) {
        System.out.print("Stratégie (fcfs | nearest) : ");
        String s = sc.nextLine().trim().toLowerCase();
        switch (s) {
            case "fcfs" -> strategy = new FcfsElevatorStrategy();
            case "nearest" -> strategy = new NearestRequestStrategy();
            default -> { System.out.println("Inconnue, inchangé."); return; }
        }
        System.out.println("Stratégie = " + strategyName());
    }

    private void changeReportPath(Scanner sc) {
        System.out.print("Nouveau chemin rapport (ex: target/reports/run.json) : ");
        String p = sc.nextLine().trim();
        if (!p.isEmpty()) {
            reportPath = p;
            System.out.println("OK.");
        }
    }

    private void runSimulationOnce() {
        int day = config.simulation.dayDurationSeconds;
        int tick = config.simulation.tickSeconds;

        SimulationClock clock = new SimulationClock(day, tick);
        Simulation simulation = new Simulation(building, clock, strategy);
        simulation.run();

        SimulationStats stats = simulation.getStats();
        JsonReportWriter.write(reportPath, stats, simulation.getAllRequests());

        // dérivés
        String elev = reportPath.replace(".json", "-elevators.json");
        String resi = reportPath.replace(".json", "-residents.json");
        // si vous avez déjà ces writers dans le projet :
        try {
            fr.esipe.elevatorsim.stats.ElevatorStopsJsonWriter.write(
                    elev, simulation.getElevatorStopEvents()
            );
            fr.esipe.elevatorsim.stats.ResidentsReportJsonWriter.write(
                    resi, building, simulation.getAllRequests()
            );
        } catch (Throwable ignored) {
            // Si non présents, ignorez silencieusement (MVP)
        }

        System.out.println("Simulation terminée. Rapport : " + reportPath);
    }

    private String strategyName() {
        return (strategy instanceof FcfsElevatorStrategy) ? "fcfs" : "nearest";
    }
}
