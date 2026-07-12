package ro.nectariepopa.crediblecrowd.citizens;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/** Optional runtime-only bridge. No CustomJoinItems classes are linked at compile time. */
final class CustomJoinItemsBridge {
    private final Plugin source;
    private final Method itemFactory;
    private final CitizensSettings settings;
    private final Set<String> missingItemWarnings = new HashSet<>();

    private CustomJoinItemsBridge(Plugin source, Method itemFactory, CitizensSettings settings) {
        this.source = source; this.itemFactory = itemFactory; this.settings = settings;
    }

    static CustomJoinItemsBridge detect(CredibleCrowdCitizens plugin, CitizensSettings settings) {
        if (!settings.customJoinItemsEnabled()) return null;
        Plugin cji = Bukkit.getPluginManager().getPlugin("CustomJoinItems");
        if (cji == null || !cji.isEnabled()) {
            plugin.getLogger().info("CustomJoinItems was not detected; NPC hand items are disabled.");
            return null;
        }
        try {
            Class<?> api = Class.forName("com.gmail.filoghost.customjoinitems.CustomJoinItems", true, cji.getClass().getClassLoader());
            Method factory = api.getMethod("getItemStack", String.class);
            plugin.getLogger().info("CustomJoinItems detected; optional NPC hand items are enabled.");
            return new CustomJoinItemsBridge(cji, factory, settings);
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("CustomJoinItems is present but does not expose getItemStack(String). Update it to CustomJoinItems 2.1.1+; NPC hand items are disabled.");
            return null;
        }
    }

    void apply(NPC npc, UUID virtualId, boolean lobbyAfk) {
        if (!npc.isSpawned() || !(npc.getEntity() instanceof Player player)) return;
        String key = lobbyAfk ? settings.afkSpawnItemKey() : select(settings.wanderingItemKeys(), virtualId);
        if (key == null || key.isBlank()) return;
        ItemStack item = item(key);
        if (item == null) {
            if (missingItemWarnings.add(key)) {
                source.getLogger().warning("CustomJoinItems has no item named '" + key
                        + "'. Add that top-level key to plugins/CustomJoinItems/items.yml, then reload/restart CustomJoinItems.");
            }
            return;
        }
        // Citizens owns the visual equipment packet for player NPCs. Updating only
        // the Bukkit inventory does not reliably update what nearby players see.
        npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.HAND, item.clone());
        player.getInventory().setItemInMainHand(item);
        player.getInventory().setHeldItemSlot(0);
    }

    private String select(List<String> keys, UUID id) {
        if (keys.isEmpty()) return null;
        return keys.get(Math.floorMod(id.hashCode(), keys.size()));
    }

    private ItemStack item(String key) {
        try {
            Object value = itemFactory.invoke(null, key);
            return value instanceof ItemStack stack ? stack.clone() : null;
        } catch (ReflectiveOperationException exception) {
            source.getLogger().warning("Could not retrieve CustomJoinItems item '" + key + "' for an NPC.");
            return null;
        }
    }
}
