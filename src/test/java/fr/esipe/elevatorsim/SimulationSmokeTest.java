package fr.esipe.elevatorsim;

import fr.esipe.elevatorsim.model.Building;
import fr.esipe.elevatorsim.simulation.Simulation;
import fr.esipe.elevatorsim.simulation.SimulationClock;
import fr.esipe.elevatorsim.strategy.FcfsElevatorStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationSmokeTest {

    @Test
    void simulationRunsWithoutError() {
        Building building = new Building(5, 3.0);
        SimulationClock clock = new SimulationClock(60, 1);
        Simulation simulation = new Simulation(building, clock, new FcfsElevatorStrategy());
        simulation.run();
        assertTrue(clock.isFinished());
    }
}
