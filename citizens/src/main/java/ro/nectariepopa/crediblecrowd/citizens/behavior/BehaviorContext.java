package ro.nectariepopa.crediblecrowd.citizens.behavior;

import java.util.List;
import java.util.random.RandomGenerator;

/** Runtime services/configuration supplied by the Citizens lifecycle layer. */
public interface BehaviorContext {
    long nowMillis();
    RandomGenerator random();

    /** Returns a safe, standable destination, or null when none is available. */
    Position randomWalkTarget(Position origin, double radius);

    /** Nearby points worth looking at (normally real players), closest first. */
    default List<Position> attentionTargets(Position origin) { return List.of(); }
}
