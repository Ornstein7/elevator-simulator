package fr.esipe.elevatorsim.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.esipe.elevatorsim.model.ElevatorRequest;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class JsonReportWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private JsonReportWriter() {}

    public static void write(String filePath,
                             SimulationStats stats,
                             List<ElevatorRequest> allRequests) {
        SimulationReport report = new SimulationReport();
        report.stats = stats;

        report.requests = allRequests.stream().map(r -> {
            SimulationReport.RequestReport rr = new SimulationReport.RequestReport();
            rr.residentId = r.getResidentId();
            rr.originFloor = r.getOriginFloor();
            rr.destinationFloor = r.getDestinationFloor();
            rr.requestTime = r.getRequestTime();
            rr.pickupTime = r.getPickupTime();
            rr.dropoffTime = r.getDropoffTime();
            rr.assignedElevatorId = (r.getAssignedElevator() != null)
                    ? r.getAssignedElevator().getId()
                    : null;
            return rr;
        }).toList();

        try {
            File out = new File(filePath);
            out.getParentFile().mkdirs();
            MAPPER.writeValue(out, report);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report to " + filePath, e);
        }
    }
}
