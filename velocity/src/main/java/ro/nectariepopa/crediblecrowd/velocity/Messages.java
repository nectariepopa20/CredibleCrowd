package ro.nectariepopa.crediblecrowd.velocity;

import java.io.*; import java.nio.charset.StandardCharsets; import java.nio.file.*; import java.util.*; import net.kyori.adventure.text.Component; import net.kyori.adventure.text.minimessage.MiniMessage; import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder; import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver; import org.yaml.snakeyaml.Yaml;

final class Messages {
 private final Map<String,String> values; private final MiniMessage mini=MiniMessage.miniMessage();
 private Messages(Map<String,String> values){this.values=Map.copyOf(values);}
 static Messages load(Path file)throws IOException{try(Reader reader=Files.newBufferedReader(file,StandardCharsets.UTF_8)){Map<String,Object> raw=new Yaml().load(reader);Map<String,String> values=new HashMap<>();raw.forEach((k,v)->values.put(k,String.valueOf(v)));return new Messages(values);}}
 Component get(String key,Object... replacements){String template=values.getOrDefault(key,"<red>Missing message: "+key);List<TagResolver> tags=new ArrayList<>();for(int i=0;i+1<replacements.length;i+=2)tags.add(Placeholder.unparsed(String.valueOf(replacements[i]),String.valueOf(replacements[i+1])));return mini.deserialize(template,TagResolver.resolver(tags));}
}

