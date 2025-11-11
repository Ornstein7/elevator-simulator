package fr.esipe.elevatorsim.model;

/**
 * Décrit un déplacement prévu pour un résident :
 * par ex. quitter l'étage 5 vers 8h15-8h40 pour aller au rez-de-chaussée.
 * On ajoutera du probabiliste plus tard.
 */
public class ResidentTripPlan {

    private final int earliestDepartureTime; // en secondes depuis 00:00
    private final int latestDepartureTime;   // en secondes depuis 00:00
    private final int targetFloor;

    public ResidentTripPlan(int earliestDepartureTime, int latestDepartureTime, int targetFloor) {
        if (latestDepartureTime < earliestDepartureTime) {
            throw new IllegalArgumentException("latestDepartureTime must be >= earliestDepartureTime");
        }
        this.earliestDepartureTime = earliestDepartureTime;
        this.latestDepartureTime = latestDepartureTime;
        this.targetFloor = targetFloor;
    }

    public int getEarliestDepartureTime() {
        return earliestDepartureTime;
    }

    public int getLatestDepartureTime() {
        return latestDepartureTime;
    }

    public int getTargetFloor() {
        return targetFloor;
    }
}
