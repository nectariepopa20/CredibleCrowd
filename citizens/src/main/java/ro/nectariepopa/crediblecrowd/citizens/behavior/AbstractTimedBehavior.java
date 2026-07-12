package ro.nectariepopa.crediblecrowd.citizens.behavior;

abstract class AbstractTimedBehavior implements Behavior {
    protected long enteredAt;
    protected final long durationMillis;

    protected AbstractTimedBehavior(long durationMillis) {
        this.durationMillis = Math.max(0, durationMillis);
    }

    @Override public void enter(BehaviorActor actor, BehaviorContext context) { enteredAt = context.nowMillis(); }
    protected boolean expired(BehaviorContext context) { return context.nowMillis() - enteredAt >= durationMillis; }
}
