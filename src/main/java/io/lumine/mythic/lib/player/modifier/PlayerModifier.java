//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.lumine.mythic.lib.player.modifier;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public abstract class PlayerModifier {
    private final ModifierSource source;
    private final EquipmentSlot slot;
    private final String key;
    private final UUID uniqueId = UUID.randomUUID();
    private boolean custom;

    public PlayerModifier(String key, EquipmentSlot slot, ModifierSource source) {
        this.key = key;
        this.slot = slot;
        this.source = source;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public String getKey() {
        return this.key;
    }

    public EquipmentSlot getSlot() {
        return this.slot;
    }

    public ModifierSource getSource() {
        return this.source;
    }

    public abstract void register(MMOPlayerData var1);

    public abstract void unregister(MMOPlayerData var1);

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            PlayerModifier that = (PlayerModifier)o;
            return this.uniqueId.equals(that.uniqueId);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.uniqueId});
    }
}
