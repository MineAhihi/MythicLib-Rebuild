//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.lumine.mythic.lib;

import io.lumine.mythic.lib.api.crafting.recipes.MythicCraftingManager;
import io.lumine.mythic.lib.api.crafting.recipes.vmp.MegaWorkbenchMapping;
import io.lumine.mythic.lib.api.crafting.recipes.vmp.SuperWorkbenchMapping;
import io.lumine.mythic.lib.api.crafting.uifilters.MythicItemUIFilter;
import io.lumine.mythic.lib.api.event.armorequip.ArmorEquipEvent;
import io.lumine.mythic.lib.api.placeholders.MythicPlaceholders;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.ExploreAttributesCommand;
import io.lumine.mythic.lib.command.HealthScaleCommand;
import io.lumine.mythic.lib.command.MMOTempStatCommand;
import io.lumine.mythic.lib.command.mythiclib.MythicLibCommand;
import io.lumine.mythic.lib.comp.McMMOAttackHandler;
import io.lumine.mythic.lib.comp.SkillAPIAttackHandler;
import io.lumine.mythic.lib.comp.adventure.AdventureParser;
import io.lumine.mythic.lib.comp.anticheat.AntiCheatSupport;
import io.lumine.mythic.lib.comp.anticheat.SpartanPlugin;
import io.lumine.mythic.lib.comp.dualwield.DualWieldHook;
import io.lumine.mythic.lib.comp.dualwield.RealDualWieldHook;
import io.lumine.mythic.lib.comp.flags.FlagHandler;
import io.lumine.mythic.lib.comp.flags.FlagPlugin;
import io.lumine.mythic.lib.comp.flags.ResidenceFlags;
import io.lumine.mythic.lib.comp.flags.WorldGuardFlags;
import io.lumine.mythic.lib.comp.formula.FormulaParser;
import io.lumine.mythic.lib.comp.mythicmobs.MythicMobsAttackHandler;
import io.lumine.mythic.lib.comp.mythicmobs.MythicMobsHook;
import io.lumine.mythic.lib.comp.placeholder.*;
import io.lumine.mythic.lib.comp.profile.ProfilePluginListener;
import io.lumine.mythic.lib.comp.protocollib.DamageParticleCap;
import io.lumine.mythic.lib.glow.GlowModule;
import io.lumine.mythic.lib.glow.provided.MythicGlowModule;
import io.lumine.mythic.lib.gson.Gson;
import io.lumine.mythic.lib.gui.PluginInventory;
import io.lumine.mythic.lib.hologram.HologramFactory;
import io.lumine.mythic.lib.hologram.HologramFactoryList;
import io.lumine.mythic.lib.hologram.factory.BukkitHologramFactory;
import io.lumine.mythic.lib.listener.*;
import io.lumine.mythic.lib.listener.event.AttackEventListener;
import io.lumine.mythic.lib.listener.option.FixMovementSpeed;
import io.lumine.mythic.lib.listener.option.HealthScale;
import io.lumine.mythic.lib.manager.*;
import io.lumine.mythic.lib.metrics.bukkit.Metrics;
import io.lumine.mythic.lib.util.gson.MythicLibGson;
import io.lumine.mythic.lib.util.loadingorder.DependencyCycleCheck;
import io.lumine.mythic.lib.util.loadingorder.DependencyNode;
import io.lumine.mythic.lib.util.network.MythicPacketSniffer;
import io.lumine.mythic.lib.version.ServerVersion;
import io.lumine.mythic.lib.version.SpigotPlugin;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class MythicLib extends JavaPlugin {
    public static MythicLib plugin;
    private final DamageManager damageManager = new DamageManager();
    private final EntityManager entityManager = new EntityManager();
    private final StatManager statManager = new StatManager();
    private final JsonManager jsonManager = new JsonManager();
    private final ConfigManager configManager = new ConfigManager();
    private final ElementManager elementManager = new ElementManager();
    private final SkillManager skillManager = new SkillManager();
    private final ModifierManager modifierManager = new ModifierManager();
    private final FlagHandler flagHandler = new FlagHandler();
    private final IndicatorManager indicatorManager = new IndicatorManager();
    private final FormulaParser formulaParser = new FormulaParser();
    private Gson gson;
    private AntiCheatSupport antiCheatSupport;
    private ServerVersion version;
    private AttackEffects attackEffects;
    private MitigationMechanics mitigationMechanics;
    private AdventureParser adventureParser;
    private PlaceholderParser placeholderParser;
    private GlowModule glowModule;
    private @Nullable Boolean hasProfiles;

    public MythicLib() {
    }

    public void onLoad() {
        plugin = this;
        this.getLogger().log(Level.INFO, "Plugin file is called '" + this.getFile().getName() + "'");

        try {
            this.version = new ServerVersion(Bukkit.getServer().getClass());
            this.getLogger().log(Level.INFO, "Detected Bukkit Version: " + this.version.toString());
        } catch (Exception var2) {
            this.getLogger().log(Level.INFO, ChatColor.RED + "Your server version is not compatible.");
            var2.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            this.flagHandler.registerPlugin(new WorldGuardFlags());
            this.getLogger().log(Level.INFO, "Hooked onto WorldGuard");
        }

        this.adventureParser = new AdventureParser();
    }

    public void onEnable() {
        new Metrics(this);
        this.gson = MythicLibGson.build();
        (new SpigotPlugin(90306, this)).checkForUpdate();
        this.saveDefaultConfig();
        int configVersion = this.getConfig().contains("config-version", true) ? this.getConfig().getInt("config-version") : -1;
        int defConfigVersion = this.getConfig().getDefaults().getInt("config-version");
        if (configVersion != defConfigVersion) {
            this.getLogger().warning("You may be using an outdated config.yml!");
            this.getLogger().warning("(Your config version: '" + configVersion + "' | Expected config version: '" + defConfigVersion + "')");
        }

        new MythicPacketSniffer(this);
        Bukkit.getServicesManager().register(HologramFactory.class, new BukkitHologramFactory(), this, ServicePriority.Low);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(this.damageManager, this);
        Bukkit.getPluginManager().registerEvents(new DamageReduction(), this);
        Bukkit.getPluginManager().registerEvents(this.attackEffects = new AttackEffects(), this);
        Bukkit.getPluginManager().registerEvents(this.mitigationMechanics = new MitigationMechanics(), this);
        Bukkit.getPluginManager().registerEvents(new AttackEventListener(), this);
        Bukkit.getPluginManager().registerEvents(new MythicCraftingManager(), this);
        Bukkit.getPluginManager().registerEvents(new SkillTriggers(), this);
        Bukkit.getPluginManager().registerEvents(new ElementalDamage(), this);
        Bukkit.getPluginManager().registerEvents(new PvpListener(), this);
        ArmorEquipEvent.registerListener(this);
        if (this.getConfig().getBoolean("health-scale.enabled")) {
            Bukkit.getPluginManager().registerEvents(new HealthScale(this.getConfig().getDouble("health-scale.scale"), this.getConfig().getInt("health-scale.delay", 0)), this);
        }

        if (this.getConfig().getBoolean("fix-movement-speed")) {
            Bukkit.getPluginManager().registerEvents(new FixMovementSpeed(), this);
        }

        HologramFactoryList[] var3 = HologramFactoryList.values();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            HologramFactoryList custom = var3[var5];
            if (custom.isInstalled(this.getServer().getPluginManager())) {
                try {
                    Bukkit.getServicesManager().register(HologramFactory.class, custom.generateFactory(), this, custom.getServicePriority());
                    this.getLogger().log(Level.INFO, "Hooked onto " + custom.getPluginName());
                } catch (Exception var8) {
                    this.getLogger().log(Level.WARNING, "Could not hook onto " + custom.getPluginName() + ": " + var8.getMessage());
                }
            }
        }

        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            this.damageManager.registerHandler(new MythicMobsAttackHandler());
            Bukkit.getPluginManager().registerEvents(new MythicMobsHook(), this);
            MythicItemUIFilter.register();
            this.getLogger().log(Level.INFO, "Hooked onto MythicMobs");
        }

        if (Bukkit.getPluginManager().getPlugin("Residence") != null) {
            this.flagHandler.registerPlugin(new ResidenceFlags());
            this.getLogger().log(Level.INFO, "Hooked onto Residence");
        }

        if (Bukkit.getPluginManager().getPlugin("Spartan") != null) {
            this.antiCheatSupport = new SpartanPlugin();
            this.getLogger().log(Level.INFO, "Hooked onto Spartan");
        }

        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            if (this.getConfig().getBoolean("damage-particles-cap.enabled")) {
                new DamageParticleCap(this.getConfig().getInt("damage-particles-cap.max-per-tick"));
            }

            this.getLogger().log(Level.INFO, "Hooked onto ProtocolLib");
        }

        if (Bukkit.getPluginManager().getPlugin("mcMMO") != null) {
            Bukkit.getPluginManager().registerEvents(new McMMOAttackHandler(), this);
            this.getLogger().log(Level.INFO, "Hooked onto mcMMO");
        }

        if (Bukkit.getPluginManager().getPlugin("SkillAPI") != null) {
            Bukkit.getPluginManager().registerEvents(new SkillAPIAttackHandler(), this);
            this.getLogger().log(Level.INFO, "Hooked onto SkillAPI");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            MythicPlaceholders.registerPlaceholder(new MythicPlaceholderAPIHook());
            (new PlaceholderAPIHook()).register();
            this.placeholderParser = new PlaceholderAPIParser();
            this.getLogger().log(Level.INFO, "Hooked onto PlaceholderAPI");
        } else {
            this.placeholderParser = new DefaultPlaceholderParser();
        }

        if (Bukkit.getPluginManager().getPlugin("RealDualWield") != null) {
            Bukkit.getPluginManager().registerEvents(new RealDualWieldHook(), this);
            this.getLogger().log(Level.INFO, "Hooked onto RealDualWield");
        }

        if (Bukkit.getPluginManager().getPlugin("DualWield") != null) {
            Bukkit.getPluginManager().registerEvents(new DualWieldHook(), this);
            this.getLogger().log(Level.INFO, "Hooked onto DualWield");
        }

        Stack<DependencyNode> dependencyCycle = (new DependencyCycleCheck()).checkCycle();
        if (dependencyCycle != null) {
            this.getLogger().log(Level.WARNING, "Found a dependency cycle! Please make sure that the plugins involved load with no errors.");
            this.getLogger().log(Level.WARNING, "Plugin dependency cycle: " + dependencyCycle);
        }

        this.indicatorManager.load(this.getConfig());
        if (this.hasProfiles == null) {
            this.hasProfiles = false;
        } else if (this.hasProfiles) {
            Bukkit.getPluginManager().registerEvents(new ProfilePluginListener(), this);
            this.getLogger().log(Level.INFO, "Hooked onto ProfileAPI");
        }

        if (this.glowModule == null) {
            this.glowModule = new MythicGlowModule();
            this.glowModule.enable();
        }

        this.getCommand("exploreattributes").setExecutor(new ExploreAttributesCommand());
        this.getCommand("mythiclib").setExecutor(new MythicLibCommand());
        this.getCommand("mmotempstat").setExecutor(new MMOTempStatCommand());
        this.getCommand("healthscale").setExecutor(new HealthScaleCommand());
        this.getCommand("superworkbench").setExecutor(SuperWorkbenchMapping.SWB);
        Bukkit.getPluginManager().registerEvents(SuperWorkbenchMapping.SWB, this);
        this.getCommand("megaworkbench").setExecutor(MegaWorkbenchMapping.MWB);
        Bukkit.getPluginManager().registerEvents(MegaWorkbenchMapping.MWB, this);
        this.skillManager.initialize(false);
        this.elementManager.reload(false);
        Bukkit.getOnlinePlayers().forEach(MMOPlayerData::setup);
        Bukkit.getScheduler().runTaskTimer(this, MMOPlayerData::flushOfflinePlayerData, 72000L, 72000L);
        this.configManager.reload();
        this.statManager.initialize(false);

        setupTask();
    }

    public void setupTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (MMOPlayerData mmoPlayerData : MMOPlayerData.getPlayerDataMap().values()) {
                mmoPlayerData.savePlayerData();
            }
        }, 20 * 60 * 5, 20 * 60 * 5);
    }

    public void reload() {
        this.reloadConfig();
        this.statManager.initialize(true);
        this.attackEffects.reload();
        this.mitigationMechanics.reload();
        this.skillManager.initialize(true);
        this.configManager.reload();
        this.elementManager.reload(true);
        this.indicatorManager.reload(this.getConfig());
    }

    public void onDisable() {
        Iterator var1 = Bukkit.getOnlinePlayers().iterator();

        while(var1.hasNext()) {
            Player player = (Player)var1.next();
            if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().getHolder() != null && player.getOpenInventory().getTopInventory().getHolder() instanceof PluginInventory) {
                player.closeInventory();
            }
        }

        for (MMOPlayerData mmoPlayerData : MMOPlayerData.getPlayerDataMap().values()) {
            mmoPlayerData.savePlayerData();
        }

        this.glowModule.disable();
    }

    public static MythicLib inst() {
        return plugin;
    }

    public Gson getGson() {
        return this.gson;
    }

    public ServerVersion getVersion() {
        return this.version;
    }

    /** @deprecated */
    @Deprecated
    public JsonManager getJson() {
        return this.jsonManager;
    }

    public DamageManager getDamage() {
        return this.damageManager;
    }

    public EntityManager getEntities() {
        return this.entityManager;
    }

    public SkillManager getSkills() {
        return this.skillManager;
    }

    public ModifierManager getModifiers() {
        return this.modifierManager;
    }

    public ElementManager getElements() {
        return this.elementManager;
    }

    public StatManager getStats() {
        return this.statManager;
    }

    public ConfigManager getMMOConfig() {
        return this.configManager;
    }

    public FlagHandler getFlags() {
        return this.flagHandler;
    }

    public PlaceholderParser getPlaceholderParser() {
        return this.placeholderParser;
    }

    public AttackEffects getAttackEffects() {
        return this.attackEffects;
    }

    public AntiCheatSupport getAntiCheat() {
        return this.antiCheatSupport;
    }

    public FormulaParser getFormulaParser() {
        return this.formulaParser;
    }

    public @Nullable GlowModule getGlowing() {
        return this.glowModule;
    }

    public void enableProfiles() {
        Validate.isTrue(this.hasProfiles == null, "Profiles have already been enabled/disabled");
        this.hasProfiles = true;
    }

    public boolean hasProfiles() {
        return this.hasProfiles;
    }

    /** @deprecated */
    @Deprecated
    public void handleFlags(FlagPlugin flagPlugin) {
        this.getFlags().registerPlugin(flagPlugin);
    }

    public boolean hasAntiCheat() {
        return this.antiCheatSupport != null;
    }

    public String parseColors(String format) {
        return this.adventureParser.parse(format);
    }

    public List<String> parseColors(String... format) {
        return this.parseColors(Arrays.asList(format));
    }

    public List<String> parseColors(List<String> format) {
        return new ArrayList(this.adventureParser.parse(format));
    }

    public AdventureParser getAdventureParser() {
        return this.adventureParser;
    }

    public File getJarFile() {
        return plugin.getFile();
    }
}
