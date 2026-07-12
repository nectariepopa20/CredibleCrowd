package ro.nectariepopa.crediblecrowd.citizens.behavior;

public interface Behavior {
    String id();
    void enter(BehaviorActor actor, BehaviorContext context);
    BehaviorResult tick(BehaviorActor actor, BehaviorContext context);
    default void exit(BehaviorActor actor, BehaviorContext context) { actor.cancelNavigation(); }
}
