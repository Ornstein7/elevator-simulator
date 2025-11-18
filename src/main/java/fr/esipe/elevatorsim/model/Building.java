package fr.esipe.elevatorsim.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Représente la tour : étages, paramètres physiques, résidents.
 */
public class Building {

    private final int floorsCount;       // Sans compter le rez-de-chaussée ? Ici : inclut l'étage 0 à floorsCount.
    private final double floorHeight;    // En mètres
    private final List<Floor> floors;
    private final List<Elevator> elevators = new ArrayList<>();

    public Building(int floorsCount, double floorHeight) {
        if (floorsCount < 1) {
            throw new IllegalArgumentException("Building must have at least 1 floor.");
        }
        this.floorsCount = floorsCount;
        this.floorHeight = floorHeight;
        this.floors = new ArrayList<>(floorsCount + 1);

        // Création des étages 0..floorsCount
        for (int i = 0; i <= floorsCount; i++) {
            floors.add(new Floor(i));
        }
    }

    public int getFloorsCount() {
        return floorsCount;
    }

    public double getFloorHeight() {
        return floorHeight;
    }

    public Floor getFloor(int index) {
        if (index < 0 || index > floorsCount) {
            throw new IllegalArgumentException("Invalid floor index: " + index);
        }
        return floors.get(index);
    }

    public List<Floor> getFloors() {
        return Collections.unmodifiableList(floors);
    }

    public List<Resident> getAllResidents() {
        List<Resident> result = new ArrayList<>();
        for (Floor floor : floors) {
            result.addAll(floor.getResidents());
        }
        return result;
    }

    public void addElevator(Elevator elevator) {
        if (elevator == null) {
            throw new IllegalArgumentException("elevator cannot be null");
        }
        elevators.add(elevator);
    }

    public List<Elevator> getElevators() {
        return Collections.unmodifiableList(elevators);
    }

    public boolean removeElevatorById(int id) {
        return elevators.removeIf(e -> e.getId() == id);
    }

    public Elevator findElevatorById(int id) {
        for (Elevator e : elevators) {
            if (e.getId() == id) return e;
        }
        return null;
    }

}


