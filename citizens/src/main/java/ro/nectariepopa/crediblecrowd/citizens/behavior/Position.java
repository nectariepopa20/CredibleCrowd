package ro.nectariepopa.crediblecrowd.citizens.behavior;

/** Immutable, world-aware position used by the Citizens adapter. */
public record Position(String world, double x, double y, double z, float yaw, float pitch) {
    public Position {
        if (world == null || world.isBlank()) throw new IllegalArgumentException("world is required");
    }

    public double distanceSquared(Position other) {
        if (!world.equals(other.world)) return Double.POSITIVE_INFINITY;
        double dx = x - other.x, dy = y - other.y, dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public Position offset(double dx, double dy, double dz) {
        return new Position(world, x + dx, y + dy, z + dz, yaw, pitch);
    }
}
