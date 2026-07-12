package ro.nectariepopa.crediblecrowd.citizens;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import java.util.ArrayList;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.configuration.ConfigurationSection;
import ro.nectariepopa.crediblecrowd.citizens.behavior.AfkBehavior;
import ro.nectariepopa.crediblecrowd.citizens.behavior.BehaviorActor;
import ro.nectariepopa.crediblecrowd.citizens.behavior.BehaviorContext;
import ro.nectariepopa.crediblecrowd.citizens.behavior.BehaviorController;
import ro.nectariepopa.crediblecrowd.citizens.behavior.LookAroundBehavior;
import ro.nectariepopa.crediblecrowd.citizens.behavior.Position;
import ro.nectariepopa.crediblecrowd.citizens.behavior.PatrolBehavior;
import ro.nectariepopa.crediblecrowd.citizens.behavior.ParkourBehavior;
import ro.nectariepopa.crediblecrowd.citizens.behavior.ParkourCheckpoint;
import ro.nectariepopa.crediblecrowd.citizens.behavior.WanderBehavior;
import ro.nectariepopa.crediblecrowd.citizens.behavior.WeightedBehaviorPool;

final class CitizensBehaviorRuntime {
    static BehaviorController create(CredibleCrowdCitizens plugin, NPC npc) {
        var config = plugin.getConfig();
        long minDuration = Math.max(5, config.getLong("behaviors.duration-min-seconds", 30)) * 1000;
        long maxDuration = Math.max(minDuration, config.getLong("behaviors.duration-max-seconds", 180) * 1000);
        WeightedBehaviorPool pool = new WeightedBehaviorPool()
                .add(config.getDouble("behaviors.afk-weight", 35), () -> new AfkBehavior(duration(minDuration, maxDuration)))
                .add(config.getDouble("behaviors.look-around-weight", 20), () -> new LookAroundBehavior(duration(minDuration, maxDuration), 2500))
                .add(config.getDouble("behaviors.wander-weight", 45), () -> new WanderBehavior(duration(minDuration, maxDuration),
                        config.getDouble("behaviors.wander-radius", 20), config.getDouble("behaviors.wander-speed-min", .85),
                        config.getDouble("behaviors.wander-speed-max", 1.15), 1000, 6000));
        addPatrolRoutes(pool, config.getConfigurationSection("patrol-routes"), npc);
        addParkourCourses(pool, config.getConfigurationSection("parkour-courses"), npc);
        return new BehaviorController(new Actor(npc), new Context(), pool,
                config.getLong("behaviors.tick-millis", 250),
                config.getLong("behaviors.transition-pause-min-millis", 1000),
                config.getLong("behaviors.transition-pause-max-millis", 5000));
    }

    private static void addPatrolRoutes(WeightedBehaviorPool pool, ConfigurationSection routes, NPC npc) {
        if (routes == null || npc.getEntity() == null) return;
        for (String key : routes.getKeys(false)) {
            ConfigurationSection route = routes.getConfigurationSection(key); if (route == null) continue;
            List<Position> points = parsePositions(route.getStringList("waypoints"));
            if (points.isEmpty() || !nearCourse(npc, points.getFirst(), route.getDouble("activation-distance", 64))) continue;
            double weight = route.getDouble("weight", 10), speed = route.getDouble("speed", 1), pause = route.getDouble("pause-seconds", 2) * 1000;
            if (weight > 0) pool.add(weight, () -> new PatrolBehavior(points, speed, (long) pause));
        }
    }

    private static void addParkourCourses(WeightedBehaviorPool pool, ConfigurationSection courses, NPC npc) {
        if (courses == null || npc.getEntity() == null) return;
        for (String key : courses.getKeys(false)) {
            ConfigurationSection course = courses.getConfigurationSection(key); if (course == null) continue;
            List<ParkourCheckpoint> checkpoints = parseCheckpoints(course.getStringList("checkpoints"));
            if (checkpoints.size() < 2 || !nearCourse(npc, checkpoints.getFirst().position(), course.getDouble("activation-distance", 12))) continue;
            double weight = course.getDouble("weight", 8), arrival = course.getDouble("arrival-distance", 1.1), mistakes = course.getDouble("mistake-chance", .08);
            int failures = course.getInt("maximum-failures", 4); long timeout = course.getLong("jump-timeout-millis", 2500), retry = course.getLong("retry-delay-millis", 3000);
            if (weight > 0) pool.add(weight, () -> new ParkourBehavior(checkpoints, arrival, mistakes, failures, timeout, retry));
        }
    }

