package fr.esipe.elevatorsim;

import fr.esipe.elevatorsim.stats.ElevatorStopsJsonWriter;
import fr.esipe.elevatorsim.stats.ResidentsReportJsonWriter;
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

public class App {

    public static void main(String[] args) {
        // Defaults
        String configPath = "config/demo-config.json";
        String strategyName = "nearest";
        String reportPath = "target/reports/demo-report.json";

        // Parsing ultra-simple: --config=..., --strategy=..., --report=...
        for (String arg : args) {
            if (arg.startsWith("--config=")) {
                configPath = arg.substring("--config=".length());
            } else if (arg.startsWith("--strategy=")) {
                strategyName = arg.substring("--strategy=".length());
            } else if (arg.startsWith("--report=")) {
                reportPath = arg.substring("--report=".length());
            }
        }

        System.out.println("Configuration: " + configPath);
        System.out.println("Stratégie    : " + strategyName);
        System.out.println("Rapport JSON : " + reportPath);

        // 1) Charger config
        SimulationConfig config = ConfigLoader.load(configPath);

        // 2) Construire modèle
        Building building = ModelFactory.buildFromConfig(config);

        // 3) Clock
        SimulationClock clock = new SimulationClock(
                config.simulation.dayDurationSeconds,
                config.simulation.tickSeconds
        );

        // 4) Stratégie choisie
        ElevatorStrategy strategy = StrategyFactory.fromName(strategyName);

        // 5) Lancer simulation
        Simulation simulation = new Simulation(building, clock, strategy);
        simulation.run();

        // 6) Export JSON
        SimulationStats stats = simulation.getStats();
        JsonReportWriter.write(reportPath, stats, simulation.getAllRequests());

        System.out.println("Rapport JSON écrit dans " + reportPath);

        // Rapport arrêts ascenseurs
        ElevatorStopsJsonWriter.write(
                reportPath.replace(".json", "-elevators.json"),
                simulation.getElevatorStopEvents()
        );

        // Rapport par résident
        ResidentsReportJsonWriter.write(
                reportPath.replace(".json", "-residents.json"),
                building,
                simulation.getAllRequests()
        );

        System.out.println("Rapports détaillés écrits dans " + reportPath.replace(".json", "-elevators.json")
                + " et " + reportPath.replace(".json", "-residents.json"));

    }
}
