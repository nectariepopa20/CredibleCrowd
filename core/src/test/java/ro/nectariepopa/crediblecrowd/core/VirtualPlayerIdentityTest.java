package ro.nectariepopa.crediblecrowd.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class VirtualPlayerIdentityTest {
  @Test void identityIsStableAndCaseInsensitive() {
    var first = VirtualPlayerIdentity.named("ExamplePlayer");
    var second = VirtualPlayerIdentity.named("exampleplayer");
    assertEquals(first.id(), second.id());
    assertEquals("ExamplePlayer", first.name());
  }

  @Test void differentNamesHaveDifferentIdentities() {
    assertNotEquals(VirtualPlayerIdentity.named("Alpha").id(), VirtualPlayerIdentity.named("Bravo").id());
  }
}
