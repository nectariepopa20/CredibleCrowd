package ro.nectariepopa.crediblecrowd.citizens.behavior;

import java.util.List;

public final class LookAroundBehavior extends AbstractTimedBehavior {
    private final long lookIntervalMillis;
    private long nextLook;

    public LookAroundBehavior(long durationMillis, long lookIntervalMillis) {
        super(durationMillis); this.lookIntervalMillis = Math.max(250, lookIntervalMillis);
    }
    @Override public String id() { return "look-around"; }
    @Override public void enter(BehaviorActor actor, BehaviorContext context) {
        super.enter(actor, context); actor.cancelNavigation(); nextLook = context.nowMillis();
    }
    @Override public BehaviorResult tick(BehaviorActor actor, BehaviorContext context) {
        if (expired(context)) return BehaviorResult.COMPLETE;
        if (context.nowMillis() >= nextLook) {
            List<Position> targets = context.attentionTargets(actor.position());
            Position target = targets.isEmpty()
                ? actor.position().offset(context.random().nextDouble(-5, 5), context.random().nextDouble(-1, 2), context.random().nextDouble(-5, 5))
                : targets.get(context.random().nextInt(Math.min(3, targets.size())));
            actor.lookAt(target);
            nextLook = context.nowMillis() + lookIntervalMillis + context.random().nextLong(lookIntervalMillis + 1);
        }
        return BehaviorResult.RUNNING;
    }
}
