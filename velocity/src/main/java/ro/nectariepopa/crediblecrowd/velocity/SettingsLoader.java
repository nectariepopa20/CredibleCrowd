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
   CrowdConfig c=new CrowdConfig(ZoneId.of(String.valueOf(root.get("timezone"))),ranges,servers,integer(root,"update-seconds"),number(root,"jitter"),((Number)root.get("seed")).longValue(),integer(root,"ping-sample-size"));
   return new VelocitySettings(c,"add-to-real".equalsIgnoreCase(String.valueOf(root.get("display-mode"))),integer(root,"maximum-players"),Boolean.TRUE.equals(root.get("reserve-fake-names")),String.valueOf(root.get("already-connected-message")));
  }
 }
 @SuppressWarnings("unchecked") private static Map<String,Object> map(Object o){return (Map<String,Object>)o;} private static int integer(Map<String,Object>m,String k){return ((Number)m.get(k)).intValue();} private static double number(Map<String,Object>m,String k){return ((Number)m.get(k)).doubleValue();}
}

