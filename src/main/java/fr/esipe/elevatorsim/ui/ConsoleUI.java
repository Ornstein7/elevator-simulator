package fr.esipe.elevatorsim.ui;

import fr.esipe.elevatorsim.config.SimulationConfig;
import fr.esipe.elevatorsim.model.Building;
import fr.esipe.elevatorsim.model.Elevator;
import fr.esipe.elevatorsim.simulation.Simulation;
import fr.esipe.elevatorsim.simulation.SimulationClock;
import fr.esipe.elevatorsim.stats.JsonReportWriter;
import fr.esipe.elevatorsim.stats.SimulationStats;
import fr.esipe.elevatorsim.strategy.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

public class ConsoleUI {
    private enum Menu { MAIN, ELEV, STRAT }

    private final SimulationConfig cfg;
    private final ConsoleIO io;
    private final Building building;
    private ElevatorStrategy strategy;
    private String reportPath;
    private final Deque<String> history = new ArrayDeque<>();  // dernieres actions
    private String lastStatus = null;

    private enum LastActionType { NONE, ADD_ELEVATOR, REMOVE_ELEVATOR, EDIT_ELEVATOR }
    private static class LastAction {
        LastActionType type = LastActionType.NONE;
        Elevator before; // état précédent (pour REMOVE / EDIT)
        Elevator after;  // état actuel (pour ADD / EDIT)
    }
    private final LastAction lastAction = new LastAction();

    public ConsoleUI(Building building, SimulationConfig cfg,
                     ElevatorStrategy strategy, String reportPath) {
        this.building = building;
        this.cfg = cfg;
        this.strategy = strategy;
        this.reportPath = reportPath;
        this.io = new ConsoleIO(new Scanner(System.in));
    }

    public void start() {
        Menu state = Menu.MAIN;
        while (true) {
            io.clear();
            printHeader();
            switch (state) {
                case MAIN -> state = mainMenu();
                case ELEV -> state = elevMenu();
                case STRAT -> state = stratMenu();
            }
            if (state == null) {
                System.out.println("Bye.");
                return;
            }
        }
    }


    private Menu mainMenu() {
        System.out.println("Menu principal");
        System.out.println("1) Résumé");
        System.out.println("2) Ascenseurs ->");
        System.out.println("3) Stratégie -> " + strategyName());
        System.out.println("4) Changer chemin rapport");
        System.out.println("5) Lancer simulation");
        System.out.println("0) Quitter");
        int c = io.readInt("> ", 0, 5);
        switch (c) {
            case 0 -> { return null; }
            case 1 -> { showSummary(); io.pause(); return Menu.MAIN; }
            case 2 -> { return Menu.ELEV; }
            case 3 -> { return Menu.STRAT; }
            case 4 -> { changeReportPath(); return Menu.MAIN; }
            case 5 -> { runOnce(); io.pause(); return Menu.MAIN; }
            default -> { return Menu.MAIN; }
        }
    }


    private Menu elevMenu() {
        System.out.println("=== Ascenseurs ===   (9 = Retour)");
        System.out.println("1) Lister");
        System.out.println("2) Ajouter");
        System.out.println("3) Modifier (par id)");
        System.out.println("4) Supprimer (par id)");
        System.out.println("5) Annuler dernière modification");
        System.out.println("9) Retour");
        int c = io.readInt("> ", 1, 9);
        switch (c) {
            case 1 -> { listElevators(); io.pause(); return Menu.ELEV; }
            case 2 -> { addElevator(); return Menu.ELEV; }
            case 3 -> { editElevator(); return Menu.ELEV; }
            case 4 -> { removeElevator(); return Menu.ELEV; }
            case 5 -> { undoLastElevatorChange(); return Menu.ELEV; }
            case 9 -> { return Menu.MAIN; }
            default -> { return Menu.ELEV; }
        }
    }



    private Menu stratMenu() {
        System.out.println("=== Stratégie ===   (9 = Retour)");
        System.out.println("Courante : " + strategyName());
        System.out.println("1) fcfs");
        System.out.println("2) nearest");
        System.out.println("9) Retour");
        int c = io.readInt("> ", 1, 9);
        switch (c) {
            case 1 -> { strategy = new FcfsElevatorStrategy(); System.out.println("OK: fcfs"); io.pause(); return Menu.STRAT; }
            case 2 -> { strategy = new NearestRequestStrategy(); System.out.println("OK: nearest"); io.pause(); return Menu.STRAT; }
            case 9 -> { return Menu.MAIN; }
            default -> { return Menu.STRAT; }
        }
    }

