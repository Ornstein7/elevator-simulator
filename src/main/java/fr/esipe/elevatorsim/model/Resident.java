package fr.esipe.elevatorsim.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un résident avec un étage de domicile et une routine quotidienne.
 */
public class Resident {

    private final int id;
    private final int homeFloor;

    // Position courante pendant la simulation
    private int currentFloor;
    private boolean inBuilding = true;

    // Plans de déplacement pour la journée
    private final List<ResidentTripPlan> tripPlans = new ArrayList<>();

    // Stats escaliers (phase 2)
    private int totalStairsFloors = 0;        // nombre total d'étages parcourus à pied
    private int totalStairsTimeSeconds = 0;   // temps total passé dans les escaliers

    public Resident(int id, int homeFloor) {
        this.id = id;
        this.homeFloor = homeFloor;
        this.currentFloor = homeFloor;
    }

    public int getId() {
        return id;
    }

    public int getHomeFloor() {
        return homeFloor;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public boolean isInBuilding() {
        return inBuilding;
    }

    public void setInBuilding(boolean inBuilding) {
        this.inBuilding = inBuilding;
    }

    public List<ResidentTripPlan> getTripPlans() {
        return tripPlans;
    }

    public void addTripPlan(ResidentTripPlan plan) {
        tripPlans.add(plan);
    }

    // --- Stats escaliers ---

    public void addStairsUsage(int floors, int durationSeconds) {
        this.totalStairsFloors += floors;
        this.totalStairsTimeSeconds += durationSeconds;
    }

    public int getTotalStairsFloors() {
        return totalStairsFloors;
    }

    public int getTotalStairsTimeSeconds() {
        return totalStairsTimeSeconds;
    }
}
