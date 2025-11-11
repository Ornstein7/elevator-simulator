package fr.esipe.elevatorsim.strategy;

import fr.esipe.elevatorsim.model.Building;
import fr.esipe.elevatorsim.model.Elevator;
import fr.esipe.elevatorsim.model.ElevatorRequest;

import java.util.Comparator;
import java.util.List;

/**
 * Stratégie simple :
 * - Quand l'ascenseur est libre (aucun stop),
 *   il prend la requête non assignée la plus ancienne,
 *   va à l'étage d'origine, puis à l'étage de destination.
 */
public class FcfsElevatorStrategy implements ElevatorStrategy {

    @Override
    public void step(Building building,
                     Elevator elevator,
                     List<ElevatorRequest> pendingRequests,
                     int currentTimeSeconds,
                     int tickSeconds) {

        // Si l'ascenseur a déjà un plan d'arrêts, on le laisse finir
        if (elevator.hasStops()) {
            return;
        }

        // Cherche la requête non assignée la plus ancienne
        ElevatorRequest oldest = pendingRequests.stream()
                .filter(r -> !r.isAssigned() && !r.isCompleted())
                .min(Comparator.comparingInt(ElevatorRequest::getRequestTime))
                .orElse(null);

        if (oldest == null) {
            return; // aucune requête à gérer
        }

        oldest.assignTo(elevator);

        int currentFloor = elevator.getCurrentFloorRounded();

        // Aller d'abord à l'origine si nécessaire
        if (currentFloor != oldest.getOriginFloor()) {
            elevator.addStop(oldest.getOriginFloor());
        }

        // Puis aller à la destination
        elevator.addStop(oldest.getDestinationFloor());
    }
}
