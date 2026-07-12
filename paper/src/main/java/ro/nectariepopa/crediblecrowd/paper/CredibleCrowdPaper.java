package ro.nectariepopa.crediblecrowd.paper;

import java.io.*; import java.util.*; import java.util.concurrent.atomic.AtomicReference; import org.bukkit.Bukkit; import org.bukkit.command.*; import org.bukkit.entity.Player; import org.bukkit.event.*; import org.bukkit.event.player.PlayerCommandPreprocessEvent; import org.bukkit.plugin.java.JavaPlugin; import org.bukkit.plugin.messaging.PluginMessageListener;

public final class CredibleCrowdPaper extends JavaPlugin implements PluginMessageListener,Listener,CommandExecutor {
 private static final String CHANNEL="crediblecrowd:sync"; private final AtomicReference<List<String>> fakeNames=new AtomicReference<>(List.of());
 @Override public void onEnable(){saveDefaultConfig();getServer().getMessenger().registerIncomingPluginChannel(this,CHANNEL,this);getServer().getPluginManager().registerEvents(this,this);Objects.requireNonNull(getCommand("ccplayers")).setExecutor(this);getLogger().info("Paper bridge ready; waiting for Velocity sync.");}
 @Override public void onDisable(){getServer().getMessenger().unregisterIncomingPluginChannel(this,CHANNEL,this);}
 @Override public void onPluginMessageReceived(String channel,Player player,byte[] message){if(!CHANNEL.equals(channel))return;try(var in=new DataInputStream(new ByteArrayInputStream(message))){if(in.readInt()!=1)return;int count=in.readInt();if(count<0||count>100000)throw new IOException("Invalid count");ArrayList<String> names=new ArrayList<>(count);for(int i=0;i<count;i++)names.add(in.readUTF());fakeNames.set(List.copyOf(names));}catch(IOException e){getLogger().warning("Rejected invalid Velocity sync payload: "+e.getMessage());}}
 @EventHandler(priority=EventPriority.HIGHEST) public void onPlayerCommand(PlayerCommandPreprocessEvent event){if(getConfig().getBoolean("intercept-list-command",true)&&isList(event.getMessage())){event.setCancelled(true);event.getPlayer().sendMessage(render());}}
 @EventHandler(priority=EventPriority.HIGHEST) public void onConsoleCommand(org.bukkit.event.server.ServerCommandEvent event){if(getConfig().getBoolean("intercept-list-command",true)&&isList(event.getCommand())){event.setCancelled(true);event.getSender().sendMessage(render());}}
 private boolean isList(String command){String c=command.startsWith("/")?command.substring(1):command;c=c.trim().toLowerCase(Locale.ROOT);return c.equals("list")||c.equals("minecraft:list");}
 private String render(){List<String> all=new ArrayList<>();Bukkit.getOnlinePlayers().forEach(p->all.add(p.getName()));all.addAll(fakeNames.get());return getConfig().getString("list-format","There are {count} of a max of {max} players online: {players}").replace("{count}",String.valueOf(all.size())).replace("{max}",String.valueOf(Bukkit.getMaxPlayers())).replace("{players}",String.join(", ",all));}
 @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){sender.sendMessage(render());return true;}
 public List<String> getVirtualPlayers(){return fakeNames.get();}
}
