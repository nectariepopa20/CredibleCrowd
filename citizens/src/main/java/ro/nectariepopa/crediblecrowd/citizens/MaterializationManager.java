package ro.nectariepopa.crediblecrowd.citizens;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ro.nectariepopa.crediblecrowd.paper.CredibleCrowdPaper;
import ro.nectariepopa.crediblecrowd.paper.VirtualPlayer;
import ro.nectariepopa.crediblecrowd.paper.VirtualPlayersUpdatedEvent;
import ro.nectariepopa.crediblecrowd.citizens.behavior.BehaviorController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

final class MaterializationManager implements Listener {
    private final CredibleCrowdCitizens plugin;
    private final CredibleCrowdPaper bridge;
    private final CitizensSettings settings;
    private CustomJoinItemsBridge customJoinItems;
    private final Map<UUID, NPC> active = new LinkedHashMap<>();
    private final Map<UUID, BehaviorController> behaviors = new HashMap<>();
    private NPCRegistry registry;
    private BukkitTask task;
    private BukkitTask behaviorTask;

    MaterializationManager(CredibleCrowdCitizens plugin, CredibleCrowdPaper bridge,
                           CitizensSettings settings) {
        this.plugin = plugin;
        this.bridge = bridge;
        this.settings = settings;
    }

