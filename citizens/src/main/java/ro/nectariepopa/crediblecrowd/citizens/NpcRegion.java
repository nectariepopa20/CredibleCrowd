package ro.nectariepopa.crediblecrowd.citizens;

import org.bukkit.Location;

record NpcRegion(String world, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    boolean contains(Location location) {
        return location != null && location.getWorld() != null && location.getWorld().getName().equalsIgnoreCase(world)
                && location.getX() >= minX && location.getX() <= maxX
                && location.getY() >= minY && location.getY() <= maxY
                && location.getZ() >= minZ && location.getZ() <= maxZ;
    }
}
