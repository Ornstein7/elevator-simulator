package fr.esipe.elevatorsim.stats;

import java.util.Map;

public class SimulationStats {

    public int totalRequests;
    public int completedRequests;

    public double averageWait;
    public double medianWait;
    public int maxWait;

    public double averageTravel;
    public double medianTravel;
    public int maxTravel;

    public Map<Integer, Double> averageWaitByOriginFloor;

    public double totalEnergy;
    public double energyPerRequest;

    public double averageFillRate; // 0.0 -> 1.0
}
