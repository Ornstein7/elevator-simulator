package fr.esipe.elevatorsim.config;

import fr.esipe.elevatorsim.model.Building;
import fr.esipe.elevatorsim.model.Floor;
import fr.esipe.elevatorsim.model.Resident;
import fr.esipe.elevatorsim.model.ResidentTripPlan;

/**
 * Génère une configuration de test simple :
 * - Quelques résidents par étage
 * - Un aller matin vers le RDC
 * - Un retour soir vers l'étage de domicile
 */
public final class DemoDataFactory {

    private DemoDataFactory() {
        // utilitaire
    }

    public static void populateDemoBuilding(Building building) {
        int residentId = 1;

        for (Floor floor : building.getFloors()) {
            int index = floor.getIndex();
            if (index == 0) {
                continue; // pas de résidents au RDC
            }

            // 5 résidents par étage (exemple)
            for (int i = 0; i < 5; i++) {
                Resident r = new Resident(residentId++, index);

                // Matin : descend au RDC vers 08:00 (28800s)
                r.addTripPlan(new ResidentTripPlan(
                        8 * 3600, 8 * 3600, 0
                ));

                // Soir : remonte à son étage vers 18:00 (64800s)
                r.addTripPlan(new ResidentTripPlan(
                        18 * 3600, 18 * 3600, index
                ));

                floor.addResident(r);
            }
        }
    }
}
