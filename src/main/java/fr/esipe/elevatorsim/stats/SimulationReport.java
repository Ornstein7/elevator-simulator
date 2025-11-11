package fr.esipe.elevatorsim.stats;

import java.util.List;

/**
 * Conteneur sérialisable pour un rapport complet de simulation.
 */
public class SimulationReport {

    public SimulationStats stats;
    public List<RequestReport> requests;

    public static class RequestReport {
        public int residentId;
        public int originFloor;
        public int destinationFloor;
        public int requestTime;
        public Integer pickupTime;
        public Integer dropoffTime;
        public Integer assignedElevatorId;
    }
}
