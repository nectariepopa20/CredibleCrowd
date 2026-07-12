package ro.nectariepopa.crediblecrowd.citizens.behavior;

import java.util.List;

public final class PatrolBehavior implements Behavior {
    private final List<Position> waypoints;
    private final double speed;
    private final long pauseMillis;
    private int index;
    private boolean travelling;
    private long pausedUntil;

    public PatrolBehavior(List<Position> waypoints, double speed, long pauseMillis) {
        if (waypoints == null || waypoints.isEmpty()) throw new IllegalArgumentException("waypoints are required");
        this.waypoints = List.copyOf(waypoints); this.speed = Math.max(.1, speed); this.pauseMillis = Math.max(0, pauseMillis);
    }
    @Override public String id() { return "patrol"; }
    @Override public void enter(BehaviorActor actor, BehaviorContext context) { index = nearest(actor.position()); travelling = false; }
    @Override public BehaviorResult tick(BehaviorActor actor, BehaviorContext context) {
        if (travelling && !actor.isNavigating()) { travelling = false; index = (index + 1) % waypoints.size(); pausedUntil = context.nowMillis() + pauseMillis; }
        if (!travelling && context.nowMillis() >= pausedUntil) { actor.navigateTo(waypoints.get(index), speed); travelling = true; }
        return BehaviorResult.RUNNING;
    }
    private int nearest(Position p) {
        int best = 0; double distance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < waypoints.size(); i++) { double d = p.distanceSquared(waypoints.get(i)); if (d < distance) { distance = d; best = i; } }
        return best;
    }
}