    // === Actions ===

    private void showSummary() {
        System.out.printf("Bâtiment : %d étages (0..%d)%n", building.getFloorsCount(), building.getFloorsCount());
        System.out.printf("Résidents: %d%n", building.getAllResidents().size());
        listElevators();
    }

    private void listElevators() {
        if (building.getElevators().isEmpty()) {
            System.out.println("📭 Aucun ascenseur configuré");
            return;
        }

        System.out.println("┌─────┬──────────┬────────┬──────────┬─────────┐");
        System.out.println("│ ID  │ Capacité │ Vmax   │ Accel    │ Portes  │");
        System.out.println("├─────┼──────────┼────────┼──────────┼─────────┤");

        for (Elevator e : building.getElevators()) {
            System.out.printf("│ %-3d │ %-8d │ %5.2f  │ %7.2f  │ %4ds   │%n",
                    e.getId(), e.getCapacity(),
                    e.getMaxSpeedFloorsPerSecond(),
                    e.getAccelerationFloorsPerSecond2(),
                    e.getDoorOpenTimeSeconds());
        }
        System.out.println("└─────┴──────────┴────────┴──────────┴─────────┘");
    }

    private void addElevator() {
        pushHistory("[2] Ajouter un ascenseur");
        io.clear();
        printHeader();

        int id = io.readInt("Id (>=0) : ", 0, Integer.MAX_VALUE);
        if (building.findElevatorById(id) != null) { System.out.println("Id déjà existant."); return; }

        int cap = io.readInt("Capacité (>=1) : ", 1, Integer.MAX_VALUE);
        double vmax = io.readPosDouble("Vitesse max (étages/s) : ");
        double acc  = io.readPosDouble("Accélération (étages/s^2) : ");
        int door    = io.readInt("Temps portes ouvertes (s) : ", 0, Integer.MAX_VALUE);

        Elevator e = new Elevator(id, cap, vmax, acc, door);
        building.addElevator(e);
        lastAction.type = LastActionType.ADD_ELEVATOR;
        lastAction.before = null;
        lastAction.after = e;

        setStatus("Ascenseur ajouté (id=" + id + ")");
        io.pause();
    }

    private void editElevator() {
        pushHistory("[3] Modifier un ascenseur");
        io.clear();
        printHeader();

        int id = io.readInt("Id à modifier : ", 0, Integer.MAX_VALUE);
        Elevator e = building.findElevatorById(id);
        if (e == null) {
            System.out.println("Id introuvable.");
            setStatus("Modification échouée (id introuvable " + id + ")");
            io.pause();
            return;
        }

        System.out.println("(Entrée = conserver la valeur actuelle)");
        Integer cap = io.readIntOptional("Capacité [" + e.getCapacity() + "]", 1, null);
        Double vmax = io.readPosDoubleOptional("Vitesse max [" + safe(() -> e.getMaxSpeedFloorsPerSecond()) + "]");
        Double acc  = io.readPosDoubleOptional("Accélération [" + safe(() -> e.getAccelerationFloorsPerSecond2()) + "]");
        Integer door= io.readIntOptional("Portes ouvertes (s) [" + safe(() -> e.getDoorOpenTimeSeconds()) + "]", 0, null);

        int newCap = (cap != null) ? cap : e.getCapacity();
        double newV = (vmax != null) ? vmax : safe(e::getMaxSpeedFloorsPerSecond);
        double newA = (acc  != null) ? acc  : safe(e::getAccelerationFloorsPerSecond2);
        int newD    = (door != null) ? door : (int) safe(e::getDoorOpenTimeSeconds);

        Elevator before = e;
        Elevator after  = new Elevator(id, newCap, newV, newA, newD);

        building.removeElevatorById(id);
        building.addElevator(after);

        lastAction.type = LastActionType.EDIT_ELEVATOR;
        lastAction.before = before;
        lastAction.after  = after;

        setStatus("Ascenseur modifié (id=" + id + ")");
        io.pause();
    }


