package ro.nectariepopa.crediblecrowd.core;

import java.time.ZoneId;
import java.util.List;

public record CrowdConfig(ZoneId zone, List<TimeRange> ranges, List<ServerWeight> servers,
                          int updateSeconds, int activityTickSeconds, double jitter, long seed,
                          double turnoverPerMinute, double quietTickChance, double maxCorrectionRatio) {
  public CrowdConfig {
    ranges = List.copyOf(ranges); servers = List.copyOf(servers);
    if (ranges.size() != 6) throw new IllegalArgumentException("Exactly 6 four-hour ranges are required");
    if (servers.isEmpty()) throw new IllegalArgumentException("At least one server is required");
    if (updateSeconds < 10 || activityTickSeconds < 1 || jitter < 0 || jitter > 0.5
        || turnoverPerMinute < 0 || turnoverPerMinute > 1 || quietTickChance < 0 || quietTickChance > 1
        || maxCorrectionRatio <= 0 || maxCorrectionRatio > 1) throw new IllegalArgumentException("Invalid tuning values");
  }
  public record ServerWeight(String name, double weight) {
    public ServerWeight { if (name == null || name.isBlank() || weight <= 0) throw new IllegalArgumentException("Invalid server weight"); }
  }
}
