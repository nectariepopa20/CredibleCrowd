package ro.nectariepopa.crediblecrowd.citizens;

import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ro.nectariepopa.crediblecrowd.paper.CredibleCrowdPaper;

public final class CredibleCrowdCitizens extends JavaPlugin {
    private MaterializationManager manager;

    @Override public void onEnable() {
        saveDefaultConfig();
        if (!getConfig().getBoolean("enabled", true)) {
            getLogger().info("NPC materialization is disabled in config.yml.");
            return;
        }
        Plugin bridgePlugin = getServer().getPluginManager().getPlugin("CredibleCrowdPaper");
        if (!(bridgePlugin instanceof CredibleCrowdPaper bridge) || !bridgePlugin.isEnabled()) {
            getLogger().severe("CredibleCrowdPaper is not available; disabling Citizens module.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!CitizensAPI.hasImplementation()) {
            getLogger().severe("Citizens has not finished loading; disabling Citizens module.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        manager = new MaterializationManager(this, bridge, CitizensSettings.load(getConfig()));
        manager.start();
        getLogger().info("Citizens materialization enabled with proximity activation and configured safety caps.");
    }

    @Override public void onDisable() {
        if (manager != null) manager.stop();
    }
}
