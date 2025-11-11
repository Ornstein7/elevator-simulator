package fr.esipe.elevatorsim.strategy;

import fr.esipe.elevatorsim.model.Building;
import fr.esipe.elevatorsim.model.Elevator;
import fr.esipe.elevatorsim.model.ElevatorRequest;

import java.util.Comparator;
import java.util.List;

/**
 * Stratégie "nearest request" :
 * Quand l'ascenseur est libre, il choisit parmi les requêtes non assignées
 * celle dont l'étage d'origine est le plus proche de sa position actuelle.
 * En cas d'égalité, on départage par requête la plus ancienne.
 */
public class NearestRequestStrategy implements ElevatorStrategy {

    @Override
    public void step(Building building,
                     Elevator elevator,
                     List<ElevatorRequest> pendingRequests,
                     int currentTimeSeconds,
                     int tickSeconds) {

        // Si l'ascenseur a déjà des arrêts prévus, on ne change rien
        if (elevator.hasStops()) {
            return;
        }

        int currentFloor = elevator.getCurrentFloorRounded();

        ElevatorRequest best = pendingRequests.stream()
                .filter(r -> !r.isAssigned() && !r.isCompleted())
                .min(Comparator
                        .comparingInt((ElevatorRequest r) ->
                                Math.abs(r.getOriginFloor() - currentFloor))
                        .thenComparingInt(ElevatorRequest::getRequestTime))
                .orElse(null);

        if (best == null) {
            return; // aucune requête à traiter
        }

        best.assignTo(elevator);

        // On ajoute d'abord l'étage d'origine si besoin,
        // puis l'étage de destination.
        if (currentFloor != best.getOriginFloor()) {
            elevator.addStop(best.getOriginFloor());
        }
        elevator.addStop(best.getDestinationFloor());
    }
}
