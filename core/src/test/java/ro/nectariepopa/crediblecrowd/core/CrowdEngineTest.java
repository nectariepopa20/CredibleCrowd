package ro.nectariepopa.crediblecrowd.core;
import static org.junit.jupiter.api.Assertions.*;
import java.time.*; import java.util.*; import org.junit.jupiter.api.Test;
class CrowdEngineTest {
 @Test void stableWithinBucketAndExcludesRealNames(){var c=new CrowdConfig(ZoneId.of("UTC"),Collections.nCopies(6,new TimeRange(3,3)),List.of(new CrowdConfig.ServerWeight("lobby",1)),60,.1,42);var e=new CrowdEngine(c);var n=List.of("Alpha","Bravo","Charlie","Delta");var a=e.snapshot(Instant.parse("2026-01-01T01:00:01Z"),n,Set.of("Bravo"));var b=e.snapshot(Instant.parse("2026-01-01T01:00:40Z"),n,Set.of("Bravo"));assertEquals(a.fakeNames(),b.fakeNames());assertFalse(a.fakeNames().contains("Bravo"));assertEquals(3,a.fakeCount());}
}
