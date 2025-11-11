package fr.esipe.elevatorsim.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Charge un SimulationConfig depuis un fichier JSON dans le classpath.
 */
public final class ConfigLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ConfigLoader() {
    }

    public static SimulationConfig load(String resourcePath) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream in = cl.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Config resource not found: " + resourcePath);
            }
            return MAPPER.readValue(in, SimulationConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config from " + resourcePath, e);
        }
    }
}
