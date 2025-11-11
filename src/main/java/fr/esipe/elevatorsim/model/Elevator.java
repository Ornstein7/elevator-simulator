package fr.esipe.elevatorsim.model;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Représente une cabine d'ascenseur avec un modèle physique simplifié.
 * Unité de position : étage (0 = rez-de-chaussée).
 */
public class Elevator {

    public enum Direction {
        UP, DOWN, IDLE
    }

    private final int id;
    private final int capacity;

    private final double maxSpeedFloorsPerSecond;
    private final double accelerationFloorsPerSecond2;
    private final int doorOpenTimeSeconds;

    // État dynamique
    private double position; // en étages
    private double velocity; // en étages/s
    private Direction direction = Direction.IDLE;

    private boolean doorOpen = false;
    private int remainingDoorTime = 0;

    private final Queue<Integer> stops = new ArrayDeque<>();

    // Occupation & énergie
    private int passengersOnboard = 0;
    private double energyConsumed = 0.0;

    // Modèle d'énergie simplifié en montée
    private static final double BASE_ENERGY_PER_FLOOR_UP = 1.0;
    private static final double ENERGY_PER_PASSENGER_PER_FLOOR_UP = 0.1;

    public Elevator(int id,
                    int capacity,
                    double maxSpeedFloorsPerSecond,
                    double accelerationFloorsPerSecond2,
                    int doorOpenTimeSeconds) {

        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        if (maxSpeedFloorsPerSecond <= 0) throw new IllegalArgumentException("maxSpeed must be > 0");
        if (accelerationFloorsPerSecond2 <= 0) throw new IllegalArgumentException("acceleration must be > 0");
        if (doorOpenTimeSeconds < 0) throw new IllegalArgumentException("doorOpenTimeSeconds must be >= 0");

        this.id = id;
        this.capacity = capacity;
        this.maxSpeedFloorsPerSecond = maxSpeedFloorsPerSecond;
        this.accelerationFloorsPerSecond2 = accelerationFloorsPerSecond2;
        this.doorOpenTimeSeconds = doorOpenTimeSeconds;

        this.position = 0.0;
        this.velocity = 0.0;
    }

    public int getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getPosition() {
        return position;
    }

    public int getCurrentFloorRounded() {
        return (int) Math.round(position);
    }

    public boolean isDoorOpen() {
        return doorOpen;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean hasStops() {
        return !stops.isEmpty();
    }

    public Integer peekCurrentTarget() {
        return stops.peek();
    }

    public int getPassengersOnboard() {
        return passengersOnboard;
    }

    public double getEnergyConsumed() {
        return energyConsumed;
    }

    public void passengerEnters() {
        passengersOnboard++;
    }

    public void passengerLeaves() {
        if (passengersOnboard > 0) {
            passengersOnboard--;
        }
    }

    /**
     * Ajoute un arrêt à la file de destinations.
     */
    public void addStop(int floor) {
        if (floor < 0) {
            throw new IllegalArgumentException("floor must be >= 0");
        }
        stops.add(floor);
    }

    /**
     * Fait évoluer l'ascenseur d'un tick de simulation.
     */
    public void step(int tickSeconds) {
        if (tickSeconds <= 0) {
            throw new IllegalArgumentException("tickSeconds must be > 0");
        }

        // Si portes ouvertes : on décrémente le temps d'ouverture
        if (doorOpen) {
            if (remainingDoorTime > 0) {
                remainingDoorTime -= tickSeconds;
            }
            if (remainingDoorTime <= 0) {
                doorOpen = false;
                remainingDoorTime = 0;
            }
            if (doorOpen) {
                // Portes encore ouvertes ce tick, pas de mouvement
                return;
            }
        }

        Integer targetFloor = stops.peek();
        if (targetFloor == null) {
            velocity = 0.0;
            direction = Direction.IDLE;
            return;
        }

        double target = targetFloor;

        // Déjà exactement à l'étage cible ?
        if (Math.abs(target - position) < 1e-6) {
            arriveAtFloor();
            return;
        }

        // Sens désiré
        Direction desiredDirection = (target > position) ? Direction.UP : Direction.DOWN;

        // Vitesse désirée
        double desiredVelocity = (desiredDirection == Direction.UP ? 1 : -1) * maxSpeedFloorsPerSecond;
        double deltaV = accelerationFloorsPerSecond2 * tickSeconds;

        // Accélération / décélération vers la vitesse cible
        if (Math.abs(desiredVelocity - velocity) <= deltaV) {
            velocity = desiredVelocity;
        } else {
            velocity += Math.signum(desiredVelocity - velocity) * deltaV;
        }

        // Direction réelle
        if (velocity > 0) {
            direction = Direction.UP;
        } else if (velocity < 0) {
            direction = Direction.DOWN;
        } else {
            direction = Direction.IDLE;
        }

        double oldPosition = position;
        double newPosition = position + velocity * tickSeconds;

        // Si on dépasse la cible, on se cale dessus
        if ((desiredDirection == Direction.UP && newPosition >= target)
                || (desiredDirection == Direction.DOWN && newPosition <= target)) {
            position = target;
            velocity = 0.0;
            arriveAtFloor();
        } else {
            position = newPosition;
        }

        // Énergie consommée uniquement en montée
        double movedUp = Math.max(0.0, position - oldPosition);
        if (movedUp > 0) {
            energyConsumed += movedUp * (BASE_ENERGY_PER_FLOOR_UP
                    + ENERGY_PER_PASSENGER_PER_FLOOR_UP * passengersOnboard);
        }
    }

    private void arriveAtFloor() {
        // On enlève l'arrêt atteint
        stops.poll();

        // Ouverture des portes
        if (doorOpenTimeSeconds > 0) {
            doorOpen = true;
            remainingDoorTime = doorOpenTimeSeconds;
        }

        if (stops.isEmpty()) {
            direction = Direction.IDLE;
        }
    }
}
