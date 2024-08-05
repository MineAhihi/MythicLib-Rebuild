//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.cooldown.CooldownType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Random;

public class AttackEffects implements Listener {
    private double weaponCritCooldown;
    private double skillCritCooldown;
    private static final Random RANDOM = new Random();

    public AttackEffects() {
        this.reload();
    }

    public void reload() {
        this.weaponCritCooldown = MythicLib.plugin.getConfig().getDouble("critical-strikes.weapon.cooldown", 3.0);
        this.skillCritCooldown = MythicLib.plugin.getConfig().getDouble("critical-strikes.skill.cooldown", 3.0);
    }

    @EventHandler(
        priority = EventPriority.LOW,
        ignoreCancelled = true
    )
    public void onHitAttackEffects(PlayerAttackEvent event) {
        PlayerMetadata stats = event.getAttacker();
        DamageType[] var3 = DamageType.values();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            DamageType type = var3[var5];
            event.getDamage().additiveModifier(stats.getStat(type.getOffenseStat()) / 100.0, type);
        }

        if (MythicLib.plugin.getVersion().getWrapper().isUndead(event.getEntity())) {
            event.getDamage().additiveModifier(stats.getStat("UNDEAD_DAMAGE") / 100.0);
        }

        event.getDamage().additiveModifier(stats.getStat(event.getEntity() instanceof Player ? "PVP_DAMAGE" : "PVE_DAMAGE") / 100.0);
        double heal;

        if ((event.getDamage().hasType(DamageType.WEAPON) || event.getDamage().hasType(DamageType.UNARMED)) && RANDOM.nextDouble() <= stats.getStat("CRITICAL_STRIKE_CHANCE") / 100.0 && !event.getAttacker().getData().isOnCooldown(CooldownType.WEAPON_CRIT)) {
            event.getAttacker().getData().applyCooldown(CooldownType.WEAPON_CRIT, this.weaponCritCooldown);
            heal = stats.getStat("CRITICAL_STRIKE_POWER") / 100.0;

            event.getDamage().multiplicativeModifier(heal, DamageType.WEAPON);
            event.getDamage().multiplicativeModifier(heal, DamageType.UNARMED);
            event.getDamage().registerWeaponCriticalStrike();
            event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
            this.applyCritEffects(event.getEntity(), Particle.CRIT, 32, 0.4000000059604645);
        }

        if (event.getDamage().hasType(DamageType.SKILL) && RANDOM.nextDouble() <= stats.getStat("SKILL_CRITICAL_STRIKE_CHANCE") / 100.0 && !event.getAttacker().getData().isOnCooldown(CooldownType.SKILL_CRIT)) {
            event.getAttacker().getData().applyCooldown(CooldownType.SKILL_CRIT, this.skillCritCooldown);
            event.getDamage().multiplicativeModifier(stats.getStat("SKILL_CRITICAL_STRIKE_POWER") / 100.0, DamageType.SKILL);
            event.getDamage().registerSkillCriticalStrike();
            event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0F, 2.0F);
            this.applyCritEffects(event.getEntity(), Particle.TOTEM, 16, 0.4000000059604645);
        }

        heal = (event.getAttack().getDamage().getDamage(DamageType.WEAPON) * event.getAttacker().getStat("LIFESTEAL") + event.getAttack().getDamage().getDamage(DamageType.SKILL) * event.getAttacker().getStat("SPELL_VAMPIRISM")) / 100.0;
        if (heal > 0.0) {
            UtilityMethods.heal(stats.getPlayer(), heal);
        }

    }

    private void applyCritEffects(Entity entity, Particle particle, int amount, double speed) {
        Location loc = entity.getLocation().add(0.0, entity.getHeight() / 2.0, 0.0);
        double offset = entity.getBoundingBox().getWidthX() / 2.0;
        entity.getWorld().spawnParticle(particle, loc, amount, offset, offset, offset, speed);
    }
}
