//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.lumine.mythic.lib.api.stat.modifier;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.util.Closeable;
import org.apache.commons.lang.Validate;
import org.bukkit.scheduler.BukkitRunnable;

public class TemporaryStatModifier extends StatModifier implements Closeable {
    private BukkitRunnable closeTask;
    private long duration;
    private long startTime;

    public TemporaryStatModifier(String key, String stat, double value, ModifierType type, EquipmentSlot slot, ModifierSource source) {
        super(key, stat, value, type, slot, source);
    }

    public long getDuration() {
        Validate.isTrue(this.isActive(), "Modifier is not active");
        return this.duration;
    }

    public long getStartTime() {
        Validate.isTrue(this.isActive(), "Modifier is not active");
        return this.startTime;
    }

    public void register(final MMOPlayerData playerData, long duration) {
        Validate.isTrue(!this.isActive(), "Modifier is already active");
        super.register(playerData);
        this.closeTask = new BukkitRunnable() {
            public void run() {
                TemporaryStatModifier.this.unregister(playerData);
            }
        };
        this.closeTask.runTaskLater(MythicLib.plugin, duration);
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    public void register(final MMOPlayerData playerData, long duration, long startTime) {
        Validate.isTrue(!this.isActive(), "Modifier is already active");
        super.register(playerData);
        this.closeTask = new BukkitRunnable() {
            public void run() {
                TemporaryStatModifier.this.unregister(playerData);
            }
        };
        this.closeTask.runTaskLater(MythicLib.plugin, duration);
        this.duration = duration;
        this.startTime = startTime;
    }

    public void register(MMOPlayerData playerData) {
        throw new UnsupportedOperationException("Use #register(MMOPlayerData, long) instead");
    }

    public void close() {
        Validate.isTrue(this.isActive(), "Modifier is not active");
        this.closeTask.cancel();
        this.closeTask = null;
    }

    public boolean isActive() {
        return this.closeTask != null;
    }
}
