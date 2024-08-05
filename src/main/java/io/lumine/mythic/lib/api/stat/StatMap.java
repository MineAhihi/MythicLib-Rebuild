//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.lumine.mythic.lib.api.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.player.PlayerMetadata;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class StatMap implements StatProvider {
    private final MMOPlayerData data;
    private final Map<String, StatInstance> stats = new ConcurrentHashMap();

    public StatMap(MMOPlayerData player) {
        this.data = player;
    }

    public MMOPlayerData getPlayerData() {
        return this.data;
    }

    public double getStat(String stat) {
        return this.getInstance(stat).getTotal();
    }

    public @NotNull StatInstance getInstance(String id) {
        return (StatInstance)this.stats.computeIfAbsent(id, (stat) -> {
            return new StatInstance(this, stat);
        });
    }

    public boolean hasStat(String stat) {
        return this.stats.containsKey(stat);
    }

    public Collection<StatInstance> getInstances() {
        return this.stats.values();
    }

    /** @deprecated */
    @Deprecated
    public void updateAll() {
        MythicLib.plugin.getStats().runUpdates(this);
    }

    /** @deprecated */
    @Deprecated
    public void update(String stat) {
        StatHandler handler = MythicLib.plugin.getStats().getStatHandler(stat);
        if (handler != null) {
            handler.runUpdate(this.getInstance(stat));
        }

    }

    public PlayerMetadata cache(EquipmentSlot castHand) {
        return new PlayerMetadata(this, castHand);
    }
}
