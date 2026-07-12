package ro.nectariepopa.crediblecrowd.core;

import java.time.*;
import java.util.*;

public final class CrowdEngine {
  private final CrowdConfig config;
  public CrowdEngine(CrowdConfig config) { this.config = config; }

  public CrowdSnapshot snapshot(Instant now, List<String> availableNames, Set<String> realNames) {
    long bucket = now.getEpochSecond() / config.updateSeconds();
    SplittableRandom random = new SplittableRandom(config.seed() ^ mix(bucket));
    int desired = desiredCount(now, random);
    List<String> pool = availableNames.stream().map(String::trim).filter(this::validName)
        .filter(n -> realNames.stream().noneMatch(r -> r.equalsIgnoreCase(n))).distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    desired = Math.min(desired, pool.size());
    ArrayList<String> shuffled = new ArrayList<>(pool);
    shuffle(shuffled, random);
    List<String> selected = List.copyOf(shuffled.subList(0, desired));
    LinkedHashMap<String, List<String>> allocation = new LinkedHashMap<>();
    config.servers().forEach(s -> allocation.put(s.name(), new ArrayList<>()));
    double weightTotal = config.servers().stream().mapToDouble(CrowdConfig.ServerWeight::weight).sum();
    for (String name : selected) {
      double ticket = random.nextDouble(weightTotal), cursor = 0;
      for (CrowdConfig.ServerWeight server : config.servers()) {
        cursor += server.weight();
        if (ticket <= cursor) { allocation.get(server.name()).add(name); break; }
      }
    }
    return new CrowdSnapshot(now, desired, selected, allocation);
  }

  int desiredCount(Instant now, SplittableRandom random) {
    ZonedDateTime local = now.atZone(config.zone());
    double block = (local.getHour() % 4 + local.getMinute() / 60.0 + local.getSecond() / 3600.0) / 4.0;
    int index = local.getHour() / 4, next = (index + 1) % 6;
    TimeRange a = config.ranges().get(index), b = config.ranges().get(next);
    double smooth = block * block * (3 - 2 * block);
    double low = lerp(a.min(), b.min(), smooth), high = lerp(a.max(), b.max(), smooth);
    double wave = 0.5 + 0.27 * Math.sin((now.getEpochSecond() / 420.0) + config.seed()) + (random.nextDouble() - 0.5) * config.jitter();
    wave = Math.max(0.05, Math.min(0.95, wave));
    return Math.max(0, (int)Math.round(low + (high - low) * wave));
  }
  private boolean validName(String n) { return n.matches("[A-Za-z0-9_]{3,16}"); }
  private static double lerp(double a, double b, double t) { return a + (b-a)*t; }
  private static long mix(long z) { z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdl; z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53l; return z ^ (z >>> 33); }
  private static <T> void shuffle(List<T> list, SplittableRandom r) { for(int i=list.size()-1;i>0;i--){int j=r.nextInt(i+1); Collections.swap(list,i,j);} }
}

