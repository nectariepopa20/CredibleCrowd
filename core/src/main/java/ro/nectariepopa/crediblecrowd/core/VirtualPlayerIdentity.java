package ro.nectariepopa.crediblecrowd.core;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Stable identity for a simulated player, independent of backend assignment and session churn. */
public record VirtualPlayerIdentity(UUID id, String name) {
  private static final String NAMESPACE = "crediblecrowd:virtual-player:";

  public VirtualPlayerIdentity {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(name, "name");
  }

  public static VirtualPlayerIdentity named(String name) {
    Objects.requireNonNull(name, "name");
    UUID id = UUID.nameUUIDFromBytes((NAMESPACE + name.toLowerCase(Locale.ROOT)).getBytes(StandardCharsets.UTF_8));
    return new VirtualPlayerIdentity(id, name);
  }
}
