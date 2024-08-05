//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.lumine.mythic.lib.api.stat.modifier;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.api.InstanceModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

public class StatModifier extends InstanceModifier {
    private final String stat;

    public StatModifier(String key, String stat, double value) {
        this(key, stat, value, ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER);
    }

    public StatModifier(String key, String stat, double value, ModifierType type) {
        this(key, stat, value, type, EquipmentSlot.OTHER, ModifierSource.OTHER);
    }

    public StatModifier(String key, String stat, double value, ModifierType type, EquipmentSlot slot, ModifierSource source) {
        super(key, slot, source, value, type);
        this.stat = stat;
    }

    public StatModifier(String key, String stat, String str) {
        super(key, EquipmentSlot.OTHER, ModifierSource.OTHER, str);
        this.stat = stat;
    }

    public StatModifier(ConfigObject object) {
        super(object);
        this.stat = object.getString("stat");
    }

    public String getStat() {
        return this.stat;
    }

    public StatModifier add(double offset) {
        return new StatModifier(this.getKey(), this.stat, this.value + offset, this.type, this.getSlot(), this.getSource());
    }

    public StatModifier multiply(double coef) {
        return new StatModifier(this.getKey(), this.stat, this.value * coef, this.type, this.getSlot(), this.getSource());
    }

    public void register(MMOPlayerData playerData) {
        playerData.getStatMap().getInstance(this.stat).addModifier(this);
    }

    public void unregister(MMOPlayerData playerData) {
        playerData.getStatMap().getInstance(this.stat).remove(this.getKey());
    }
}
