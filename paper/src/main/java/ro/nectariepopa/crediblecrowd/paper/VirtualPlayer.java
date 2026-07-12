package ro.nectariepopa.crediblecrowd.paper;

import java.util.Objects;
import java.util.UUID;

/** Public Paper bridge view of a stable virtual player identity. */
public record VirtualPlayer(UUID id, String name) {
  public VirtualPlayer {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(name, "name");
  }
}