    void start() {
        registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
        customJoinItems = CustomJoinItemsBridge.detect(plugin, settings);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::reconcile, 1L, settings.reconcileTicks());
        behaviorTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickBehaviors, 1L, settings.behaviorTicks());
    }

    @EventHandler public void onVirtualPlayersUpdated(VirtualPlayersUpdatedEvent event) { reconcile(); }

    private void tickBehaviors() { behaviors.values().forEach(BehaviorController::tick); }

    private void reconcile() {
        List<? extends Player> viewers = Bukkit.getOnlinePlayers().stream().filter(p -> !p.hasMetadata("NPC") && settings.allowsNpc(p.getLocation())).toList();
        List<VirtualPlayer> virtual = bridge.getVirtualPlayerIdentities();
        Set<UUID> virtualIds = virtual.stream().map(VirtualPlayer::id).collect(java.util.stream.Collectors.toSet());
        active.entrySet().removeIf(entry -> {
            NPC npc = entry.getValue();
            if (!virtualIds.contains(entry.getKey()) || shouldDespawn(npc, viewers)) {
                destroy(entry.getKey(), npc);
                return true;
            }
            return false;
        });
        if (viewers.isEmpty()) return;

        int target = Math.min(settings.globalLimit(), (int) Math.ceil(virtual.size() * settings.percentage()));
        target = Math.min(target, viewers.size() * settings.perPlayerLimit());
        int allowance = Math.min(settings.spawnBatchSize(), Math.max(0, target - active.size()));
        if (allowance == 0) return;

        Map<World, Integer> worldCounts = new HashMap<>();
        for (NPC npc : active.values()) if (npc.isSpawned()) worldCounts.merge(npc.getEntity().getWorld(), 1, Integer::sum);
        for (VirtualPlayer identity : virtual) {
            if (allowance == 0) break;
            if (active.containsKey(identity.id())) continue;
            Optional<Location> spawn = chooseSpawn(viewers, worldCounts);
            if (spawn.isEmpty()) break;
            NPC npc = registry.createNPC(EntityType.PLAYER, identity.name());
            npc.data().setPersistent("crediblecrowd.virtual-name", identity.name());
            npc.data().setPersistent("crediblecrowd.virtual-id", identity.id().toString());
            if (!npc.spawn(spawn.get())) {
                npc.destroy();
                continue;
            }
            active.put(identity.id(), npc);
            worldCounts.merge(spawn.get().getWorld(), 1, Integer::sum);
            boolean lobbyAfk = settings.isLobbyAfk(identity.id());
            behaviors.put(identity.id(), CitizensBehaviorRuntime.create(plugin, npc, lobbyAfk, settings));
            if (customJoinItems != null) customJoinItems.apply(npc, identity.id(), lobbyAfk);
            allowance--;
        }
    }

    private Optional<Location> chooseSpawn(List<? extends Player> viewers, Map<World, Integer> counts) {
        double activationSquared = settings.activationDistance() * settings.activationDistance();
        Optional<Location> anchor = settings.anchors().stream()
                .filter(settings::allowsNpc)
                .filter(point -> counts.getOrDefault(point.getWorld(), 0) < settings.perWorldLimit())
                .filter(point -> viewers.stream().anyMatch(player -> player.getWorld().equals(point.getWorld())
                        && player.getLocation().distanceSquared(point) <= activationSquared
                        && nearbyMaterialized(player.getLocation(), activationSquared) < settings.perPlayerLimit()))
                .min(Comparator.comparingLong(point -> nearbyMaterialized(point, 36)))
                .map(this::spreadFromAnchor);
        if (anchor.isPresent()) return anchor;
        if (!settings.allowFallbackNearPlayers()) return Optional.empty();
        return viewers.stream()
                .filter(player -> counts.getOrDefault(player.getWorld(), 0) < settings.perWorldLimit())
                .filter(player -> nearbyMaterialized(player.getLocation(), activationSquared) < settings.perPlayerLimit())
                .min(Comparator.comparingInt(player -> counts.getOrDefault(player.getWorld(), 0)))
                .flatMap(player -> safeNearby(player.getLocation()));
    }

    private long nearbyMaterialized(Location location, double distanceSquared) {
        return active.values().stream().filter(NPC::isSpawned).map(NPC::getEntity)
                .filter(java.util.Objects::nonNull).map(entity -> entity.getLocation())
                .filter(candidate -> candidate.getWorld().equals(location.getWorld())
                        && candidate.distanceSquared(location) <= distanceSquared).count();
    }

    private Location spreadFromAnchor(Location anchor) {
        if (settings.anchorSpreadRadius() <= 0) return anchor.clone();
        for (int attempt = 0; attempt < settings.anchorPlacementAttempts(); attempt++) {
            double angle = Math.random() * Math.PI * 2;
            double distance = Math.sqrt(Math.random()) * settings.anchorSpreadRadius();
            Location candidate = anchor.clone().add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance);
            if (settings.allowsNpc(candidate) && isSafeSpawn(candidate)) return candidate;
        }
        return anchor.clone();
    }

    private boolean isSafeSpawn(Location location) {
        Block feet = location.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block floor = feet.getRelative(0, -1, 0);
        return feet.isPassable() && head.isPassable() && floor.getType().isSolid();
    }

    private Optional<Location> safeNearby(Location origin) {
        double angle = Math.random() * Math.PI * 2;
        double distance = 6 + Math.random() * Math.max(2, settings.activationDistance() / 3);
        int x = origin.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
        int z = origin.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
        int y = origin.getWorld().getHighestBlockYAt(x, z) + 1;
        Location candidate = new Location(origin.getWorld(), x + .5, y, z + .5, (float) (Math.random() * 360), 0);
        return settings.allowsNpc(candidate) ? Optional.of(candidate) : Optional.empty();
    }

    private boolean shouldDespawn(NPC npc, List<? extends Player> viewers) {
        if (!npc.isSpawned() || npc.getEntity() == null || !settings.allowsNpc(npc.getEntity().getLocation())) return true;
        Location location = npc.getEntity().getLocation();
        double distanceSquared = settings.despawnDistance() * settings.despawnDistance();
        return viewers.stream().noneMatch(player -> player.getWorld().equals(location.getWorld())
                && player.getLocation().distanceSquared(location) <= distanceSquared);
    }

    private void destroy(UUID id, NPC npc) {
        BehaviorController controller = behaviors.remove(id);
        if (controller != null) controller.stop();
        npc.destroy();
    }

    void stop() {
        if (task != null) task.cancel();
        if (behaviorTask != null) behaviorTask.cancel();
        HandlerList.unregisterAll(this);
        new ArrayList<>(active.entrySet()).forEach(entry -> destroy(entry.getKey(), entry.getValue()));
        active.clear();
        if (registry != null) registry.deregisterAll();
    }
}
