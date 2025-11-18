package fr.esipe.elevatorsim;

import fr.esipe.elevatorsim.config.ConfigLoader;
import fr.esipe.elevatorsim.config.ModelFactory;
import fr.esipe.elevatorsim.config.SimulationConfig;
import fr.esipe.elevatorsim.model.Building;
import fr.esipe.elevatorsim.simulation.Simulation;
import fr.esipe.elevatorsim.simulation.SimulationClock;
import fr.esipe.elevatorsim.stats.JsonReportWriter;
import fr.esipe.elevatorsim.stats.SimulationStats;
import fr.esipe.elevatorsim.strategy.ElevatorStrategy;
import fr.esipe.elevatorsim.strategy.StrategyFactory;
import fr.esipe.elevatorsim.ui.ConsoleUI;

public class App {

    public static void main(String[] args) {
        // Defaults
        String configPath = "config/demo-config.json";
        String strategyName = "nearest";
        String reportPath = "target/reports/demo-report.json";
        boolean interactive = false;

        // Parse args
        for (String arg : args) {
            if ("--help".equals(arg) || "-h".equals(arg)) {
                printHelp();
                return;
            } else if (arg.startsWith("--config=")) {
                configPath = arg.substring("--config=".length());
            } else if (arg.startsWith("--strategy=")) {
                strategyName = arg.substring("--strategy=".length());
            } else if (arg.startsWith("--report=")) {
                reportPath = arg.substring("--report=".length());
            } else if ("--interactive".equals(arg)) {
                interactive = true;
            }
        }

        // Charger modèle depuis la config
        SimulationConfig config = ConfigLoader.load(configPath);
        Building building = ModelFactory.buildFromConfig(config);
        ElevatorStrategy strategy = StrategyFactory.fromName(strategyName);

        if (interactive) {
            // UI console minimaliste (pas de lib externe)
            new ConsoleUI(building, config, strategy, reportPath).start();
            return;
        }

        // Mode non interactif (batch)
        System.out.println("Configuration: " + configPath);
        System.out.println("Stratégie    : " + strategyName);
        System.out.println("Rapport JSON : " + reportPath);

        SimulationClock clock = new SimulationClock(
                config.simulation.dayDurationSeconds,
                config.simulation.tickSeconds
        );

        Simulation simulation = new Simulation(building, clock, strategy);
        simulation.run();

        SimulationStats stats = simulation.getStats();
        JsonReportWriter.write(reportPath, stats, simulation.getAllRequests());

        // Si vous avez les writers détaillés :
        try {
            fr.esipe.elevatorsim.stats.ElevatorStopsJsonWriter.write(
                    reportPath.replace(".json", "-elevators.json"),
                    simulation.getElevatorStopEvents()
            );
            fr.esipe.elevatorsim.stats.ResidentsReportJsonWriter.write(
                    reportPath.replace(".json", "-residents.json"),
                    building,
                    simulation.getAllRequests()
            );
        } catch (Throwable ignored) {
            // MVP : ignorer si absents
        }

        System.out.println("Rapports écrits sous " + reportPath);
    }

    private static void printHelp() {
        System.out.println("""
                Usage:
                  java -jar target/elevator-simulator.jar [options]
                
                Options:
                  --config=PATH         Chemin classpath vers le JSON de configuration (def: config/demo-config.json)
                  --strategy=NAME       fcfs | nearest (def: nearest)
                  --report=PATH         Chemin du rapport JSON (def: target/reports/demo-report.json)
                  --interactive         Lance l'interface console interactive
                  --help, -h            Affiche cette aide
                
                Exemples:
                  java -jar ... --interactive
                  java -jar ... --strategy=fcfs --report=target/reports/fcfs.json
                """);
    }
}
