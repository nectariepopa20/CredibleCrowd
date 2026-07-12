package ro.nectariepopa.crediblecrowd.paper;

import com.comphenix.protocol.*; import com.comphenix.protocol.events.PacketContainer; import com.comphenix.protocol.wrappers.*; import java.nio.charset.StandardCharsets; import java.util.*; import org.bukkit.Bukkit; import org.bukkit.entity.Player;

final class FakeTabList {
 private final ProtocolManager protocol=ProtocolLibrary.getProtocolManager(); private final int latency;
 FakeTabList(int latency){this.latency=Math.max(0,latency);}
 void replace(Collection<String> oldNames,Collection<String> newNames){for(Player viewer:Bukkit.getOnlinePlayers()){remove(viewer,oldNames);add(viewer,newNames);}}
 void show(Player viewer,Collection<String> names){add(viewer,names);}
 void clear(Collection<String> names){for(Player viewer:Bukkit.getOnlinePlayers())remove(viewer,names);}
 private void add(Player viewer,Collection<String> names){if(names.isEmpty())return;PacketContainer packet=protocol.createPacket(PacketType.Play.Server.PLAYER_INFO);packet.getPlayerInfoActions().write(0,EnumSet.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER,EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE,EnumWrappers.PlayerInfoAction.UPDATE_LISTED,EnumWrappers.PlayerInfoAction.UPDATE_LATENCY,EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME));List<PlayerInfoData> data=names.stream().map(this::entry).toList();packet.getPlayerInfoDataLists().write(0,data);protocol.sendServerPacket(viewer,packet);}
 private void remove(Player viewer,Collection<String> names){if(names.isEmpty())return;PacketContainer packet=protocol.createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);packet.getUUIDLists().write(0,names.stream().map(FakeTabList::uuid).toList());protocol.sendServerPacket(viewer,packet);}
 private PlayerInfoData entry(String name){UUID id=uuid(name);WrappedGameProfile profile=new WrappedGameProfile(id,name);return new PlayerInfoData(id,latency,true,EnumWrappers.NativeGameMode.SURVIVAL,profile,WrappedChatComponent.fromText(name));}
 private static UUID uuid(String name){return UUID.nameUUIDFromBytes(("OfflinePlayer:"+name).getBytes(StandardCharsets.UTF_8));}
}
