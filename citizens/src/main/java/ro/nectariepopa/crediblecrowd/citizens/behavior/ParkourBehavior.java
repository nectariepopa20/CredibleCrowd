package ro.nectariepopa.crediblecrowd.citizens.behavior;

import java.util.List;

/** Executes an explicitly calibrated course; arbitrary-world parkour is intentionally unsupported. */
public final class ParkourBehavior implements Behavior {
    private final List<ParkourCheckpoint> checkpoints;
    private final double arrivalDistanceSquared, mistakeChance;
    private final int maxFailures;
    private final long jumpTimeoutMillis, retryDelayMillis;
    private int index, failures;
    private long jumpStarted, retryAt;
    private boolean jumping;

    public ParkourBehavior(List<ParkourCheckpoint> checkpoints, double arrivalDistance, double mistakeChance,
                           int maxFailures, long jumpTimeoutMillis, long retryDelayMillis) {
        if (checkpoints == null || checkpoints.size() < 2) throw new IllegalArgumentException("at least two checkpoints are required");
        this.checkpoints = List.copyOf(checkpoints);
        this.arrivalDistanceSquared = Math.max(.1, arrivalDistance) * Math.max(.1, arrivalDistance);
        this.mistakeChance = Math.max(0, Math.min(1, mistakeChance));
        this.maxFailures = Math.max(1, maxFailures); this.jumpTimeoutMillis = Math.max(500, jumpTimeoutMillis);
        this.retryDelayMillis = Math.max(0, retryDelayMillis);
    }
    @Override public String id() { return "parkour"; }
    @Override public void enter(BehaviorActor actor, BehaviorContext context) { index = 0; failures = 0; jumping = false; retryAt = context.nowMillis(); }
    @Override public BehaviorResult tick(BehaviorActor actor, BehaviorContext context) {
        if (index == 0) {
            Position start = checkpoints.getFirst().position();
            if (actor.position().distanceSquared(start) <= arrivalDistanceSquared) { actor.cancelNavigation(); index = 1; retryAt = context.nowMillis() + checkpoints.getFirst().settleMillis(); }
            else if (!actor.isNavigating()) actor.navigateTo(start, 1.0);
            return BehaviorResult.RUNNING;
        }
        if (index >= checkpoints.size()) return BehaviorResult.COMPLETE;
        ParkourCheckpoint target = checkpoints.get(index);
        if (actor.position().distanceSquared(target.position()) <= arrivalDistanceSquared) {
            index++; failures = 0; jumping = false;
            retryAt = context.nowMillis() + target.settleMillis();
            return index >= checkpoints.size() ? BehaviorResult.COMPLETE : BehaviorResult.RUNNING;
        }
        if (jumping && context.nowMillis() - jumpStarted >= jumpTimeoutMillis) {
            jumping = false; failures++; retryAt = context.nowMillis() + retryDelayMillis;
            if (failures >= maxFailures) return BehaviorResult.FAILED;
        }
        if (!jumping && context.nowMillis() >= retryAt) {
            double horizontal = target.horizontalSpeed(), vertical = target.verticalSpeed();
            if (context.random().nextDouble() < mistakeChance) {
                // Small under/over-shoots look human while failure recovery prevents permanent stalls.
                horizontal *= context.random().nextBoolean() ? .72 : 1.22;
                vertical *= context.random().nextDouble(.78, 1.12);
            }
            actor.lookAt(target.position()); actor.jumpToward(target.position(), horizontal, vertical);
            jumping = true; jumpStarted = context.nowMillis();
        }
        return BehaviorResult.RUNNING;
    }
}
