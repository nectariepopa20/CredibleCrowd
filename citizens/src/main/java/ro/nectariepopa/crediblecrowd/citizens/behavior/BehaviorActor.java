package ro.nectariepopa.crediblecrowd.citizens.behavior;

/**
 * Narrow port implemented by the lifecycle module with a Citizens NPC.
 * Keeping Citizens calls behind this interface makes behaviour deterministic and testable.
 */
public interface BehaviorActor {
    Position position();
    boolean isSpawned();
    boolean isNavigating();
    void navigateTo(Position destination, double speed);
    void cancelNavigation();
    void lookAt(Position destination);
    void jumpToward(Position destination, double horizontalSpeed, double verticalSpeed);
}
