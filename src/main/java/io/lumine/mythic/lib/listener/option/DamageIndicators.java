package io.lumine.mythic.lib.listener.option;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.AttackUnregisteredEvent;
import io.lumine.mythic.lib.api.event.IndicatorDisplayEvent;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.util.CustomFont;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import java.util.*;

public class DamageIndicators extends GameIndicators {
    private final String skillIcon;

    private final String weaponIcon;

    private final String skillIconCrit;

    private final String weaponIconCrit;

    private final boolean splitHolograms;

    private final boolean skillHologramEnable;

    private final boolean weaponHologramEnable;

    private final boolean skillCritHologramEnable;

    private final boolean weaponCritHologramEnable;

    
    private final CustomFont font;

    
    private final CustomFont fontCrit;

    public DamageIndicators(ConfigurationSection config) {
        super(config);
        this.skillIcon = config.getString("icon.skill.normal");
        this.weaponIcon = config.getString("icon.weapon.normal");
        this.skillIconCrit = config.getString("icon.skill.crit");
        this.weaponIconCrit = config.getString("icon.weapon.crit");
        this.skillHologramEnable = config.getBoolean("hologram.skill.normal");
        this.weaponHologramEnable = config.getBoolean("hologram.weapon.normal");
        this.skillCritHologramEnable = config.getBoolean("hologram.skill.crit");
        this.weaponCritHologramEnable = config.getBoolean("hologram.weapon.crit");
        this.splitHolograms = config.getBoolean("split-holograms");
        if (config.getBoolean("custom-font.enabled")) {
            this.font = new CustomFont(config.getConfigurationSection("custom-font.normal"));
            this.fontCrit = new CustomFont(config.getConfigurationSection("custom-font.crit"));
        } else {
            this.font = null;
            this.fontCrit = null;
        }
    }

    @EventHandler
    public void a(AttackUnregisteredEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (event.getDamage().getDamage() < 0.02D)
            return;
        if (livingEntity instanceof Player && UtilityMethods.isVanished((Player)livingEntity))
            return;
        List<String> holos = new ArrayList<>();
        Map<IndicatorType, Double> mappedDamage = mapDamage(event.getDamage());
        double modifierDue = (event.toBukkit().getFinalDamage() - event.getDamage().getDamage()) / Math.max(1, mappedDamage.size());
        mappedDamage.forEach((type, val) -> {
            if (type.isHologramEnable()) {
                holos.add(type.computeFormat(val.doubleValue() + modifierDue));
            }
        });

        if (holos.isEmpty())
            return;

        if (this.splitHolograms) {
            for (String holo : holos)
                displayIndicator((Entity)livingEntity, holo, getDirection(event.toBukkit()), IndicatorDisplayEvent.IndicatorType.DAMAGE);
        } else {
            String joined = String.join(" ", (Iterable)holos);
            displayIndicator((Entity)livingEntity, joined, getDirection(event.toBukkit()), IndicatorDisplayEvent.IndicatorType.DAMAGE);
        }
    }

    
    private Vector getDirection(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            Vector dir = event.getEntity().getLocation().toVector().subtract(((EntityDamageByEntityEvent)event).getDamager().getLocation().toVector()).setY(0);
            if (dir.lengthSquared() > 0.0D) {
                double d = Math.atan2(dir.getZ(), dir.getX());
                d += 1.5707963267948966D * (random.nextDouble() - 0.5D);
                return new Vector(Math.cos(d), 0.0D, Math.sin(d));
            }
        }
        double a = random.nextDouble() * Math.PI * 2.0D;
        return new Vector(Math.cos(a), 0.0D, Math.sin(a));
    }

    
    private Map<IndicatorType, Double> mapDamage(DamageMetadata damageMetadata) {
        Map<IndicatorType, Double> mapped = new HashMap<>();
        for (DamagePacket packet : damageMetadata.getPackets()) {
            IndicatorType type = new IndicatorType(damageMetadata, packet);
            mapped.put(type, Double.valueOf(((Double)mapped.getOrDefault(type, Double.valueOf(0.0D))).doubleValue() + packet.getFinalValue()));
        }
        return mapped;
    }

    private class IndicatorType {
        final boolean physical;

        
        final Element element;

        final boolean crit;

        IndicatorType(DamageMetadata damageMetadata, DamagePacket packet) {
            this.physical = packet.hasType(DamageType.PHYSICAL);
            this.element = packet.getElement();
            this.crit = ((this.physical ? damageMetadata.isWeaponCriticalStrike() : damageMetadata.isSkillCriticalStrike()) || (this.element != null && damageMetadata.isElementalCriticalStrike(this.element)));
        }

        
        private String computeIcon() {
            StringBuilder build = new StringBuilder();
            if (this.physical) {
                build.append(this.crit ? DamageIndicators.this.weaponIconCrit : DamageIndicators.this.weaponIcon);
            } else {
                build.append(this.crit ? DamageIndicators.this.skillIconCrit : DamageIndicators.this.skillIcon);
            }
            if (this.element != null)
                build.append(this.element.getColor() + this.element.getLoreIcon());
            return build.toString();
        }

        public boolean isHologramEnable() {
            if (this.physical) {
                return this.crit ? DamageIndicators.this.weaponCritHologramEnable : DamageIndicators.this.weaponHologramEnable;
            }else {
                return this.crit ? DamageIndicators.this.skillCritHologramEnable : DamageIndicators.this.skillHologramEnable;
            }
        }
        
        private String computeFormat(double damage) {
            CustomFont indicatorFont = (this.crit && DamageIndicators.this.fontCrit != null) ? DamageIndicators.this.fontCrit : DamageIndicators.this.font;
            String formattedDamage = (indicatorFont == null) ? DamageIndicators.this.formatNumber(damage) : indicatorFont.format(DamageIndicators.this.formatNumber(damage));
            return MythicLib.plugin.getPlaceholderParser().parse(null, DamageIndicators.this.getRaw().replace("{icon}", computeIcon()).replace("{value}", formattedDamage));
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            IndicatorType that = (IndicatorType)o;
            return (this.physical == that.physical && Objects.equals(this.element, that.element));
        }

        public int hashCode() {
            return Objects.hash(new Object[] { Boolean.valueOf(this.physical), this.element });
        }
    }
}
