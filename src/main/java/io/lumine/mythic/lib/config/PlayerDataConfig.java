package io.lumine.mythic.lib.config;

import com.mineahihi.mineahihiplugin.bukkit.config.annotation.Configuration;
import com.mineahihi.mineahihiplugin.bukkit.config.annotation.Path;
import com.mineahihi.mineahihiplugin.bukkit.config.annotation.ValueType;
import io.lumine.mythic.lib.data.StatConfigData;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@Getter
@Setter
public class PlayerDataConfig {
    @Path("stats")
    @ValueType(StatConfigData.class)
    private Map<String, StatConfigData> statsMap = new LinkedHashMap<>();
}
