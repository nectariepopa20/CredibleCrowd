package ro.nectariepopa.crediblecrowd.citizens.behavior;

public final class AfkBehavior extends AbstractTimedBehavior {
    public AfkBehavior(long durationMillis) { super(durationMillis); }
    @Override public String id() { return "afk"; }
    @Override public void enter(BehaviorActor actor, BehaviorContext context) { super.enter(actor, context); actor.cancelNavigation(); }
    @Override public BehaviorResult tick(BehaviorActor actor, BehaviorContext context) {
        return expired(context) ? BehaviorResult.COMPLETE : BehaviorResult.RUNNING;
    }
}
