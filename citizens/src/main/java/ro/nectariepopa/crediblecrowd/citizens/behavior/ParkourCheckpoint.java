package ro.nectariepopa.crediblecrowd.citizens.behavior;

public record ParkourCheckpoint(Position position, double horizontalSpeed, double verticalSpeed, long settleMillis) {
    public ParkourCheckpoint {
        if (position == null) throw new IllegalArgumentException("position is required");
        horizontalSpeed = Math.max(.1, horizontalSpeed);
        verticalSpeed = Math.max(.1, verticalSpeed);
        settleMillis = Math.max(0, settleMillis);
    }
}
