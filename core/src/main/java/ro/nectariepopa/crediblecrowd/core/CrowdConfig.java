package ro.nectariepopa.crediblecrowd.core;

import java.time.ZoneId;
import java.util.List;

public record CrowdConfig(ZoneId zone, List<TimeRange> ranges, List<ServerWeight> servers,
                          int updateSeconds, double jitter, long seed, int sampleSize) {
  public CrowdConfig {
    ranges = List.copyOf(ranges); servers = List.copyOf(servers);
    if (ranges.size() != 6) throw new IllegalArgumentException("Exactly 6 four-hour ranges are required");
    if (servers.isEmpty()) throw new IllegalArgumentException("At least one server is required");
    if (updateSeconds < 10 || jitter < 0 || jitter > 0.5 || sampleSize < 0) throw new IllegalArgumentException("Invalid tuning values");
  }
  public record ServerWeight(String name, double weight) {
    public ServerWeight { if (name == null || name.isBlank() || weight <= 0) throw new IllegalArgumentException("Invalid server weight"); }
  }
}

