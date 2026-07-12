package ro.nectariepopa.crediblecrowd.citizens.behavior;

public final class WanderBehavior extends AbstractTimedBehavior {
    private final double radius, minSpeed, maxSpeed;
    private final long pauseMinMillis, pauseMaxMillis;
    private long pausedUntil;
    private boolean travelling;

    public WanderBehavior(long durationMillis, double radius, double minSpeed, double maxSpeed,
                          long pauseMinMillis, long pauseMaxMillis) {
        super(durationMillis); this.radius = Math.max(1, radius);
        this.minSpeed = Math.max(.1, minSpeed); this.maxSpeed = Math.max(this.minSpeed, maxSpeed);
        this.pauseMinMillis = Math.max(0, pauseMinMillis); this.pauseMaxMillis = Math.max(this.pauseMinMillis, pauseMaxMillis);
    }
    @Override public String id() { return "wander"; }
    @Override public BehaviorResult tick(BehaviorActor actor, BehaviorContext context) {
        if (expired(context)) return BehaviorResult.COMPLETE;
        if (travelling && !actor.isNavigating()) {
            travelling = false;
            pausedUntil = context.nowMillis() + randomBetween(context, pauseMinMillis, pauseMaxMillis);
        }
        if (travelling || context.nowMillis() < pausedUntil) return BehaviorResult.RUNNING;
        Position target = context.randomWalkTarget(actor.position(), radius);
        if (target == null) { pausedUntil = context.nowMillis() + 1_000; return BehaviorResult.RUNNING; }
        double speed = context.random().nextDouble(minSpeed, Math.nextUp(maxSpeed));
        actor.navigateTo(target, speed);
        travelling = true;
        return BehaviorResult.RUNNING;
    }
    private static long randomBetween(BehaviorContext c, long min, long max) {
        return max == min ? min : c.random().nextLong(min, max + 1);
    }
}
