package ro.nectariepopa.crediblecrowd.velocity;

import java.net.URI; import java.net.http.*; import java.time.Duration; import java.util.*; import java.util.concurrent.*; import java.util.regex.*;

final class MojangNameResolver {
 private static final Pattern NAME=Pattern.compile("\\\"name\\\"\\s*:\\s*\\\"([A-Za-z0-9_]{3,16})\\\"");
 private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build(); private final ConcurrentMap<String,Cache> cache=new ConcurrentHashMap<>();
CompletableFuture<String> correctCase(String input){if(!input.matches("[A-Za-z0-9_]{3,16}"))return CompletableFuture.completedFuture(input);String key=input.toLowerCase(Locale.ROOT);Cache hit=cache.get(key);if(hit!=null&&hit.expiresAt>System.currentTimeMillis())return CompletableFuture.completedFuture(hit.name);HttpRequest request=HttpRequest.newBuilder(URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/name/"+input)).timeout(Duration.ofSeconds(5)).header("User-Agent","CredibleCrowd/1.4.5").GET().build();return client.sendAsync(request,HttpResponse.BodyHandlers.ofString()).handle((response,error)->{String resolved=input;if(error==null&&response.statusCode()==200){Matcher matcher=NAME.matcher(response.body());if(matcher.find())resolved=matcher.group(1);}cache.put(key,new Cache(resolved,System.currentTimeMillis()+Duration.ofHours(6).toMillis()));return resolved;});}
 private record Cache(String name,long expiresAt){}
}
