package ro.nectariepopa.crediblecrowd.core;
import static org.junit.jupiter.api.Assertions.*;
import java.time.*; import java.util.*; import org.junit.jupiter.api.Test;
class CrowdEngineTest {
 @Test void stableWithinBucketAndExcludesRealNames(){var c=config(3,0);var e=new CrowdEngine(c);var n=List.of("Alpha","Bravo","Charlie","Delta");var a=e.snapshot(Instant.parse("2026-01-01T01:00:01Z"),n,Set.of("Bravo"));var b=e.snapshot(Instant.parse("2026-01-01T01:00:40Z"),n,Set.of("Bravo"));assertEquals(a.fakeNames(),b.fakeNames());assertFalse(a.fakeNames().contains("Bravo"));assertEquals(3,a.fakeCount());}
 @Test void simulatorRetainsPopulationWithoutTurnover(){var s=new CrowdSimulator(config(3,0));var names=List.of("Alpha","Bravo","Charlie","Delta","Echo");var a=s.advance(Instant.parse("2026-01-01T01:00:00Z"),names,Set.of());var b=s.advance(Instant.parse("2026-01-01T01:00:10Z"),names,Set.of());assertEquals(a.fakeNames(),b.fakeNames());}
 @Test void simulatorScalesTurnoverButDoesNotReplaceEveryone(){var s=new CrowdSimulator(config(100,0.12));var names=java.util.stream.IntStream.range(0,150).mapToObj(i->String.format("User%03d",i)).toList();var a=s.advance(Instant.parse("2026-01-01T01:00:00Z"),names,Set.of());var b=s.advance(Instant.parse("2026-01-01T01:00:10Z"),names,Set.of());long retained=a.fakeNames().stream().filter(b.fakeNames()::contains).count();assertTrue(retained>=90&&retained<100);}
 private CrowdConfig config(int count,double turnover){return new CrowdConfig(ZoneId.of("UTC"),Collections.nCopies(6,new TimeRange(count,count)),List.of(new CrowdConfig.ServerWeight("lobby",1)),60,10,.1,42,turnover,0,.03);}
}
