package ro.nectariepopa.crediblecrowd.core;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CrowdSnapshot(Instant generatedAt, int fakeCount, List<String> fakeNames, Map<String, List<String>> byServer) {
  public CrowdSnapshot { fakeNames = List.copyOf(fakeNames); byServer = byServer.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> List.copyOf(e.getValue()))); }
}

