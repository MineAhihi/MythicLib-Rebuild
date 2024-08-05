//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.gui.PluginInventory;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    public PlayerListener() {
    }

    @EventHandler(
        priority = EventPriority.LOWEST
    )
    public void loadData(PlayerJoinEvent event) {
        MMOPlayerData data = MMOPlayerData.setup(event.getPlayer());

        Bukkit.getScheduler().runTaskLater(MythicLib.plugin, () -> {
            MythicLib.plugin.getStats().runUpdates(data.getStatMap());
        }, 1L);
    }

    @EventHandler(
        priority = EventPriority.MONITOR
    )
    public void registerOfflinePlayers(PlayerQuitEvent event) {
        MMOPlayerData data = MMOPlayerData.get(event.getPlayer());
        data.updatePlayer(null);
    }

    @EventHandler
    public void handleCustomInventoryClicks(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof PluginInventory) {
            ((PluginInventory)event.getInventory().getHolder()).whenClicked(event);
        }

    }
}
