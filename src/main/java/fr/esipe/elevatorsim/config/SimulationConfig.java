package fr.esipe.elevatorsim.config;

import java.util.List;

/**
 * Représente la configuration de la simulation chargée depuis un fichier JSON.
 */
public class SimulationConfig {

    public BuildingConfig building;
    public ResidentsConfig residents;
    public List<ElevatorConfig> elevators;
    public SimulationParameters simulation;

    public static class BuildingConfig {
        public int floorsCount;
        public double floorHeight;
    }

    public static class ResidentsConfig {
        public int defaultPerFloor;

        public TimeWindow morning; // vers targetFloor (ex: 0)
        public TimeWindow evening; // retour vers homeFloor

        public static class TimeWindow {
            public int earliest;
            public int latest;
            public Integer targetFloor; // pour morning : cible; pour evening : null = homeFloor
        }
    }

    public static class ElevatorConfig {
        public int id;
        public int capacity;
        public double maxSpeedFloorsPerSecond;
        public double accelerationFloorsPerSecond2;
        public int doorOpenTimeSeconds;
    }

    public static class SimulationParameters {
        public int dayDurationSeconds;
        public int tickSeconds;
        public long randomSeed;
    }
}
