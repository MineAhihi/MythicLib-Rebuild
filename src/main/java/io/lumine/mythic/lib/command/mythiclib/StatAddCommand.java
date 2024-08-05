//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.lumine.mythic.lib.command.mythiclib;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatInstance;
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

public class StatAddCommand extends CommandTreeNode {
    public StatAddCommand(CommandTreeNode parent) {
        super(parent, "statadd");
        this.addParameter(Parameter.PLAYER);
        this.addParameter(new Parameter("<STAT_NAME>", (tree, list) -> {
            list.add("ATTACK_DAMAGE");
        }));
        this.addParameter(new Parameter("<key>", (tree, list) -> {
            String playerName = tree.getArguments()[1];
            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                list.add("example-key");
                return;
            }

            String statName = UtilityMethods.enumName(tree.getArguments()[2]);

            MMOPlayerData playerData = MMOPlayerData.get(target);

            if (!playerData.getStatMap().hasStat(statName)) {
                list.add("example-key");
                return;
            }

            playerData.getStatMap().getInstance(statName).getModifiers().forEach((key) -> {
                if (key.isCustom()) {
                    list.add(key.getKey());
                }
            });
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
        if (args.length < 5) {
            return CommandResult.THROW_USAGE;
        } else {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return CommandResult.FAILURE;
            } else {
                String statName = UtilityMethods.enumName(args[2]);
                String key = args[3];
                MMOPlayerData playerData = MMOPlayerData.get(target);
                ModifierType type = args[4].toCharArray()[args[4].length() - 1] == '%' ? ModifierType.RELATIVE : ModifierType.FLAT;
                double value = Double.parseDouble(type == ModifierType.RELATIVE ? args[4].substring(0, args[4].length() - 1) : args[4]);
                long duration = args.length > 5 ? Math.max(1L, (long)Double.parseDouble(args[5])) : 0L;

                double baseValue = 0;
                StatInstance statInstance = playerData.getStatMap().getInstance(statName);
                for (StatModifier statModifier : statInstance.getModifiers()) {
                    if (statModifier.getKey().equals(key)) {
                        baseValue = statModifier.getValue();
                    }
                }

                if (duration <= 0L) {
                    StatModifier stat = (new StatModifier(key, statName, value + baseValue, type, EquipmentSlot.OTHER, ModifierSource.OTHER));
                    stat.setCustom(true);
                    stat.register(playerData);
                } else {
                    TemporaryStatModifier stat = (new TemporaryStatModifier(key, statName, value + baseValue, type, EquipmentSlot.OTHER, ModifierSource.OTHER));
                    stat.setCustom(true);
                    stat.register(playerData, duration);
                }

                return CommandResult.SUCCESS;
            }
        }
    }
}
