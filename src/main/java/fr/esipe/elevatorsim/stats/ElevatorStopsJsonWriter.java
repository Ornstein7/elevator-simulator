package fr.esipe.elevatorsim.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.esipe.elevatorsim.simulation.Simulation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class ElevatorStopsJsonWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private ElevatorStopsJsonWriter() {
    }

    public static void write(String filePath,
                             Map<Integer, List<Simulation.ElevatorStopEvent>> stopsByElevator) {
        try {
            File out = new File(filePath);
            out.getParentFile().mkdirs();
            MAPPER.writeValue(out, stopsByElevator);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write elevator stops report to " + filePath, e);
        }
    }
}
