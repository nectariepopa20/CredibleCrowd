package ro.nectariepopa.crediblecrowd.paper;

import me.clip.placeholderapi.expansion.PlaceholderExpansion; import org.bukkit.OfflinePlayer; import org.jetbrains.annotations.NotNull; import org.jetbrains.annotations.Nullable;

final class BungeeExpansion extends PlaceholderExpansion {
 private final CredibleCrowdPaper plugin; BungeeExpansion(CredibleCrowdPaper plugin){this.plugin=plugin;}
 @Override public @NotNull String getIdentifier(){return "bungee";}
 @Override public @NotNull String getAuthor(){return "nectariepopa20";}
 @Override public @NotNull String getVersion(){return plugin.getPluginMeta().getVersion();}
 @Override public boolean persist(){return true;}
 @Override public @Nullable String onRequest(OfflinePlayer player,@NotNull String params){return switch(params.toLowerCase(java.util.Locale.ROOT)){case "total"->String.valueOf(plugin.getNetworkTotal());case "server"->String.valueOf(plugin.getLocalTotal());default->null;};}
}

