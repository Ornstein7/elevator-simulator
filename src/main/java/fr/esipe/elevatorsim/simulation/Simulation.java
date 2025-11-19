package fr.esipe.elevatorsim.simulation;

import fr.esipe.elevatorsim.stats.SimulationStats;
import fr.esipe.elevatorsim.model.Building;
import fr.esipe.elevatorsim.model.Elevator;
import fr.esipe.elevatorsim.model.ElevatorRequest;
import fr.esipe.elevatorsim.model.Resident;
import fr.esipe.elevatorsim.model.ResidentTripPlan;
import fr.esipe.elevatorsim.strategy.ElevatorStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;


/**
 * Moteur principal de simulation.
 * Phase 4 : les déplacements des résidents créent des requêtes d'ascenseur,
 * traitées par une stratégie de contrôle.
 */
public class Simulation {

    private SimulationStats stats;
    private final Map<Integer, List<ElevatorStopEvent>> elevatorStopEvents = new java.util.HashMap<>();

    private long occupancySum = 0;          // somme des passagers sur tous les ticks et ascenseurs
    private long capacitySum = 0;           // somme des capacités correspondantes (pour un taux moyen)

    private final Random random = new Random(42); // graine fixe pour reproductibilité
    private final Building building;
    private final SimulationClock clock;
    private final ElevatorStrategy elevatorStrategy;

    private final List<ElevatorRequest> allRequests = new ArrayList<>();
    private final List<ElevatorRequest> pendingRequests = new ArrayList<>();

    public Simulation(Building building, SimulationClock clock, ElevatorStrategy elevatorStrategy) {
        this.building = building;
        this.clock = clock;
        this.elevatorStrategy = elevatorStrategy;
        generateRequestsFromResidents();
    }

    public SimulationStats getStats() {
        if (stats == null) {
            stats = computeStats();
        }
        return stats;
    }

    public Map<Integer, List<ElevatorStopEvent>> getElevatorStopEvents() {
        return elevatorStopEvents;
    }

    private SimulationStats computeStats() {
        SimulationStats s = new SimulationStats();

        s.totalRequests = allRequests.size();
        s.completedRequests = (int) allRequests.stream().filter(ElevatorRequest::isCompleted).count();

        var waits = allRequests.stream()
                .filter(ElevatorRequest::isPickedUp)
                .map(r -> r.getPickupTime() - r.getRequestTime())
                .sorted()
                .toList();

        var travels = allRequests.stream()
                .filter(ElevatorRequest::isCompleted)
                .map(r -> r.getDropoffTime() - r.getPickupTime())
                .sorted()
                .toList();

        s.averageWait = waits.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        s.medianWait = median(waits);
        s.maxWait = waits.stream().mapToInt(Integer::intValue).max().orElse(0);

        s.averageTravel = travels.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        s.medianTravel = median(travels);
        s.maxTravel = travels.stream().mapToInt(Integer::intValue).max().orElse(0);

        // Moyenne d'attente par étage d'origine
        Map<Integer, java.util.List<Integer>> waitByOrigin = new java.util.HashMap<>();
        for (ElevatorRequest r : allRequests) {
            if (!r.isPickedUp()) continue;
            int w = r.getPickupTime() - r.getRequestTime();
            waitByOrigin.computeIfAbsent(r.getOriginFloor(), k -> new java.util.ArrayList<>()).add(w);
        }
        Map<Integer, Double> avgWaitByOrigin = new java.util.TreeMap<>();
        for (var e : waitByOrigin.entrySet()) {
            double avg = e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0);
            avgWaitByOrigin.put(e.getKey(), avg);
        }
        s.averageWaitByOriginFloor = avgWaitByOrigin;

        // Énergie
        double totalEnergy = 0.0;
        for (var elevator : building.getElevators()) {
            totalEnergy += elevator.getEnergyConsumed();
        }
        s.totalEnergy = totalEnergy;
        s.energyPerRequest = (s.totalRequests == 0) ? 0.0 : totalEnergy / s.totalRequests;

        // Taux d'occupation moyen (déjà accumulé pendant run)
        s.averageFillRate = (capacitySum == 0)
                ? 0.0
                : (double) occupancySum / (double) capacitySum;

