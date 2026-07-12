package ro.nectariepopa.crediblecrowd.citizens.behavior;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BehaviorControllerTest {
    @Test void throttlesAndTransitionsAfterCompletion() {
        TestContext context = new TestContext(); TestActor actor = new TestActor();
        WeightedBehaviorPool pool = new WeightedBehaviorPool().add(1, () -> new AfkBehavior(100));
        BehaviorController controller = new BehaviorController(actor, context, pool, 50, 20, 20);
        controller.tick(); assertEquals("afk", controller.currentBehavior());
        context.now = 49; controller.tick(); assertEquals("afk", controller.currentBehavior());
        context.now = 100; controller.tick(); assertEquals("idle", controller.currentBehavior());
        context.now = 119; controller.tick(); assertEquals("idle", controller.currentBehavior());
        context.now = 150; controller.tick(); assertEquals("afk", controller.currentBehavior());
    }

    @Test void parkourRecoversAndEventuallyFails() {
        TestContext context = new TestContext(); TestActor actor = new TestActor();
        var start = actor.position; var end = start.offset(2, 1, 0);
        ParkourBehavior behavior = new ParkourBehavior(
            java.util.List.of(new ParkourCheckpoint(start, 1, 1, 0), new ParkourCheckpoint(end, 1, 1, 0)),
            .2, 0, 2, 500, 100);
        behavior.enter(actor, context); assertEquals(BehaviorResult.RUNNING, behavior.tick(actor, context));
        assertEquals(BehaviorResult.RUNNING, behavior.tick(actor, context));
        assertEquals(1, actor.jumps);
        context.now = 500; behavior.tick(actor, context);
        context.now = 600; behavior.tick(actor, context); assertEquals(2, actor.jumps);
        context.now = 1100; assertEquals(BehaviorResult.FAILED, behavior.tick(actor, context));
    }

    private static final class TestContext implements BehaviorContext {
        long now; final Random random = new Random(7);
        public long nowMillis() { return now; }
        public Random random() { return random; }
        public Position randomWalkTarget(Position origin, double radius) { return origin.offset(1, 0, 0); }
    }
    private static final class TestActor implements BehaviorActor {
        Position position = new Position("world", 0, 64, 0, 0, 0); int jumps;
        public Position position() { return position; }
        public boolean isSpawned() { return true; }
        public boolean isNavigating() { return false; }
        public void navigateTo(Position destination, double speed) {}
        public void cancelNavigation() {}
        public void lookAt(Position destination) {}
        public void jumpToward(Position destination, double horizontalSpeed, double verticalSpeed) { jumps++; }
    }
}
