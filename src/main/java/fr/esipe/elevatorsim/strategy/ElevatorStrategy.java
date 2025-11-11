package fr.esipe.elevatorsim.strategy;

import fr.esipe.elevatorsim.model.Building;
import fr.esipe.elevatorsim.model.Elevator;
import fr.esipe.elevatorsim.model.ElevatorRequest;

import java.util.List;

/**
 * Stratégie de contrôle d'un ascenseur.
 * A chaque tick, elle peut décider d'ajouter des arrêts en fonction des requêtes.
 */
public interface ElevatorStrategy {

    void step(Building building,
              Elevator elevator,
              List<ElevatorRequest> pendingRequests,
              int currentTimeSeconds,
              int tickSeconds);
}