    private static boolean nearCourse(NPC npc, Position start, double distance) {
        Location here = npc.getEntity().getLocation(); Location target = to(start);
        return target != null && here.getWorld().equals(target.getWorld()) && here.distanceSquared(target) <= distance * distance;
    }

    private static List<Position> parsePositions(List<String> lines) {
        List<Position> result = new ArrayList<>(); for (String line : lines) { String[] p=line.split(","); if(p.length<4)continue; try { result.add(new Position(p[0].trim(),Double.parseDouble(p[1].trim()),Double.parseDouble(p[2].trim()),Double.parseDouble(p[3].trim()),p.length>4?Float.parseFloat(p[4].trim()):0,p.length>5?Float.parseFloat(p[5].trim()):0)); } catch(NumberFormatException ignored){} } return List.copyOf(result);
    }

    private static List<ParkourCheckpoint> parseCheckpoints(List<String> lines) {
        List<ParkourCheckpoint> result = new ArrayList<>(); for (String line : lines) { String[] p=line.split(","); if(p.length<4)continue; try { Position pos=new Position(p[0].trim(),Double.parseDouble(p[1].trim()),Double.parseDouble(p[2].trim()),Double.parseDouble(p[3].trim()),p.length>4?Float.parseFloat(p[4].trim()):0,p.length>5?Float.parseFloat(p[5].trim()):0);result.add(new ParkourCheckpoint(pos,p.length>6?Double.parseDouble(p[6].trim()):.42,p.length>7?Double.parseDouble(p[7].trim()):.42,p.length>8?Long.parseLong(p[8].trim()):350)); } catch(NumberFormatException ignored){} } return List.copyOf(result);
    }

    private static long duration(long min, long max) {
        return min == max ? min : ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    private record Actor(NPC npc) implements BehaviorActor {
        @Override public Position position() { return from(npc.getEntity().getLocation()); }
        @Override public boolean isSpawned() { return npc.isSpawned() && npc.getEntity() != null; }
        @Override public boolean isNavigating() { return npc.getNavigator().isNavigating(); }
        @Override public void navigateTo(Position destination, double speed) {
            Location target = to(destination); if (target == null) return;
            npc.getNavigator().setTarget(target);
            npc.getNavigator().getLocalParameters().speedModifier((float) speed);
        }
        @Override public void cancelNavigation() { npc.getNavigator().cancelNavigation(); }
        @Override public void lookAt(Position destination) { Location target = to(destination); if (target != null) npc.faceLocation(target); }
        @Override public void jumpToward(Position destination, double horizontalSpeed, double verticalSpeed) {
            Location target = to(destination); if (target == null || npc.getEntity() == null) return;
            Vector direction = target.toVector().subtract(npc.getEntity().getLocation().toVector()).setY(0);
            if (direction.lengthSquared() > 0) direction.normalize().multiply(horizontalSpeed);
            direction.setY(verticalSpeed); npc.getEntity().setVelocity(direction);
        }
    }

    private static final class Context implements BehaviorContext {
        @Override public long nowMillis() { return System.currentTimeMillis(); }
        @Override public RandomGenerator random() { return ThreadLocalRandom.current(); }
        @Override public Position randomWalkTarget(Position origin, double radius) {
            World world = Bukkit.getWorld(origin.world()); if (world == null) return null;
            double angle = random().nextDouble(Math.PI * 2), distance = random().nextDouble(2, Math.max(2.1, radius));
            int x = (int) Math.floor(origin.x() + Math.cos(angle) * distance);
            int z = (int) Math.floor(origin.z() + Math.sin(angle) * distance);
            int y = world.getHighestBlockYAt(x, z) + 1;
            return new Position(world.getName(), x + .5, y, z + .5, origin.yaw(), origin.pitch());
        }
        @Override public List<Position> attentionTargets(Position origin) {
            return Bukkit.getOnlinePlayers().stream().filter(player -> !player.hasMetadata("NPC"))
                    .map(Player::getLocation).map(CitizensBehaviorRuntime::from)
                    .filter(position -> position.world().equals(origin.world()))
                    .sorted(Comparator.comparingDouble(origin::distanceSquared)).limit(5).toList();
        }
    }

    private static Position from(Location location) {
        return new Position(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }
    private static Location to(Position position) {
        World world = Bukkit.getWorld(position.world());
        return world == null ? null : new Location(world, position.x(), position.y(), position.z(), position.yaw(), position.pitch());
    }
}
