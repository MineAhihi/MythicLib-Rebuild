//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.lumine.mythic.lib.command.mythiclib;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.api.stat.modifier.TemporaryStatModifier;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class StatModTempCommand extends CommandTreeNode {
    public StatModTempCommand(CommandTreeNode parent) {
        super(parent, "statmodtemp");
        this.addParameter(Parameter.PLAYER);
        this.addParameter(new Parameter("<STAT_NAME>", (tree, list) -> {
            list.add("ATTACK_DAMAGE");
        }));
        this.addParameter(new Parameter("<value>", (tree, list) -> {
            list.add("10");
        }));
        this.addParameter(new Parameter("(duration)", (tree, list) -> {
            for(int j = 1; j < 5; ++j) {
                list.add(String.valueOf(20 * j));
            }

        }));
    }

    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            return CommandResult.THROW_USAGE;
        } else {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return CommandResult.FAILURE;
            } else {
                String statName = UtilityMethods.enumName(args[2]);
                MMOPlayerData playerData = MMOPlayerData.get(target);
                ModifierType type = args[3].toCharArray()[args[3].length() - 1] == '%' ? ModifierType.RELATIVE : ModifierType.FLAT;
                double value = Double.parseDouble(type == ModifierType.RELATIVE ? args[3].substring(0, args[3].length() - 1) : args[3]);
                long duration = args.length > 4 ? Math.max(1L, (long)Double.parseDouble(args[4])) : 0L;
                if (duration <= 0L) {
                    StatModifier stat = (new StatModifier(UUID.randomUUID().toString(), statName, value, type, EquipmentSlot.OTHER, ModifierSource.OTHER));
                    stat.register(playerData);
                } else {
                    TemporaryStatModifier stat = (new TemporaryStatModifier(UUID.randomUUID().toString(), statName, value, type, EquipmentSlot.OTHER, ModifierSource.OTHER));
                    stat.register(playerData, duration);
                }

                return CommandResult.SUCCESS;
            }
        }
    }
}
