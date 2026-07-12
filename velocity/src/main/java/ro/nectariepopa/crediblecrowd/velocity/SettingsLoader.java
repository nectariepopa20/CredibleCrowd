package ro.nectariepopa.crediblecrowd.velocity;
import java.io.*; import java.nio.charset.StandardCharsets; import java.nio.file.*; import java.time.ZoneId; import java.util.*;
import org.yaml.snakeyaml.Yaml; import ro.nectariepopa.crediblecrowd.core.*;
final class SettingsLoader {
 static VelocitySettings load(Path file) throws IOException {
  try(Reader r=Files.newBufferedReader(file,StandardCharsets.UTF_8)){
   Map<String,Object> root=new Yaml().load(r); Map<String,Object> periods=map(root.get("periods"));
   String[] keys={"00-04","04-08","08-12","12-16","16-20","20-00"}; List<TimeRange> ranges=new ArrayList<>();
   for(String k:keys){Map<String,Object> p=map(periods.get(k));ranges.add(new TimeRange(integer(p,"min"),integer(p,"max")));}
   List<CrowdConfig.ServerWeight> servers=new ArrayList<>(); for(Object o:(List<?>)root.get("servers")){Map<String,Object>s=map(o);servers.add(new CrowdConfig.ServerWeight(String.valueOf(s.get("name")),number(s,"weight")));}
   CrowdConfig c=new CrowdConfig(ZoneId.of(String.valueOf(root.get("timezone"))),ranges,servers,integer(root,"update-seconds"),integer(root,"activity-tick-seconds",10),number(root,"jitter"),((Number)root.get("seed")).longValue(),number(root,"turnover-per-minute",0.06),number(root,"quiet-tick-chance",0.28),number(root,"max-correction-ratio",0.025));
   int pingMin=integer(root,"fake-ping-min",35),pingMax=integer(root,"fake-ping-max",95);if(pingMin<0||pingMax<pingMin)throw new IllegalArgumentException("Invalid fake ping range");return new VelocitySettings(c,"add-to-real".equalsIgnoreCase(String.valueOf(root.get("display-mode"))),integer(root,"maximum-players"),Boolean.TRUE.equals(root.get("reserve-fake-names")),String.valueOf(root.get("already-connected-message")),pingMin,pingMax);
  }
 }
 @SuppressWarnings("unchecked") private static Map<String,Object> map(Object o){return (Map<String,Object>)o;} private static int integer(Map<String,Object>m,String k){return ((Number)m.get(k)).intValue();} private static int integer(Map<String,Object>m,String k,int fallback){return m.get(k) instanceof Number n?n.intValue():fallback;} private static double number(Map<String,Object>m,String k){return ((Number)m.get(k)).doubleValue();} private static double number(Map<String,Object>m,String k,double fallback){return m.get(k) instanceof Number n?n.doubleValue():fallback;}
}
