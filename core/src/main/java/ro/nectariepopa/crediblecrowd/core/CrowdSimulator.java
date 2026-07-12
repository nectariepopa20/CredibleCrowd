package ro.nectariepopa.crediblecrowd.core;

import java.time.Instant; import java.util.*;

/** Stateful population simulation: identities persist and only plausible churn is applied per tick. */
public final class CrowdSimulator {
 private final CrowdConfig config; private final CrowdEngine curve; private final LinkedHashMap<String,String> active=new LinkedHashMap<>(); private long step;
 public CrowdSimulator(CrowdConfig config){this.config=config;this.curve=new CrowdEngine(config);}

 public synchronized CrowdSnapshot advance(Instant now,List<String> availableNames,Set<String> realNames){
  SplittableRandom random=new SplittableRandom(config.seed() ^ mix(now.getEpochSecond()/config.activityTickSeconds()) ^ ++step);
  List<String> valid=availableNames.stream().map(String::trim).filter(n->n.matches("[A-Za-z0-9_]{3,16}")).distinct().toList();
  Set<String> realLower=new HashSet<>();realNames.forEach(n->realLower.add(n.toLowerCase(Locale.ROOT)));
  active.keySet().removeIf(n->realLower.contains(n.toLowerCase(Locale.ROOT))||valid.stream().noneMatch(v->v.equalsIgnoreCase(n)));
  int desired=Math.min(curve.targetCount(now),(int)valid.stream().filter(n->!realLower.contains(n.toLowerCase(Locale.ROOT))).count());
  if(active.isEmpty()){addRandom(valid,realLower,desired,random);return snapshot(now);}
  int current=active.size(), delta=desired-current;
  int maxCorrection=Math.max(1,(int)Math.ceil(current*config.maxCorrectionRatio()));
  int correction=Integer.signum(delta)*Math.min(Math.abs(delta),maxCorrection);
  double expected=current*config.turnoverPerMinute()*config.activityTickSeconds()/60.0;
  int turnover=config.quietTickChance()>random.nextDouble()?0:stochastic(expected,random);
  int departures=Math.min(current,turnover+Math.max(0,-correction));
  removeRandom(departures,random);
  int arrivals=turnover+Math.max(0,correction);
  addRandom(valid,realLower,arrivals,random);
  return snapshot(now);
 }
 private void removeRandom(int count,SplittableRandom random){ArrayList<String> names=new ArrayList<>(active.keySet());shuffle(names,random);for(int i=0;i<Math.min(count,names.size());i++)active.remove(names.get(i));}
 private void addRandom(List<String> valid,Set<String> realLower,int count,SplittableRandom random){ArrayList<String> pool=new ArrayList<>();for(String n:valid)if(!realLower.contains(n.toLowerCase(Locale.ROOT))&&active.keySet().stream().noneMatch(a->a.equalsIgnoreCase(n)))pool.add(n);shuffle(pool,random);for(int i=0;i<Math.min(count,pool.size());i++)active.put(pool.get(i),pickServer(random));}
 private String pickServer(SplittableRandom random){double total=config.servers().stream().mapToDouble(CrowdConfig.ServerWeight::weight).sum(),ticket=random.nextDouble(total),cursor=0;for(var s:config.servers()){cursor+=s.weight();if(ticket<=cursor)return s.name();}return config.servers().getLast().name();}
 private CrowdSnapshot snapshot(Instant now){LinkedHashMap<String,List<String>> byServer=new LinkedHashMap<>();config.servers().forEach(s->byServer.put(s.name(),new ArrayList<>()));active.forEach((name,server)->byServer.computeIfAbsent(server,k->new ArrayList<>()).add(name));return new CrowdSnapshot(now,active.size(),new ArrayList<>(active.keySet()),byServer);}
 private static int stochastic(double value,SplittableRandom random){int whole=(int)Math.floor(value);return whole+(random.nextDouble()<value-whole?1:0);}
 private static <T> void shuffle(List<T> list,SplittableRandom random){for(int i=list.size()-1;i>0;i--)Collections.swap(list,i,random.nextInt(i+1));}
 private static long mix(long z){z=(z^(z>>>33))*0xff51afd7ed558ccdl;z=(z^(z>>>33))*0xc4ceb9fe1a85ec53l;return z^(z>>>33);}
}
