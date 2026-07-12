package ro.nectariepopa.crediblecrowd.citizens;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

record CitizensSettings(double percentage, int globalLimit, int perWorldLimit,
                        int perPlayerLimit, double activationDistance,
                        double despawnDistance, long reconcileTicks,
                        long behaviorTicks, int spawnBatchSize, List<Location> anchors) {
    static CitizensSettings load(FileConfiguration config) {
        double percentage = clamp(config.getDouble("materialization.percentage", .2), 0, 1);
        int global = positive(config.getInt("materialization.global-limit", 40));
        int world = positive(config.getInt("materialization.per-world-limit", 20));
        int player = positive(config.getInt("materialization.per-real-player-limit", 8));
        double activation = Math.max(8, config.getDouble("materialization.activation-distance", 64));
        double despawn = Math.max(activation, config.getDouble("materialization.despawn-distance", 96));
        long ticks = Math.max(20, config.getLong("materialization.reconcile-seconds", 10) * 20);
        long behaviorTicks = Math.max(1, config.getLong("behaviors.scheduler-ticks", 5));
        int batch = positive(config.getInt("materialization.spawn-batch-size", 2));
        List<Location> anchors = new ArrayList<>();
        for (String raw : config.getStringList("spawn-anchors")) {
            String[] parts = raw.split(",");
            if (parts.length < 4) continue;
            World candidate = Bukkit.getWorld(parts[0].trim());
            if (candidate == null) continue;
            try {
                float yaw = parts.length > 4 ? Float.parseFloat(parts[4].trim()) : 0;
                float pitch = parts.length > 5 ? Float.parseFloat(parts[5].trim()) : 0;
                anchors.add(new Location(candidate, Double.parseDouble(parts[1].trim()),
                        Double.parseDouble(parts[2].trim()), Double.parseDouble(parts[3].trim()), yaw, pitch));
            } catch (NumberFormatException ignored) {}
        }
        return new CitizensSettings(percentage, global, world, player, activation,
                despawn, ticks, behaviorTicks, batch, List.copyOf(anchors));
    }

    private static int positive(int value) { return Math.max(1, value); }
    private static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
}
