package fr.esipe.elevatorsim.simulation;

/**
 * Gère le temps de la simulation (discret).
 */
public class SimulationClock {

    private final int endTimeSeconds;
    private final int tickSeconds;
    private int currentTimeSeconds = 0;

    public SimulationClock(int endTimeSeconds, int tickSeconds) {
        if (endTimeSeconds <= 0 || tickSeconds <= 0) {
            throw new IllegalArgumentException("endTimeSeconds and tickSeconds must be > 0");
        }
        this.endTimeSeconds = endTimeSeconds;
        this.tickSeconds = tickSeconds;
    }

    public boolean isFinished() {
        return currentTimeSeconds >= endTimeSeconds;
    }

    public void tick() {
        currentTimeSeconds += tickSeconds;
    }

    public int getCurrentTimeSeconds() {
        return currentTimeSeconds;
    }

    public int getTickSeconds() {
        return tickSeconds;
    }
}
