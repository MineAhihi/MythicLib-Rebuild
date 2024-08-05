package io.lumine.mythic.lib.data;

import com.mineahihi.mineahihiplugin.bukkit.config.annotation.Configuration;
import com.mineahihi.mineahihiplugin.bukkit.config.annotation.Key;
import com.mineahihi.mineahihiplugin.bukkit.config.annotation.Path;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configuration
@Getter
@Setter
@ToString
public class StatConfigData {
    @Key
    private String key;
    @Path
    private String stat;
    @Path
    private double value;
    @Path
    private ModifierType type;
    @Path
    private EquipmentSlot slot;
    @Path
    private ModifierSource source;
    @Path
    private long startTime;
    @Path
    private long duration;
}
