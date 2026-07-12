package ro.nectariepopa.crediblecrowd.citizens;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

record CitizensSettings(double percentage, int globalLimit, int perWorldLimit,
                        int perPlayerLimit, double activationDistance,
                        double despawnDistance, long reconcileTicks,
                        long behaviorTicks, int spawnBatchSize, double anchorSpreadRadius, int anchorPlacementAttempts, List<Location> anchors,
                        List<String> enabledWorlds, boolean customJoinItemsEnabled,
                        double lobbyAfkPercentage, String afkSpawnItemKey, List<String> wanderingItemKeys,
                        List<NpcRegion> npcRegions, boolean allowFallbackNearPlayers) {
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
        double anchorSpread = Math.max(0, config.getDouble("materialization.anchor-spread-radius", 4));
        int anchorAttempts = positive(config.getInt("materialization.anchor-placement-attempts", 12));
        List<String> enabledWorlds = config.getStringList("lobby-scope.worlds").stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();
        boolean cji = config.getBoolean("custom-join-items.enabled", true);
        double afkPercentage = clamp(config.getDouble("custom-join-items.lobby-afk.percentage", .65), 0, 1);
        String afkItem = config.getString("custom-join-items.lobby-afk.item-key", "lobby-compass");
        List<String> wanderingItems = List.copyOf(config.getStringList("custom-join-items.wandering-item-keys"));
        List<NpcRegion> regions = new ArrayList<>();
        for (String raw : config.getStringList("lobby-scope.npc-regions")) {
            String[] p = raw.split(","); if (p.length != 7) continue;
            try { regions.add(new NpcRegion(p[0].trim(), Double.parseDouble(p[1].trim()), Double.parseDouble(p[2].trim()), Double.parseDouble(p[3].trim()), Double.parseDouble(p[4].trim()), Double.parseDouble(p[5].trim()), Double.parseDouble(p[6].trim()))); } catch (NumberFormatException ignored) {}
        }
        boolean fallback = config.getBoolean("materialization.allow-fallback-near-players", false);
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
                despawn, ticks, behaviorTicks, batch, anchorSpread, anchorAttempts, List.copyOf(anchors), List.copyOf(enabledWorlds), cji, afkPercentage, afkItem, wanderingItems, List.copyOf(regions), fallback);
    }

    boolean appliesTo(World world) { return enabledWorlds.isEmpty() || enabledWorlds.contains(world.getName().toLowerCase(Locale.ROOT)); }
    boolean inNpcRegion(Location location) { return npcRegions.isEmpty() || npcRegions.stream().anyMatch(region -> region.contains(location)); }
    boolean allowsNpc(Location location) { return appliesTo(location.getWorld()) && inNpcRegion(location); }
    boolean isLobbyAfk(java.util.UUID id) { return Math.floorMod(id.hashCode(), 10_000) < (int) Math.round(lobbyAfkPercentage * 10_000); }

    private static int positive(int value) { return Math.max(1, value); }
    private static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
}
