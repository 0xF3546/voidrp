package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.utils.gameplay.GamePlay;
import de.polo.voidroleplay.utils.enums.Drug;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UseCommand implements CommandExecutor {
    private final GamePlay gamePlay;

    public UseCommand(GamePlay gamePlay) {
        this.gamePlay = gamePlay;
        Main.registerCommand("use", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(Main.error + "Syntax-Fehler: /use [Kokain/Joint/Schmerzmittel/Spritze/Antibiotikum]");
            return false;
        }
        int cocaineCount = ItemManager.getCustomItemCount(player, RoleplayItem.SNUFF);
        int jointCount = ItemManager.getCustomItemCount(player, RoleplayItem.CIGAR);
        int crystalCount = ItemManager.getCustomItemCount(player, RoleplayItem.CRYSTAL);
        String errorMsg = "§cDu hast nicht genug Drogen.";
        switch (args[0].toLowerCase()) {
            case "schnupftabak":
            case "kokain":
                if (cocaineCount >= 1) {
                    GamePlay.useDrug(player, Drug.COCAINE);
                } else {
                    player.sendMessage(errorMsg);
                }
                break;
            case "zigarre":
            case "joint":
                if (jointCount >= 1) {
                    GamePlay.useDrug(player, Drug.JOINT);
                } else {
                    player.sendMessage(errorMsg);
                }
                break;
            case "schmerzmittel":
                if (ItemManager.getCustomItemCount(player, RoleplayItem.SCHMERZMITTEL) >= 1) {
                    GamePlay.useDrug(player, Drug.SCHMERZMITTEL);
                } else {
                    player.sendMessage(errorMsg);
                }
                break;
            case "spritze":
                if (ItemManager.getCustomItemCount(player, RoleplayItem.ADRENALINE_INJECTION) >= 1) {
                    GamePlay.useDrug(player, Drug.ADRENALINE_INJECTION);
                } else {
                    player.sendMessage(errorMsg);
                }
                break;
            case "antibiotikum":
                if (ItemManager.getCustomItemCount(player, RoleplayItem.ANTIBIOTIKUM) >= 1) {
                    GamePlay.useDrug(player, Drug.ANTIBIOTIKUM);
                } else {
                    player.sendMessage(errorMsg);
                }
                break;
            case "crystal":
            case "kristall":
                if (crystalCount >= 1) {
                    GamePlay.useDrug(player, Drug.CRYSTAL);
                } else {
                    player.sendMessage(errorMsg);
                }
                break;
            default:
                player.sendMessage(Main.error + "Syntax-Fehler: /use [Schnupftabak/Zigarre/Kristall/Schmerzmittel/Spritze]");
                break;
        }
        return false;
    }
}