    private void removeElevator() {
        pushHistory("[3] Supprimer un ascenseur");
        io.clear();
        printHeader();

        int id = io.readInt("Id à supprimer : ", 0, Integer.MAX_VALUE);
        Elevator e = building.findElevatorById(id);
        if (e == null) {
            System.out.println("Id introuvable.");
            setStatus("Suppression échouée (id introuvable " + id + ")");
            io.pause();
            return;
        }
        if (!io.confirm("Confirmer la suppression de l'ascenseur " + id + " ?")) {
            setStatus("Suppression annulée par l'utilisateur");
            return;
        }

        building.removeElevatorById(id);
        lastAction.type = LastActionType.REMOVE_ELEVATOR;
        lastAction.before = e;
        lastAction.after = null;

        setStatus("Ascenseur supprimé (id=" + id + ")");
        io.pause();
    }

    private void changeReportPath() {
        String p = io.prompt("Nouveau chemin rapport JSON : ").trim();
        if (!p.isBlank()) { reportPath = p; System.out.println("OK."); }
        else { System.out.println("Chemin inchangé."); }
    }

    private void runOnce() {
        SimulationClock clock = new SimulationClock(cfg.simulation.dayDurationSeconds, cfg.simulation.tickSeconds);
        Simulation sim = new Simulation(building, clock, strategy);
        sim.run(); // affiche déjà le résumé détaillé si ton Simulation imprime

        SimulationStats stats = sim.getStats();
        JsonReportWriter.write(reportPath, stats, sim.getAllRequests());
        try {
            fr.esipe.elevatorsim.stats.ElevatorStopsJsonWriter.write(
                    reportPath.replace(".json", "-elevators.json"), sim.getElevatorStopEvents());
            fr.esipe.elevatorsim.stats.ResidentsReportJsonWriter.write(
                    reportPath.replace(".json", "-residents.json"), building, sim.getAllRequests());
        } catch (Throwable ignore) {}
        System.out.println("Rapports écrits dans " + reportPath + " (+ dérivés).");
    }

    private String strategyName() {
        return (strategy instanceof FcfsElevatorStrategy) ? "fcfs" : "nearest";
    }

    private static double safe(SupplierD s) {
        try { return s.get(); } catch (Throwable t) { return Double.NaN; }
    }
    @FunctionalInterface private interface SupplierD { double get(); }

    private void pushHistory(String label) {
        if (label == null || label.isBlank()) return;
        history.addFirst(label);
        while (history.size() > 5) { // on garde les 5 dernières actions
            history.removeLast();
        }
    }

    private void setStatus(String msg) {
        this.lastStatus = msg;
    }

    private void printHeader() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   Elevator Simulator 1.1               ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("Stratégie : \033[1;36m" + strategyName() + "\033[0m");
        System.out.println("Rapport   : " + reportPath);
        if (lastStatus != null) {
            System.out.println("État      : " + lastStatus);
        }
        if (!history.isEmpty()) {
            System.out.println("Historique récent :");
            for (String h : history) {
                System.out.println("  - " + h);
            }
        }
        System.out.println();
    }

    private void undoLastElevatorChange() {
        switch (lastAction.type) {
            case NONE -> {
                System.out.println("Aucune modification récente à annuler.");
                setStatus("Rien à annuler");
            }
            case ADD_ELEVATOR -> {
                if (lastAction.after != null) {
                    building.removeElevatorById(lastAction.after.getId());
                    setStatus("Annulation ajout ascenseur (id=" + lastAction.after.getId() + ")");
                }
            }
            case REMOVE_ELEVATOR -> {
                if (lastAction.before != null) {
                    building.addElevator(lastAction.before);
                    setStatus("Annulation suppression ascenseur (id=" + lastAction.before.getId() + ")");
                }
            }
            case EDIT_ELEVATOR -> {
                if (lastAction.before != null && lastAction.after != null) {
                    building.removeElevatorById(lastAction.after.getId());
                    building.addElevator(lastAction.before);
                    setStatus("Annulation modification ascenseur (id=" + lastAction.before.getId() + ")");
                }
            }
        }
        lastAction.type = LastActionType.NONE;
        io.pause();
    }



}
