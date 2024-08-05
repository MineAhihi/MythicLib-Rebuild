//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.lumine.mythic.lib.api.player;

import com.mineahihi.mineahihiplugin.bukkit.config.AdvancedFileConfiguration;
import com.mineahihi.mineahihiplugin.bukkit.utils.FileUtils;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.addon.MythicLibAddon;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.api.stat.modifier.TemporaryStatModifier;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.config.PlayerDataConfig;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.data.StatConfigData;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;
import io.lumine.mythic.lib.player.cooldown.CooldownType;
import io.lumine.mythic.lib.player.particle.ParticleEffectMap;
import io.lumine.mythic.lib.player.potion.PermanentPotionEffectMap;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.player.skill.PassiveSkillMap;
import io.lumine.mythic.lib.player.skillmod.SkillModifierMap;
import io.lumine.mythic.lib.script.variable.VariableList;
import io.lumine.mythic.lib.script.variable.VariableScope;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import org.apache.commons.lang.Validate;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class MMOPlayerData {
    private final UUID playerId;
    private UUID profileId;
    private @Nullable Player player;
    private long lastLogActivity;
    private final CooldownMap cooldownMap = new CooldownMap();
    private final StatMap statMap = new StatMap(this);
    private final SkillModifierMap skillModifierMap = new SkillModifierMap(this);
    private final PermanentPotionEffectMap permEffectMap = new PermanentPotionEffectMap(this);
    private final ParticleEffectMap particleEffectMap = new ParticleEffectMap(this);
    private final PassiveSkillMap passiveSkillMap = new PassiveSkillMap(this);
    private final VariableList variableList;
    private final Map<String, Object> externalData;
    private static final long CACHE_TIME_OUT = 86400000L;
    private static final Map<UUID, MMOPlayerData> PLAYER_DATA = new WeakHashMap();
    private String playerName;
    private AdvancedFileConfiguration playerDataFileConfig;
    private PlayerDataConfig playerDataConfig;

    public static Map<UUID, MMOPlayerData> getPlayerDataMap() {
        return new HashMap<>(PLAYER_DATA);
    }

    private MMOPlayerData(@NotNull Player player) {
        this.variableList = new VariableList(VariableScope.PLAYER);
        this.externalData = new HashMap();
        this.playerId = player.getUniqueId();
        this.player = player;
        this.playerName = player.getName();
    }

    public MMOPlayerData(@NotNull UUID playerId) {
        this.variableList = new VariableList(VariableScope.PLAYER);
        this.externalData = new HashMap();
        this.playerId = playerId;
        this.setProfileId(playerId);
    }

    /**
     * 1.6.1.1-Rebuild
     */
    public void loadPlayerData() {
        File playerDataFile = MythicLibAddon.getPlayerFile(playerName);
        FileUtils.createFile(playerDataFile, true);

        this.playerDataFileConfig = AdvancedFileConfiguration.of(playerDataFile);
        this.playerDataConfig = playerDataFileConfig.get(PlayerDataConfig.class);

        for (StatConfigData statConfigData : playerDataConfig.getStatsMap().values()) {
            try {
                if (statConfigData.getDuration() != 0) {
                    TemporaryStatModifier temporaryStatModifier = new TemporaryStatModifier(statConfigData.getKey(), statConfigData.getStat(), statConfigData.getValue(), statConfigData.getType(), statConfigData.getSlot(), statConfigData.getSource());
                    temporaryStatModifier.setCustom(true);
                    temporaryStatModifier.register(this, statConfigData.getDuration(), statConfigData.getStartTime());
                } else {
                    StatModifier statModifier = new StatModifier(statConfigData.getKey(), statConfigData.getStat(), statConfigData.getValue(), statConfigData.getType(), statConfigData.getSlot(), statConfigData.getSource());
                    statModifier.setCustom(true);
                    statModifier.register(this);
                }
            }catch (Exception e) {
                MythicLib.plugin.getLogger().warning("Failed to load stat modifier: " + statConfigData.getKey() + " for player: " + playerName);
            }
        }
    }

    /**
     * 1.6.1.1-Rebuild
     */
    public void savePlayerData() {
        Map<String, StatConfigData> statsMap = new LinkedHashMap<>();

        for (StatInstance statInstance : getStatMap ().getInstances()) {
            for (StatModifier statModifier : statInstance.getModifiers()) {
                if (statModifier.isCustom() && statModifier.getValue() != 0.0D) {
                    StatConfigData statConfigData = new StatConfigData();
                    statConfigData.setKey(statModifier.getKey());
                    statConfigData.setStat(statModifier.getStat());
                    statConfigData.setValue(statModifier.getValue());
                    statConfigData.setType(statModifier.getType());
                    statConfigData.setSlot(statModifier.getSlot());
                    statConfigData.setSource(statModifier.getSource());

                    if (statModifier instanceof TemporaryStatModifier) {
                        statConfigData.setDuration(((TemporaryStatModifier) statModifier).getDuration());
                        statConfigData.setStartTime(((TemporaryStatModifier) statModifier).getStartTime());
                    }

                    statsMap.put(statModifier.getKey(), statConfigData);
                }
            }
        }

        playerDataConfig.setStatsMap(statsMap);
        playerDataFileConfig.set(playerDataConfig);
        playerDataFileConfig.save();
    }

    public @NotNull UUID getUniqueId() {
        return this.playerId;
    }

    public @NotNull UUID getProfileId() {
        return MythicLib.plugin.hasProfiles() ? (UUID)Objects.requireNonNull(this.profileId, "No profile has been chosen yet") : this.playerId;
    }

    public void setProfileId(@NotNull UUID profileId) {
        this.profileId = (UUID)Objects.requireNonNull(profileId, "Profile ID cannot be null");
    }

    public StatMap getStatMap() {
        return this.statMap;
    }

    public SkillModifierMap getSkillModifierMap() {
        return this.skillModifierMap;
    }

    /** @deprecated */
    @Deprecated
    public PermanentPotionEffectMap getPermanentEffectMap() {
        return this.permEffectMap;
    }

    /** @deprecated */
    @Deprecated
    public ParticleEffectMap getParticleEffectMap() {
        return this.particleEffectMap;
    }

    public PassiveSkillMap getPassiveSkillMap() {
        return this.passiveSkillMap;
    }

    public void triggerSkills(@NotNull TriggerType triggerType, @Nullable Entity target) {
        Validate.isTrue(!triggerType.isActionHandSpecific(), "You must provide an action hand");
        this.triggerSkills(triggerType, EquipmentSlot.MAIN_HAND, target);
    }

    public void triggerSkills(@NotNull TriggerType triggerType, @NotNull EquipmentSlot actionHand, @Nullable Entity target) {
        Validate.notNull(actionHand, "Action hand cannot be null");
        this.triggerSkills(triggerType, this.statMap.cache(actionHand), target);
    }

    /** @deprecated */
    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @NotNull PlayerMetadata caster, @Nullable AttackMetadata attackMetadata, @Nullable Entity target) {
        this.triggerSkills(triggerType, caster, target, attackMetadata);
    }

    public void triggerSkills(@NotNull TriggerType triggerType, @NotNull PlayerMetadata caster, @Nullable Entity target, @Nullable AttackMetadata attackMetadata) {
        Iterable<PassiveSkill> cast = triggerType.isActionHandSpecific() ? this.passiveSkillMap.isolateModifiers(caster.getActionHand()) : this.passiveSkillMap.getModifiers();
        this.triggerSkills(triggerType, caster, (Iterable)cast, target, attackMetadata);
    }

    public void triggerSkills(@NotNull TriggerType triggerType, @NotNull PlayerMetadata caster, @Nullable Entity target) {
        Iterable<PassiveSkill> cast = triggerType.isActionHandSpecific() ? this.passiveSkillMap.isolateModifiers(caster.getActionHand()) : this.passiveSkillMap.getModifiers();
        this.triggerSkills(triggerType, caster, (Iterable)cast, target, (AttackMetadata)null);
    }

    public void triggerSkills(@NotNull TriggerType triggerType, @NotNull PlayerMetadata caster, @NotNull Iterable<PassiveSkill> skills, @Nullable Entity target) {
        this.triggerSkills(triggerType, caster, skills, target, (AttackMetadata)null);
    }

    public void triggerSkills(@NotNull TriggerType triggerType, @NotNull PlayerMetadata caster, @NotNull Iterable<PassiveSkill> skills, @Nullable Entity target, @Nullable AttackMetadata attack) {
        if (this.getPlayer().getGameMode() != GameMode.SPECTATOR && MythicLib.plugin.getFlags().isFlagAllowed(this.getPlayer(), CustomFlag.MMO_ABILITIES)) {
            TriggerMetadata triggerMeta = new TriggerMetadata(caster, target, attack);
            Iterator var7 = skills.iterator();

            while(var7.hasNext()) {
                PassiveSkill skill = (PassiveSkill)var7.next();
                SkillHandler handler = skill.getTriggeredSkill().getHandler();
                if (handler.isTriggerable() && skill.getType().equals(triggerType)) {
                    skill.getTriggeredSkill().cast(triggerMeta);
                }
            }

        }
    }

    public VariableList getVariableList() {
        return this.variableList;
    }

    /** @deprecated */
    @Deprecated
    public long getLastLogin() {
        return this.getLastLogActivity();
    }

    public long getLastLogActivity() {
        return this.lastLogActivity;
    }

    public boolean isTimedOut() {
        return !this.isOnline() && this.lastLogActivity + 86400000L < System.currentTimeMillis();
    }

    public boolean isOnline() {
        return this.player != null;
    }

    public @NotNull Player getPlayer() {
        return (Player)Objects.requireNonNull(this.player, "Player is offline");
    }

    public void updatePlayer(@Nullable Player player) {
        this.player = player;
        this.lastLogActivity = System.currentTimeMillis();
    }

    public void applyCooldown(CooldownType cd, double value) {
        this.cooldownMap.applyCooldown(cd.name(), value);
    }

    public boolean isOnCooldown(CooldownType cd) {
        return this.cooldownMap.isOnCooldown(cd.name());
    }

    public CooldownMap getCooldownMap() {
        return this.cooldownMap;
    }

    @Nullable
    public <T> T getExternalData(String key, Class<T> objectType) {
        Object found = this.externalData.get(key);
        return found == null ? null : (T) found;
    }

    public void setExternalData(String key, Object obj) {
        this.externalData.put(key, obj);
    }

    public boolean hasExternalData(String key) {
        return this.externalData.containsKey(key);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof MMOPlayerData)) {
            return false;
        } else {
            MMOPlayerData that = (MMOPlayerData)o;
            return this.getUniqueId().equals(that.getUniqueId());
        }
    }

    public int hashCode() {
        return this.getUniqueId().hashCode();
    }

    public static MMOPlayerData setup(@NotNull Player player) {
        MMOPlayerData found = (MMOPlayerData)PLAYER_DATA.get(player.getUniqueId());
        if (found == null) {
            MMOPlayerData playerData = new MMOPlayerData(player);
            playerData.loadPlayerData();

            PLAYER_DATA.put(player.getUniqueId(), playerData);
            return playerData;
        } else {
            found.updatePlayer(player);
            return found;
        }
    }

    /** @deprecated */
    @Deprecated
    public static boolean isLoaded(UUID uuid) {
        return has(uuid);
    }

    public static @NotNull MMOPlayerData get(@NotNull OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    public static @NotNull MMOPlayerData get(@NotNull UUID uuid) {
        return (MMOPlayerData)Objects.requireNonNull((MMOPlayerData)PLAYER_DATA.get(uuid), "Player data not loaded");
    }

    public static @Nullable MMOPlayerData getOrNull(@NotNull OfflinePlayer player) {
        return getOrNull(player.getUniqueId());
    }

    public static @Nullable MMOPlayerData getOrNull(@NotNull UUID uuid) {
        return (MMOPlayerData)PLAYER_DATA.get(uuid);
    }

    public static boolean has(@NotNull OfflinePlayer player) {
        return has(player.getUniqueId());
    }

    public static boolean has(@NotNull UUID uuid) {
        return PLAYER_DATA.containsKey(uuid);
    }

    public static Collection<MMOPlayerData> getLoaded() {
        return PLAYER_DATA.values();
    }

    public static void forEachOnline(Consumer<MMOPlayerData> action) {
        Iterator var1 = PLAYER_DATA.values().iterator();

        while(var1.hasNext()) {
            MMOPlayerData registered = (MMOPlayerData)var1.next();
            if (registered.isOnline()) {
                action.accept(registered);
            }
        }

    }

    public static void flushOfflinePlayerData() {
        Iterator<MMOPlayerData> iterator = PLAYER_DATA.values().iterator();

        while(iterator.hasNext()) {
            MMOPlayerData tempData = (MMOPlayerData)iterator.next();
            if (tempData.isTimedOut()) {
                iterator.remove();
            }
        }

    }
}
