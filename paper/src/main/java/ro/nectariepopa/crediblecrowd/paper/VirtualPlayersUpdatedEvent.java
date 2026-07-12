package ro.nectariepopa.crediblecrowd.paper;

import java.util.List;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Fired after an authoritative Velocity snapshot changes the local virtual-player state. */
public final class VirtualPlayersUpdatedEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();
  private final long revision;
  private final List<VirtualPlayer> current;
  private final List<VirtualPlayer> joined;
  private final List<VirtualPlayer> departed;

  public VirtualPlayersUpdatedEvent(long revision, List<VirtualPlayer> current, List<VirtualPlayer> joined, List<VirtualPlayer> departed) {
    this.revision = revision;
    this.current = List.copyOf(current);
    this.joined = List.copyOf(joined);
    this.departed = List.copyOf(departed);
  }

  public long getRevision() { return revision; }
  public List<VirtualPlayer> getCurrent() { return current; }
  public List<VirtualPlayer> getJoined() { return joined; }
  public List<VirtualPlayer> getDeparted() { return departed; }
  @Override public HandlerList getHandlers() { return HANDLERS; }
  public static HandlerList getHandlerList() { return HANDLERS; }
}
