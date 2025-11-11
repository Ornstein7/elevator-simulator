package fr.esipe.elevatorsim.strategy;

public final class StrategyFactory {

    private StrategyFactory() {}

    /**
     * Retourne une stratégie en fonction d'un nom simple.
     * Exemples: "fcfs", "nearest"
     */
    public static ElevatorStrategy fromName(String name) {
        if (name == null) {
            return new NearestRequestStrategy(); // défaut
        }
        String n = name.toLowerCase();
        switch (n) {
            case "fcfs":
                return new FcfsElevatorStrategy();
            case "nearest":
                return new NearestRequestStrategy();
            default:
                System.out.println("[WARN] Stratégie inconnue '" + name + "', utilisation de 'nearest'.");
                return new NearestRequestStrategy();
        }
    }
}
