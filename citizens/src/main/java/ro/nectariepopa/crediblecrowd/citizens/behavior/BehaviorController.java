package ro.nectariepopa.crediblecrowd.citizens.behavior;

import java.util.function.Supplier;

/** Per-NPC state machine. Call freely from a scheduler; expensive behaviour ticks are throttled here. */
public final class BehaviorController {
    private final BehaviorActor actor;
    private final BehaviorContext context;
    private final WeightedBehaviorPool pool;
    private final long tickIntervalMillis, transitionPauseMinMillis, transitionPauseMaxMillis;
    private Supplier<? extends Behavior> firstBehavior;
    private Behavior current;
    private long nextTick, nextBehaviorAt;

    public BehaviorController(BehaviorActor actor, BehaviorContext context, WeightedBehaviorPool pool,
                              long tickIntervalMillis, long transitionPauseMinMillis, long transitionPauseMaxMillis) {
        this(actor, context, pool, tickIntervalMillis, transitionPauseMinMillis, transitionPauseMaxMillis, null);
    }

    public BehaviorController(BehaviorActor actor, BehaviorContext context, WeightedBehaviorPool pool,
                              long tickIntervalMillis, long transitionPauseMinMillis, long transitionPauseMaxMillis,
                              Supplier<? extends Behavior> firstBehavior) {
        this.actor = actor; this.context = context; this.pool = pool;
        this.tickIntervalMillis = Math.max(50, tickIntervalMillis);
        this.transitionPauseMinMillis = Math.max(0, transitionPauseMinMillis);
        this.transitionPauseMaxMillis = Math.max(this.transitionPauseMinMillis, transitionPauseMaxMillis);
        this.firstBehavior = firstBehavior;
    }

    public void tick() {
        long now = context.nowMillis();
        if (!actor.isSpawned() || now < nextTick) return;
        nextTick = now + tickIntervalMillis;
        if (current == null) {
            if (now < nextBehaviorAt) return;
            current = firstBehavior == null ? pool.choose(context.random()) : firstBehavior.get();
            firstBehavior = null;
            current.enter(actor, context);
        }
        BehaviorResult result = current.tick(actor, context);
        if (result != BehaviorResult.RUNNING) transition(now);
    }

    public void stop() {
        if (current != null) { current.exit(actor, context); current = null; }
    }

    public String currentBehavior() { return current == null ? "idle" : current.id(); }

    private void transition(long now) {
        current.exit(actor, context); current = null;
        long pause = transitionPauseMinMillis == transitionPauseMaxMillis ? transitionPauseMinMillis
            : context.random().nextLong(transitionPauseMinMillis, transitionPauseMaxMillis + 1);
        nextBehaviorAt = now + pause;
    }
}
