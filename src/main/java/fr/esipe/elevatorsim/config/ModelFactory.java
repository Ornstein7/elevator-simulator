package fr.esipe.elevatorsim.config;

import fr.esipe.elevatorsim.model.Building;
import fr.esipe.elevatorsim.model.Elevator;
import fr.esipe.elevatorsim.model.Floor;
import fr.esipe.elevatorsim.model.Resident;
import fr.esipe.elevatorsim.model.ResidentTripPlan;

import java.util.Random;

/**
 * Construit le Building, les résidents et les ascenseurs à partir d'un SimulationConfig.
 */
public final class ModelFactory {

    private ModelFactory() {
    }

    public static Building buildFromConfig(SimulationConfig config) {
        // 1) Bâtiment
        Building building = new Building(
                config.building.floorsCount,
                config.building.floorHeight
        );

        // 2) Résidents + habitudes
        SimulationConfig.ResidentsConfig rc = config.residents;
        Random random = new Random(config.simulation.randomSeed);

        int residentId = 1;
        for (Floor floor : building.getFloors()) {
            int index = floor.getIndex();
            if (index == 0) {
                continue; // pas de résidents au RDC
            }

            for (int i = 0; i < rc.defaultPerFloor; i++) {
                Resident r = new Resident(residentId++, index);

                // Matin
                if (rc.morning != null) {
                    int dep = randomInWindow(random, rc.morning.earliest, rc.morning.latest);
                    int target = (rc.morning.targetFloor != null)
                            ? rc.morning.targetFloor
                            : 0;
                    r.addTripPlan(new ResidentTripPlan(dep, dep, target));
                }

                // Soir
                if (rc.evening != null) {
                    int dep = randomInWindow(random, rc.evening.earliest, rc.evening.latest);
                    // retour vers l'étage de domicile
                    r.addTripPlan(new ResidentTripPlan(dep, dep, index));
                }

                floor.addResident(r);
            }
        }

        // 3) Ascenseurs
        for (SimulationConfig.ElevatorConfig ec : config.elevators) {
            Elevator e = new Elevator(
                    ec.id,
                    ec.capacity,
                    ec.maxSpeedFloorsPerSecond,
                    ec.accelerationFloorsPerSecond2,
                    ec.doorOpenTimeSeconds
            );
            building.addElevator(e);
        }

        return building;
    }

    private static int randomInWindow(Random random, int earliest, int latest) {
        if (latest <= earliest) {
            return earliest;
        }
        int delta = latest - earliest;
        return earliest + random.nextInt(delta + 1);
    }
}
