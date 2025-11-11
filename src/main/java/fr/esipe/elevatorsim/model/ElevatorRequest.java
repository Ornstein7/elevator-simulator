package fr.esipe.elevatorsim.model;

/**
 * Représente une demande de transport par ascenseur
 * depuis un étage d'origine vers un étage de destination.
 */
public class ElevatorRequest {

    private final int residentId;
    private final int requestTime;      // seconde où la personne appelle l'ascenseur
    private final int originFloor;
    private final int destinationFloor;

    private Elevator assignedElevator;  // ascenseur choisi
    private Integer pickupTime;         // heure de prise en charge
    private Integer dropoffTime;        // heure de dépôt

    public ElevatorRequest(int residentId,
                           int requestTime,
                           int originFloor,
                           int destinationFloor) {
        if (requestTime < 0) throw new IllegalArgumentException("requestTime must be >= 0");
        if (originFloor < 0 || destinationFloor < 0) throw new IllegalArgumentException("Floors must be >= 0");
        if (originFloor == destinationFloor) throw new IllegalArgumentException("Origin and destination floors must differ");

        this.residentId = residentId;
        this.requestTime = requestTime;
        this.originFloor = originFloor;
        this.destinationFloor = destinationFloor;
    }

    public int getResidentId() {
        return residentId;
    }

    public int getRequestTime() {
        return requestTime;
    }

    public int getOriginFloor() {
        return originFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    // Affectation à un ascenseur

    public boolean isAssigned() {
        return assignedElevator != null;
    }

    public Elevator getAssignedElevator() {
        return assignedElevator;
    }

    public void assignTo(Elevator elevator) {
        if (elevator == null) {
            throw new IllegalArgumentException("elevator cannot be null");
        }
        this.assignedElevator = elevator;
    }

    // Pickup / dropoff

    public Integer getPickupTime() {
        return pickupTime;
    }

    public Integer getDropoffTime() {
        return dropoffTime;
    }

    public boolean isPickedUp() {
        return pickupTime != null;
    }

    public boolean isCompleted() {
        return dropoffTime != null;
    }

    public void markPickedUp(int time) {
        if (pickupTime == null) {
            pickupTime = time;
        }
    }

    public void markDroppedOff(int time) {
        if (dropoffTime == null) {
            dropoffTime = time;
        }
    }
}