        return s;
    }

    public void run() {
        int tickSeconds = clock.getTickSeconds();
        int nextRequestIndex = 0;

        while (!clock.isFinished()) {
            int currentTime = clock.getCurrentTimeSeconds();

            // Activer les nouvelles requêtes
            while (nextRequestIndex < allRequests.size()
                    && allRequests.get(nextRequestIndex).getRequestTime() <= currentTime) {
                pendingRequests.add(allRequests.get(nextRequestIndex));
                nextRequestIndex++;
            }

            // Stratégie pour chaque ascenseur
            for (Elevator elevator : building.getElevators()) {
                elevatorStrategy.step(building, elevator, pendingRequests, currentTime, tickSeconds);
            }

            // Mouvement + gestion des pickups/dropoffs
            for (Elevator elevator : building.getElevators()) {
                boolean previousDoorOpen = elevator.isDoorOpen();
                elevator.step(tickSeconds);
                handleStopsAndRequests(elevator, currentTime + tickSeconds, previousDoorOpen);
            }

            // Stat occupation : on ne considère que les ticks où au moins un passager est à bord
            for (Elevator elevator : building.getElevators()) {
                int onboard = countOnboardPassengers(elevator);
                if (onboard > 0) {
                    occupancySum += onboard;
                    capacitySum += elevator.getCapacity();
                }
            }
            clock.tick();
        }

        printSummary();
    }


    /**
     * Transforme les ResidentTripPlan en requêtes d'ascenseur.
     * Hypothèse : chaque plan est un trajet depuis l'étage courant vers targetFloor.
     */
    private void generateRequestsFromResidents() {
        for (Resident resident : building.getAllResidents()) {
            int currentFloor = resident.getCurrentFloor();

            for (ResidentTripPlan plan : resident.getTripPlans()) {
                int origin = currentFloor;
                int dest = plan.getTargetFloor();
                int earliest = plan.getEarliestDepartureTime();
                int latest = plan.getLatestDepartureTime();
                int time = earliest;

                if (latest > earliest) {
                    int delta = latest - earliest;
                    time = earliest + random.nextInt(delta + 1); // uniforme dans [earliest, latest]
                }

                if (origin != dest) {
                    allRequests.add(new ElevatorRequest(resident.getId(),time, origin, dest));
                }

                currentFloor = dest;
            }
        }

        allRequests.sort(Comparator.comparingInt(ElevatorRequest::getRequestTime));
    }

    /**
     * Quand un ascenseur ouvre ses portes à un étage,
     * on gère les pickups/dropoffs des requêtes qui lui sont associées.
     */
    private void handleStopsAndRequests(Elevator elevator,
                                        int eventTime,
                                        boolean previousDoorOpen) {

        if (!elevator.isDoorOpen() || previousDoorOpen) {
            return;
        }

        int floor = elevator.getCurrentFloorRounded();

        int entered = 0;
        int left = 0;

        Iterator<ElevatorRequest> it = pendingRequests.iterator();
        while (it.hasNext()) {
            ElevatorRequest request = it.next();

            if (!request.isAssigned() || request.getAssignedElevator() != elevator) {
                continue;
            }

            // Pickup
            if (!request.isPickedUp()
                    && request.getOriginFloor() == floor
                    && eventTime >= request.getRequestTime()) {
                request.markPickedUp(eventTime);
                entered++;
            }

            // Dropoff
            if (request.isPickedUp()
                    && !request.isCompleted()
                    && request.getDestinationFloor() == floor) {
                request.markDroppedOff(eventTime);
                left++;
                it.remove();
            }
        }

        // Si arrêt utile ou non, on log quand même l'info : rapport ascenseur complet
        int onboardAfter = countOnboardPassengers(elevator);
        elevatorStopEvents
                .computeIfAbsent(elevator.getId(), k -> new java.util.ArrayList<>())
                .add(new ElevatorStopEvent(eventTime, floor, entered, left, onboardAfter));
    }



    private void printSummary() {
        SimulationStats s = getStats();

        System.out.println("=== Résumé des requêtes d'ascenseur ===");
        System.out.printf("Nombre total de requêtes        : %d%n", s.totalRequests);
        System.out.printf("Requêtes complétées             : %d%n", s.completedRequests);
        System.out.printf("Temps d'attente moyen (s)       : %.2f%n", s.averageWait);
        System.out.printf("Temps d'attente médian (s)      : %.2f%n", s.medianWait);
        System.out.printf("Temps d'attente max (s)         : %d%n", s.maxWait);
        System.out.printf("Temps de trajet moyen (s)       : %.2f%n", s.averageTravel);
        System.out.printf("Temps de trajet médian (s)      : %.2f%n", s.medianTravel);
        System.out.printf("Temps de trajet max (s)         : %d%n", s.maxTravel);

        System.out.println("--- Temps d'attente moyen par étage d'origine ---");
        s.averageWaitByOriginFloor.forEach((floor, avg) ->
                System.out.printf("Étage %2d : %.2f s%n", floor, avg));

        System.out.printf("Énergie totale consommée        : %.2f%n", s.totalEnergy);
        System.out.printf("Énergie moyenne par requête     : %.4f%n", s.energyPerRequest);
        System.out.printf("Taux d'occupation moyen cabines : %.2f%n", s.averageFillRate);
    }


    private static double median(List<Integer> values) {
        int n = values.size();
        if (n == 0) {
            return 0.0;
        }
        if (n % 2 == 1) {
            return values.get(n / 2);
        }
        return (values.get(n / 2 - 1) + values.get(n / 2)) / 2.0;
    }

    private int countOnboardPassengers(Elevator elevator) {
        int count = 0;
        for (ElevatorRequest r : allRequests) {
            if (r.isAssigned()
                    && r.getAssignedElevator() == elevator
                    && r.isPickedUp()
                    && !r.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    public java.util.List<ElevatorRequest> getAllRequests() {
        return allRequests;
    }

    public static class ElevatorStopEvent {
        public final int time;            // seconde où les portes s’ouvrent
        public final int floor;           // étage
        public final int entered;         // nb personnes montées
        public final int left;            // nb personnes descendues
        public final int onboardAfter;    // nb passagers à bord après l'arrêt

        public ElevatorStopEvent(int time, int floor, int entered, int left, int onboardAfter) {
            this.time = time;
            this.floor = floor;
            this.entered = entered;
            this.left = left;
            this.onboardAfter = onboardAfter;
        }
    }


}
