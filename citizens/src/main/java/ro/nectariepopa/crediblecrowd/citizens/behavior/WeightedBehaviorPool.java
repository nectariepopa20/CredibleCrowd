package ro.nectariepopa.crediblecrowd.citizens.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public final class WeightedBehaviorPool {
    private record Entry(double weight, Supplier<? extends Behavior> factory) {}
    private final List<Entry> entries = new ArrayList<>();
    private double totalWeight;

    public WeightedBehaviorPool add(double weight, Supplier<? extends Behavior> factory) {
        if (!Double.isFinite(weight) || weight <= 0) throw new IllegalArgumentException("weight must be positive");
        if (factory == null) throw new IllegalArgumentException("factory is required");
        entries.add(new Entry(weight, factory)); totalWeight += weight; return this;
    }

    public Behavior choose(RandomGenerator random) {
        if (entries.isEmpty()) throw new IllegalStateException("behavior pool is empty");
        double selected = random.nextDouble(totalWeight);
        for (Entry entry : entries) { selected -= entry.weight; if (selected < 0) return entry.factory.get(); }
        return entries.getLast().factory.get();
    }
}
