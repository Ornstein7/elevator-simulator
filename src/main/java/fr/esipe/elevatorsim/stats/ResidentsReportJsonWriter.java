package fr.esipe.elevatorsim.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.esipe.elevatorsim.model.Building;
import fr.esipe.elevatorsim.model.ElevatorRequest;
import fr.esipe.elevatorsim.model.Floor;
import fr.esipe.elevatorsim.model.Resident;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class ResidentsReportJsonWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private ResidentsReportJsonWriter() {
    }

    public static void write(String filePath,
                             Building building,
                             List<ElevatorRequest> allRequests) {
        // Index résidents par id
        Map<Integer, Resident> residentsById = new HashMap<>();
        for (Floor floor : building.getFloors()) {
            for (Resident r : floor.getResidents()) {
                residentsById.put(r.getId(), r);
            }
        }

        // Regrouper requêtes par résident
        Map<Integer, List<ElevatorRequest>> byResident = new HashMap<>();
        for (ElevatorRequest r : allRequests) {
            byResident.computeIfAbsent(r.getResidentId(), k -> new ArrayList<>()).add(r);
        }

        // Construire structure export
        List<ResidentReport> reports = new ArrayList<>();
        for (Map.Entry<Integer, List<ElevatorRequest>> entry : byResident.entrySet()) {
            int residentId = entry.getKey();
            Resident resident = residentsById.get(residentId);

            ResidentReport rr = new ResidentReport();
            rr.residentId = residentId;
            rr.homeFloor = (resident != null) ? resident.getHomeFloor() : null;

            rr.trips = new ArrayList<>();
            for (ElevatorRequest req : entry.getValue()) {
                Trip t = new Trip();
                t.originFloor = req.getOriginFloor();
                t.destinationFloor = req.getDestinationFloor();
                t.requestTime = req.getRequestTime();
                t.pickupTime = req.getPickupTime();
                t.dropoffTime = req.getDropoffTime();
                t.elevatorId = (req.getAssignedElevator() != null)
                        ? req.getAssignedElevator().getId()
                        : null;
                rr.trips.add(t);
            }

            reports.add(rr);
        }

        // Tri par id pour une sortie stable
        reports.sort(Comparator.comparingInt(r -> r.residentId));

        try {
            File out = new File(filePath);
            out.getParentFile().mkdirs();
            MAPPER.writeValue(out, reports);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write residents report to " + filePath, e);
        }
    }

    public static class ResidentReport {
        public int residentId;
        public Integer homeFloor;
        public List<Trip> trips;
    }

    public static class Trip {
        public int originFloor;
        public int destinationFloor;
        public int requestTime;
        public Integer pickupTime;
        public Integer dropoffTime;
        public Integer elevatorId;
    }
}
